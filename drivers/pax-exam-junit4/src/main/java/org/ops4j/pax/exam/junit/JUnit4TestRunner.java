/*
 * Copyright 2010 - 2012 Toni Menzel, Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.junit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExceptionHelper;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDirectory;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;
import org.ops4j.pax.exam.util.InjectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default Test Runner using the Exam plumbing API. Its also the blueprint for custom,
 * much more specific runners. This will make a single probe bundling in all @Tests in this class.
 * 
 * This uses the whole regression class as a single unit of tests with the following valid
 * annotations: - @Configuration -> Configuration 1:N. Multiple configurations will result in
 * multiple invocations of the same regression. - @ProbeBuilder -> Customize the probe creation. - @Test
 * -> Single tests to be invoked. Note that in @Configuration you can specify the invocation
 * strategy.
 * 
 * @author Toni Menzel
 * @author Harald Wellmann
 */
public class JUnit4TestRunner extends BlockJUnit4ClassRunner {

    private static Logger LOG = LoggerFactory.getLogger(JUnit4TestRunner.class);

    /**
     * Reactor manager singleton.
     */
    private ReactorManager manager;

    /**
     * Staged reactor for this test class. This may actually be a reactor already staged for a
     * previous test class, depending on the reactor strategy.
     */
    private StagedExamReactor reactor;

    /**
     * Shall we use a probe invoker, or invoke test methods directly?
     */
    private boolean useProbeInvoker;

    private Map<FrameworkMethod, TestAddress> methodToTestAddressMap = new LinkedHashMap<FrameworkMethod, TestAddress>();

    public JUnit4TestRunner(Class<?> klass) throws Exception {
        super(klass);
        LOG.info("creating PaxExam runner for {}", klass);
        Object testClassInstance = klass.newInstance();

        manager = ReactorManager.getInstance();
        manager.setAnnotationHandler(new JUnitLegacyAnnotationHandler());
        ExamReactor examReactor = manager.prepareReactor(klass, testClassInstance);
        useProbeInvoker = !manager.getSystemType().equals(Constants.EXAM_SYSTEM_CDI);
        if (useProbeInvoker) {
            addTestsToReactor(examReactor, klass, testClassInstance);
        }
        reactor = manager.stageReactor();
    }

    /**
     * We decorate the super method by reactor setup and teardown. This method is called once per
     * class. Note that the given reactor strategy decides whether or not the setup and teardown
     * actually happens at this level.
     */
    @Override
    public void run(RunNotifier notifier) {
        LOG.info("running test class {}", getTestClass().getName());
        Class<?> testClass = getTestClass().getJavaClass();
        try {
            manager.beforeClass(reactor, testClass);
            super.run(notifier);
        }
        catch (Exception e) {
            // rethrowing the exception does not help, we have to use the notifier here
            Description description = Description.createSuiteDescription(testClass);
            notifier.fireTestFailure(new Failure(description, e));
        }
        finally {
            manager.afterClass(reactor, testClass);
        }
    }

    /**
     * Override to avoid running BeforeClass and AfterClass by the driver. They shall only be run by
     * the container when using a probe invoker.
     */
    protected Statement classBlock(final RunNotifier notifier) {
        if (useProbeInvoker) {
            Statement statement = childrenInvoker(notifier);
            return statement;
        }
        else {
            return super.classBlock(notifier);
        }
    }

