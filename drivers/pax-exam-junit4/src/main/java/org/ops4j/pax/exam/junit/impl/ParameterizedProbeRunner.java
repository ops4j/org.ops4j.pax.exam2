/*
 * Copyright 2010 - 2013 Toni Menzel, Harald Wellmann
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
package org.ops4j.pax.exam.junit.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExceptionHelper;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestDirectory;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;
import org.ops4j.pax.exam.util.InjectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit runner for parameterized Pax Exam tests executed via an invoker. This runner is used for
 * all operation modes except CDI. See {@link Parameterized} for more details on specifying
 * parameter sets.
 *
 * @author Harald Wellmann
 *
 */
public class ParameterizedProbeRunner extends BlockJUnit4ClassRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ParameterizedProbeRunner.class);

    /**
     * Reactor manager singleton.
     */
    private ReactorManager manager;

    /**
     * Staged reactor for this test class. This may actually be a reactor already staged for a
     * previous test class, depending on the reactor strategy.
     */
    private StagedExamReactor stagedReactor;

    private Map<FrameworkMethod, TestAddress> methodToTestAddressMap = new LinkedHashMap<FrameworkMethod, TestAddress>();

    private Object[] parameters;

    private TestListener listener;

    public ParameterizedProbeRunner(Class<?> klass) throws InitializationError {
        super(klass);
        LOG.info("creating PaxExam runner for {}", klass);
        manager = ReactorManager.getInstance();
        try {
            ExamReactor examReactor = manager.prepareReactor(klass, null);
            addTestsToReactor(examReactor, klass, null);
            stagedReactor = manager.stageReactor();
        }
        catch (IOException | ExamConfigurationException exc) {
            throw new InitializationError(exc);
        }
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
            manager.beforeClass(stagedReactor, testClass);
            listener = new ClassLevelFailureListener(notifier);
            super.run(notifier);
            listener = new JUnitTestListener(notifier);
            stagedReactor.runTest(new TestDescription(getTestClass().getName(), null, 0), listener);
        }
        // CHECKSTYLE:SKIP : catch all wanted
        catch (Throwable e) {
            // rethrowing the exception does not help, we have to use the notifier here
            Description description = Description.createSuiteDescription(testClass);
            notifier.fireTestFailure(new Failure(description, e));
        }
        finally {
            manager.afterClass(stagedReactor, testClass);
        }
    }

    /**
     * Override to avoid running BeforeClass and AfterClass by the driver. They shall only be run by
     * the container when using a probe invoker.
     */
    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        Statement statement = childrenInvoker(notifier);
        return statement;
    }

    /**
     * Override to avoid running Before, After and Rule methods by the driver. They shall only be
     * run by the container when using a probe invoker.
     */
    @Override
    protected Statement methodBlock(FrameworkMethod method) {

        Object test;
        try {
            ReflectiveCallable reflectiveCallable = new ReflectiveCallable() {

                @Override
                // CHECKSTYLE:SKIP : Base class API
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            };
            test = reflectiveCallable.run();
        }
        // CHECKSTYLE:SKIP : ReflectiveCallable API
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
        if (methodToTestAddressMap.isEmpty()) {
            fillChildren();
        }
        return new ArrayList<FrameworkMethod>(methodToTestAddressMap.keySet());
    }

    private void fillChildren() {
        Set<TestAddress> targets = stagedReactor.getTargets();
        TestDirectory testDirectory = TestDirectory.getInstance();
        for (TestAddress address : targets) {
            FrameworkMethod frameworkMethod = (FrameworkMethod) manager.lookupTestMethod(address
                .root());

            // The reactor may contain targets which do not belong to the current test class
            if (frameworkMethod == null) {
                continue;
            }
            Class<?> frameworkMethodClass = frameworkMethod.getMethod().getDeclaringClass();
            String className = frameworkMethodClass.getName();
            String methodName = frameworkMethod.getName();

            if (frameworkMethodClass.isAssignableFrom(getTestClass().getJavaClass())) {
                FrameworkMethod method = new ParameterizedFrameworkMethod(address, frameworkMethod);
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

        Iterator<Object[]> it = null;
        int index = 0;
        try {
            it = allParameters().iterator();
        }
        // CHECKSTYLE:SKIP : JUnit API
        catch (Throwable t) {
            throw new ExamConfigurationException(t.getMessage());
        }

        while (it.hasNext()) {
            parameters = it.next();
            // probe.setAnchor( testClass );
            for (FrameworkMethod s : super.getChildren()) {
                // record the method -> adress matching
                TestAddress address = probe.addTest(testClass, s.getMethod().getName(), index);
                manager.storeTestMethod(address, s);
            }
            index++;
        }
        reactor.addProbe(probe);
    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        // FIXME use more robust criteria
        if (stagedReactor.getClass().getName().contains("PerMethod")) {
            return super.childrenInvoker(notifier);
        }
        else {
            return new Statement() {

                @Override
                // CHECKSTYLE:SKIP : Statement API
                public void evaluate() throws Throwable {
                    // empty
                }
            };
        }
    }



    /**
     * When using a probe invoker, we replace the super method and invoke the test method indirectly
     * via the reactor.
     */
    @Override
    protected synchronized Statement methodInvoker(final FrameworkMethod method, final Object test) {

        return new Statement() {

            @Override
            // CHECKSTYLE:SKIP : Statement API
            public void evaluate() throws Throwable {
                TestDescription description = new TestDescription(getTestClass().getName(), method.getName());

                LOG.debug("Invoke {}", description);
                try {
                    stagedReactor.runTest(description, listener);
                }
                // CHECKSTYLE:SKIP : StagedExamReactor API
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
        return getTestClass().getOnlyConstructor().newInstance(parameters);
    }

    @SuppressWarnings("unchecked")
    // CHECKSTYLE:SKIP - JUnit API
    private Iterable<Object[]> allParameters() throws Throwable {
        Object params = getParametersMethod().invokeExplosively(null);
        if (params instanceof Iterable) {
            return (Iterable<Object[]>) params;
        }
        else {
            throw parametersMethodReturnedWrongType();
        }
    }

    private FrameworkMethod getParametersMethod() throws Exception {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Parameters.class);
        for (FrameworkMethod each : methods) {
            if (each.isStatic() && each.isPublic()) {
                return each;
            }
        }

        throw new Exception("No public static parameters method on class "
            + getTestClass().getName());
    }

    private Exception parametersMethodReturnedWrongType() throws Exception {
        String className = getTestClass().getName();
        String methodName = getParametersMethod().getName();
        String message = MessageFormat.format("{0}.{1}() must return an Iterable of arrays.",
            className, methodName);
        return new TestContainerException(message);
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
        if (fieldsAreAnnotated()) {
            validateZeroArgConstructor(errors);
        }
    }

    private List<FrameworkField> getAnnotatedFieldsByParameter() {
        return getTestClass().getAnnotatedFields(Parameter.class);
    }

    private boolean fieldsAreAnnotated() {
        return !getAnnotatedFieldsByParameter().isEmpty();
    }
}
