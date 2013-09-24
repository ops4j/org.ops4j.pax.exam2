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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;
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
 * This class was factored out from the JUnit4TestRunner of Pax Exam 2.x and does not depend on JUnit.
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
     * Maps test addresses to driver-dependent test method wrappers. A test address is a unique
     * identifier for a test method in a given container which is used by a {@link ProbeInvoker} for
     * indirectly invoking the test method in the container.
     * <p>
     * This map is not used when tests are executed directly, i.e. without invoker.
     */
    private Map<TestAddress, Object> testAddressToMethodMap = new LinkedHashMap<TestAddress, Object>();

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

    private AnnotationHandler annotationHandler;

    private int numConfigurations;

    /**
     * Private constructor for singleton.
     */
    private ReactorManager() {
        try {
            cm = new ConfigurationManager();
            system = createExamSystem();
            reactorStrategies = new HashMap<String, StagedExamReactorFactory>(3);
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
     * @param testClassInstance
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     */
    public synchronized ExamReactor prepareReactor(Class<?> _testClass, Object testClassInstance)
        throws InstantiationException, IllegalAccessException, InvocationTargetException, IOException {
        this.currentTestClass = _testClass;
        this.reactor = createReactor(_testClass);
        testClasses.add(_testClass);
        addConfigurationsToReactor(_testClass, testClassInstance);
        return reactor;
    }

    /**
     * Stages the reactor for the current class.
     * 
     * @return staged reactor
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public StagedExamReactor stageReactor() throws IOException, InstantiationException,
        IllegalAccessException {
        StagedExamReactor stagedReactor = reactor.stage(getStagingFactory(currentTestClass));
        return stagedReactor;
    }

    private ExamSystem createExamSystem() throws IOException {
        systemType = cm.getProperty(EXAM_SYSTEM_KEY, EXAM_SYSTEM_TEST);
        String timeout = cm.getProperty(EXAM_SERVICE_TIMEOUT_KEY, EXAM_SERVICE_TIMEOUT_DEFAULT);
        Option timeoutOption = new SystemPropertyOption(EXAM_SERVICE_TIMEOUT_KEY).value(timeout);
        if (EXAM_SYSTEM_DEFAULT.equals(systemType)) {
            system = DefaultExamSystem.create(new Option[] { timeoutOption });
        }
        else if (EXAM_SYSTEM_JAVAEE.equals(systemType)) {
            system = DefaultExamSystem.create(new Option[] { new WarProbeOption() });
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
     * @param testClassInstance
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    private void addConfigurationsToReactor(Class<?> testClass, Object testClassInstance)
        throws IllegalAccessException, InvocationTargetException, IOException {
        numConfigurations = 0;
        Method[] methods = testClass.getMethods();
        for (Method m : methods) {
            if (isConfiguration(m)) {
                // consider as option, so prepare that one:
                reactor.addConfiguration(((Option[]) m.invoke(testClassInstance)));
                numConfigurations++;
            }
        }
    }

    /**
     * Returns the number of configurations for the current reactor.
     * 
     * @return number of configurations
     */
    public int getNumConfigurations() {
        return numConfigurations;
    }

    private boolean isConfiguration(Method m) {
        Configuration conf = m.getAnnotation(Configuration.class);
        return (conf != null) || annotationHandler.isConfiguration(m);
    }

    /**
     * Creates a staging factory indicated by the {@link ExamReactorStrategy} annotation of the test
     * class.
     * 
     * @param testClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private StagedExamReactorFactory getStagingFactory(Class<?> testClass)
        throws InstantiationException, IllegalAccessException {
        ExamReactorStrategy strategy = testClass.getAnnotation(ExamReactorStrategy.class);
        String strategyName = cm.getProperty(EXAM_REACTOR_STRATEGY_KEY);
        StagedExamReactorFactory fact;
        if (strategy != null) {
            fact = strategy.value()[0].newInstance();
            return fact;
        }

        fact = annotationHandler.createStagedReactorFactory(testClass);
        if (fact != null) {
            return fact;
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
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private DefaultExamReactor createReactor(Class<?> testClass) throws InstantiationException,
        IllegalAccessException {
        return new DefaultExamReactor(system, createsTestContainerFactory(testClass));
    }

    /**
     * Creates the test container factory to be used by the reactor.
     * <p>
     * TODO Do we really need this?
     * 
     * @param testClass
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private TestContainerFactory createsTestContainerFactory(Class<?> testClass)
        throws IllegalAccessException, InstantiationException {
        ExamFactory f = testClass.getAnnotation(ExamFactory.class);

        TestContainerFactory fact;
        if (f != null) {
            fact = f.value().newInstance();
            return fact;
        }

        fact = annotationHandler.createTestContainerFactory(testClass);

        if (fact == null) {
            // default:
            fact = PaxExamRuntime.getTestContainerFactory();
        }
        return fact;
    }

    /**
     * Lazily creates a probe builder. The same probe builder will be reused for all test classes,
     * unless the default builder is overridden in a given class.
     * 
     * @param testClassInstance
     * @return
     * @throws IOException
     * @throws ExamConfigurationException
     */
    public TestProbeBuilder createProbeBuilder(Object testClassInstance) throws IOException,
        ExamConfigurationException {
        if (defaultProbeBuilder == null) {
            defaultProbeBuilder = system.createProbe();
        }
        TestProbeBuilder probeBuilder = overwriteWithUserDefinition(currentTestClass, testClassInstance);
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
        return (builder != null) || annotationHandler.isProbeBuilder(m);
    }

    /**
     * @return the systemType
     */
    public String getSystemType() {
        return systemType;
    }

    /**
     * Looks up a test method for a given address.
     * 
     * @param address
     *            test method address used by probe
     * @return test method wrapper - the type is only known to the test driver.
     */
    public Object lookupTestMethod(TestAddress address) {
        return testAddressToMethodMap.get(address);
    }

    /**
     * Stores the test method wrapper for a given test address
     * 
     * @param address
     *            test method address used by probe
     * @param testMethod
     *            test method wrapper - the type is only known to the test driver
     */
    public void storeTestMethod(TestAddress address, Object testMethod) {
        testAddressToMethodMap.put(address, testMethod);
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
            testAddressToMethodMap.clear();
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
     * @return
     */
    private Injector findInjector() {
        InjectorFactory injectorFactory = ServiceProviderFinder
            .loadUniqueServiceProvider(InjectorFactory.class);
        return injectorFactory.createInjector();
    }

    /**
     * @param annotationHandler
     *            the annotationHandler to set
     */
    public void setAnnotationHandler(AnnotationHandler annotationHandler) {
        this.annotationHandler = annotationHandler;
    }
}
