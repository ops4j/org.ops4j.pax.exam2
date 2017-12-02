/*
 * Copyright 2012 Harald Wellmann.
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
package org.ops4j.pax.exam.spi.reactors;

import static java.util.stream.Collectors.toList;
import static org.ops4j.pax.exam.Constants.EXAM_REACTOR_STRATEGY_KEY;
import static org.ops4j.pax.exam.Constants.EXAM_REACTOR_STRATEGY_PER_CLASS;
import static org.ops4j.pax.exam.Constants.EXAM_REACTOR_STRATEGY_PER_METHOD;
import static org.ops4j.pax.exam.Constants.EXAM_REACTOR_STRATEGY_PER_SUITE;
import static org.ops4j.pax.exam.Constants.EXAM_SERVICE_TIMEOUT_DEFAULT;
import static org.ops4j.pax.exam.Constants.EXAM_SERVICE_TIMEOUT_KEY;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_CDI;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_DEFAULT;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_JAVAEE;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_KEY;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_TEST;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.WarProbeOption;
import org.ops4j.pax.exam.spi.DefaultExamReactor;
import org.ops4j.pax.exam.spi.DefaultExamSystem;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.PaxExamRuntime;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.InjectorFactory;
import org.ops4j.spi.ServiceProviderFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the exam system and reactor required by a test driver. This class is a singleton and
 * keeps track of all tests in the current test suite and lets a reactor reuse the Exam system and
 * the test probe, where applicable.
 *
 * <p>
 * This class was factored out from the JUnit4TestRunner of Pax Exam 2.x and does not depend on
 * JUnit.
 * <p>
 * TODO check if there are any concurrency issues. Some methods are synchronized, which is just
 * inherited from the 2.1.0 implementation. The use cases are not quite clear.
 *
 * @author Harald Wellmann
 */
public class ReactorManager {

    private static final Logger LOG = LoggerFactory.getLogger(ReactorManager.class);

    /** Singleton instance of this manager. */
    private static ReactorManager instance;

    private Map<String, StagedExamReactorFactory> reactorStrategies;

    /** Exam system, containing system and user configuration options. */
    private ExamSystem system;

    /** The system type, which determines the kind of probe to be used. */
    private String systemType;

    /** The current test class. */
    private Class<?> currentTestClass;

    /** The reactor. */
    private ExamReactor reactor;

    private StagedExamReactor currentStagedReactor;

    /**
     * A probe builder for the current test probe. A probe builder contains a number of test classes
     * and their dependent classes and a list of test methods to be executed.
     * <p>
     * Test methods are added incrementally as classes are scanned. By default, the same probe
     * builder is reused for all test classes, unless a given class overrides the default probe
     * configuration.
     */
    private TestProbeBuilder defaultProbeBuilder;

    /**
     * Set of test classes in suite.
     */
    private Set<Class<?>> testClasses = new HashSet<Class<?>>();

    /**
     * Has the suite been started? Set to true when the first test class is about to run.
     */
    private boolean suiteStarted;

    /**
     * Configuration property access.
     */
    private ConfigurationManager cm;

    private boolean waitForAfterSuiteEvent;

    /**
     * Private constructor for singleton.
     */
    private ReactorManager() {
        try {
            cm = new ConfigurationManager();
            system = createExamSystem();
            reactorStrategies = new HashMap<>();
            reactorStrategies.put(EXAM_REACTOR_STRATEGY_PER_SUITE, new PerSuite());
            reactorStrategies.put(EXAM_REACTOR_STRATEGY_PER_CLASS, new PerClass());
            reactorStrategies.put(EXAM_REACTOR_STRATEGY_PER_METHOD, new PerMethod());
        }
        catch (IOException exc) {
            throw new TestContainerException("cannot create Exam system", exc);
        }
    }

    /**
     * Returns the singleton ReactorManager instance.
     *
     * @return reactor manager
     */
    public static synchronized ReactorManager getInstance() {
        if (instance == null) {
            instance = new ReactorManager();
        }
        return instance;
    }