    /**
     * Override to avoid running Before, After and Rule methods by the driver. They shall only be
     * run by the container when using a probe invoker.
     */
    protected Statement methodBlock(FrameworkMethod method) {
        if (!useProbeInvoker) {
            return super.methodBlock(method);
        }

        Object test;
        try {
            test = new ReflectiveCallable() {

                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        }
        catch (Throwable e) {
            return new Fail(e);
        }

        Statement statement = methodInvoker(method, test);
        return statement;
    }

    /**
     * When using a probe invoker, we replace the test methods of this class by a potentially larger
     * set of decorated test methods. Each original test method may give rise to multiple copies per
     * test container or configuration.
     */
    @Override
    protected List<FrameworkMethod> getChildren() {
        if (!useProbeInvoker) {
            return super.getChildren();
        }

        if (methodToTestAddressMap.isEmpty()) {
            fillChildren();
        }
        return new ArrayList<FrameworkMethod>(methodToTestAddressMap.keySet());
    }

    private void fillChildren() {
        Set<TestAddress> targets = reactor.getTargets();
        TestDirectory testDirectory = TestDirectory.getInstance();
        boolean mangleMethodNames = manager.getNumConfigurations() > 1;
        for (TestAddress address : targets) {
            FrameworkMethod frameworkMethod = (FrameworkMethod) manager.lookupTestMethod(address
                .root());
            String className = frameworkMethod.getMethod().getDeclaringClass().getName();
            String methodName = frameworkMethod.getName();

            if (className.equals(getTestClass().getName())) {
                FrameworkMethod method = mangleMethodNames ? new DecoratedFrameworkMethod(address,
                    frameworkMethod) : frameworkMethod;
                testDirectory.add(address, new TestInstantiationInstruction(className + ";"
                    + methodName));

                methodToTestAddressMap.put(method, address);
            }
        }
    }

    /**
     * Adds test methods to the reactor, mapping method names to test addresses which are used by
     * the probe invoker.
     * <p>
     * Note that when a collection of test classes is passed to an external JUnit runner like
     * Eclipse or Maven Surefire, this method is invoked (via the constructor of this runner) for
     * each class <em>before</em> the {@link #run(RunNotifier)} method is invoked for any class.
     * <p>
     * This way, we can register all test methods in the reactor before the actual test execution
     * starts.
     * 
     * @param reactor
     * @param testClass
     * @param testClassInstance
     * @throws IOException
     * @throws ExamConfigurationException
     */
    private void addTestsToReactor(ExamReactor reactor, Class<?> testClass, Object testClassInstance)
        throws IOException, ExamConfigurationException {
        TestProbeBuilder probe = manager.createProbeBuilder(testClassInstance);

        // probe.setAnchor( testClass );
        for (FrameworkMethod s : super.getChildren()) {
            // record the method -> adress matching
            TestAddress address = delegateTest(testClassInstance, probe, s);
            if (address == null) {
                address = probe.addTest(testClass, s.getMethod().getName());
            }
            manager.storeTestMethod(address, s);
        }
        reactor.addProbe(probe);
    }

    /**
     * FIXME What is this doing, and what is the use case? Parameterized methods break JUnit's
     * default behaviour, and most of these non-standard signatures introduced in 2.0.0 have been
     * dropped since 2.3.0.
     * 
     * @param testClassInstance
     * @param probe
     * @param s
     * @return
     */
    private TestAddress delegateTest(Object testClassInstance, TestProbeBuilder probe,
        FrameworkMethod s) {
        try {
            Class<?>[] types = s.getMethod().getParameterTypes();
            if (types.length == 1 && types[0].isAssignableFrom(TestProbeBuilder.class)) {
                // do some backtracking:
                return (TestAddress) s.getMethod().invoke(testClassInstance, probe);

            }
            else {
                return null;
            }
        }
        catch (Exception e) {
            throw new TestContainerException("Problem delegating to test.", e);
        }
    }

    /**
     * When using a probe invoker, we replace the super method and invoke the test method indirectly
     * via the reactor.
     */
    protected synchronized Statement methodInvoker(final FrameworkMethod method, final Object test) {
        if (!useProbeInvoker) {
            return super.methodInvoker(method, test);
        }

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                TestAddress address = methodToTestAddressMap.get(method);
                TestAddress root = address.root();

                LOG.debug("Invoke " + method.getName() + " @ " + address + " Arguments: "
                    + root.arguments());
                try {
                    reactor.invoke(address);
                }
                catch (Exception e) {
                    Throwable t = ExceptionHelper.unwind(e);
                    throw t;
                }
            }
        };
    }

    /**
     * Creates an instance of the current test class. When using a probe invoker, this simply
     * delegates to super. Otherwise, we perform injection on the instance created by the super
     * method.
     * <p>
     * In this case, an {@link InjectorFactory} is obtained via SPI lookup.
     */
    @Override
    protected Object createTest() throws Exception {
        if (useProbeInvoker) {
            return super.createTest();
        }
        else {
            Object test = super.createTest();
            manager.inject(test);
            return test;
        }
    }
}