    /**
     * Prepares the unstaged reactor for the given test class instance. Any configurations from
     * {@code Configuration} methods of the class are added to the reactor.
     *
     * @param _testClass
     *            test class
     * @param testClassInstance
     *            instance of test class
     * @return reactor
     */
    public synchronized ExamReactor prepareReactor(Class<?> _testClass, Object testClassInstance) {
        this.currentTestClass = _testClass;
        this.reactor = createReactor(_testClass);
        testClasses.add(_testClass);
        try {
            addConfigurationsToReactor(_testClass, testClassInstance);
        }
        catch (IllegalAccessException exc) {
            throw new TestContainerException(exc);
        }
        catch (InvocationTargetException exc) {
            Throwable cause = exc.getCause();
            if (cause instanceof AssertionError) {
                throw (AssertionError)cause;
            }
            else {
                throw new TestContainerException(cause);
            }
        }
        return reactor;
    }

    /**
     * Stages the reactor for the current class.
     *
     * @return staged reactor
     */
    public StagedExamReactor stageReactor() {
        this.currentStagedReactor = reactor.stage(getStagingFactory(currentTestClass));
        return currentStagedReactor;
    }

    public StagedExamReactor getStagedReactor() {
        return currentStagedReactor;
    }

    private ExamSystem createExamSystem() throws IOException {
        systemType = cm.getProperty(EXAM_SYSTEM_KEY, EXAM_SYSTEM_TEST);
        String timeout = cm.getProperty(EXAM_SERVICE_TIMEOUT_KEY, EXAM_SERVICE_TIMEOUT_DEFAULT);
        Option timeoutOption = new SystemPropertyOption(EXAM_SERVICE_TIMEOUT_KEY).value(timeout);
        if (EXAM_SYSTEM_DEFAULT.equals(systemType)) {
            system = DefaultExamSystem.create(new Option[] { timeoutOption });
        }
        else if (EXAM_SYSTEM_JAVAEE.equals(systemType)) {
            WarProbeOption warProbe = new WarProbeOption().classPathDefaultExcludes();
            system = DefaultExamSystem.create(new Option[] { warProbe });
        }
        else {
            system = PaxExamRuntime.createTestSystem(timeoutOption);
        }
        return system;
    }

    /**
     * Scans the current test class for declared or inherited {@code @Configuration} methods and
     * invokes them, adding the returned configuration to the reactor.
     *
     * @param testClass
     *            test class
     * @param testClassInstance
     *            instance of test class
     * @throws IllegalAccessException
     *             when configuration method is not public
     * @throws InvocationTargetException
     *             when configuration method cannot be invoked
     * @throws ExamConfigurationException
     */
    private void addConfigurationsToReactor(Class<?> testClass, Object testClassInstance)
        throws IllegalAccessException, InvocationTargetException {
        List<Method> configMethods = Stream.of(testClass.getMethods()).filter(this::isConfiguration).collect(toList());
        if (configMethods.size() > 1) {
            String msg = String.format("Test class %s has multiple configuration methods: %s. As of Pax Exam 5.0.0., at most one configuration method is supported", testClass, configMethods);
            throw new TestContainerException(msg);
        }
        reactor.addConfiguration(((Option[]) configMethods.get(0).invoke(testClassInstance)));
    }

    private boolean isConfiguration(Method m) {
        Configuration conf = m.getAnnotation(Configuration.class);
        return (conf != null);
    }

    /**
     * Creates a staging factory indicated by the {@link ExamReactorStrategy} annotation of the test
     * class.
     *
     * @param testClass
     * @return staging factory
     */
    private StagedExamReactorFactory getStagingFactory(Class<?> testClass) {
        ExamReactorStrategy strategy = testClass.getAnnotation(ExamReactorStrategy.class);
        String strategyName = cm.getProperty(EXAM_REACTOR_STRATEGY_KEY);
        StagedExamReactorFactory fact;
        try {
            if (strategy != null) {
                fact = strategy.value()[0].newInstance();
                return fact;
            }
        }
        catch (IllegalAccessException | InstantiationException exc) {
            throw new TestContainerException(exc);
        }

        if (strategyName == null) {
            if (systemType.equals(EXAM_SYSTEM_CDI) || systemType.equals(EXAM_SYSTEM_JAVAEE)) {
                strategyName = EXAM_REACTOR_STRATEGY_PER_SUITE;
            }
            else {
                // OSGi default from Pax Exam 2.x
                strategyName = EXAM_REACTOR_STRATEGY_PER_METHOD;
            }
        }
        fact = reactorStrategies.get(strategyName);
        if (fact == null) {
            throw new IllegalArgumentException("unknown reactor strategy " + strategyName);
        }
        return fact;
    }

    /**
     * Creates an unstaged reactor for the given test class.
     *
     * @param testClass
     * @return unstaged reactor
     */
    private DefaultExamReactor createReactor(Class<?> testClass) {
        return new DefaultExamReactor(system, PaxExamRuntime.getTestContainerFactory());
    }

    /**
     * Lazily creates a probe builder. The same probe builder will be reused for all test classes,
     * unless the default builder is overridden in a given class.
     *
     * @param testClassInstance instance of test class
     * @return probe builder
     * @throws IOException when probe cannot be created
     * @throws ExamConfigurationException when user-defined probe cannot be created
     */
    public TestProbeBuilder createProbeBuilder(Object testClassInstance) throws IOException,
        ExamConfigurationException {
        if (defaultProbeBuilder == null) {
            defaultProbeBuilder = system.createProbe();
        }
        TestProbeBuilder probeBuilder = overwriteWithUserDefinition(currentTestClass,
            testClassInstance);
        if (probeBuilder.getTempDir() == null) {
            probeBuilder.setTempDir(defaultProbeBuilder.getTempDir());
        }
        return probeBuilder;
    }

    private TestProbeBuilder overwriteWithUserDefinition(Class<?> testClass, Object testInstance)
        throws ExamConfigurationException {
        Method[] methods = testClass.getMethods();
        for (Method m : methods) {
            if (isProbeBuilder(m)) {
                LOG.debug("User defined probe hook found: " + m.getName());
                TestProbeBuilder probeBuilder;
                try {
                    probeBuilder = (TestProbeBuilder) m.invoke(testInstance, defaultProbeBuilder);
                }
                // CHECKSTYLE:SKIP : catch all wanted
                catch (Exception e) {
                    throw new ExamConfigurationException("Invoking custom probe hook "
                        + m.getName() + " failed", e);
                }
                if (probeBuilder != null) {
                    return probeBuilder;
                }
                else {
                    throw new ExamConfigurationException("Invoking custom probe hook "
                        + m.getName() + " succeeded but returned null");
                }
            }
        }
        LOG.debug("No User defined probe hook found");
        return defaultProbeBuilder;
    }

    private boolean isProbeBuilder(Method m) {
        ProbeBuilder builder = m.getAnnotation(ProbeBuilder.class);
        return (builder != null);
    }

    /**
     * @return the systemType
     */
    public String getSystemType() {
        return systemType;
    }

    public void beforeSuite(StagedExamReactor stagedReactor) {
        stagedReactor.beforeSuite();
        suiteStarted = true;
        waitForAfterSuiteEvent = true;
    }

    public void afterSuite(StagedExamReactor stagedReactor) {
        waitForAfterSuiteEvent = false;
        stagedReactor.afterSuite();
    }

    public void afterClass(StagedExamReactor stagedReactor, Class<?> klass) {
        stagedReactor.afterClass();
        testClasses.remove(klass);
        if (!waitForAfterSuiteEvent && testClasses.isEmpty()) {
            LOG.info("suite finished");
            stagedReactor.afterSuite();
            suiteStarted = false;
            testClasses.clear();
        }
    }

    public void beforeClass(StagedExamReactor stagedReactor, Object testClassInstance) {
        if (!suiteStarted) {
            suiteStarted = true;
            stagedReactor.beforeSuite();
        }
        stagedReactor.beforeClass();
    }

    /**
     * Performs field injection on the given test class instance.
     *
     * @param test
     *            test class instance
     */
    public void inject(Object test) {
        Injector injector = findInjector();
        injector.injectFields(test);
    }

    /**
     * Finds an injector factory and creates an injector.
     *
     * @return injector
     */
    private Injector findInjector() {
        InjectorFactory injectorFactory = ServiceProviderFinder
            .loadUniqueServiceProvider(InjectorFactory.class);
        return injectorFactory.createInjector();
    }
}
