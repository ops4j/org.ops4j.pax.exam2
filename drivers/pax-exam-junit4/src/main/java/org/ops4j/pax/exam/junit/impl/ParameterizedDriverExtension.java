/*
 * Copyright 2016 Harald Wellmann.
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
import java.util.List;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExceptionHelper;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.RunnerExtension;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;
import org.ops4j.pax.exam.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hwellmann
 *
 */
public class ParameterizedDriverExtension extends RunnerExtension {

    private static final Logger LOG = LoggerFactory.getLogger(ParameterizedDriverExtension.class);

    private final List<Runner> runners = new ArrayList<>();

    private Class<?> testClass;

    private ReactorManager manager;

    private StagedExamReactor stagedReactor;

    private ExtensibleSuite base;

    /**
     * @throws InitializationError
     *
     */
    public ParameterizedDriverExtension(Class<?> klass) throws InitializationError {
        this.testClass = klass;

    }

    /**
     * @param base the base to set
     * @throws InitializationError
     */
    public void setBase(ExtensibleSuite base) throws InitializationError {
        this.base = base;
        manager = ReactorManager.getInstance();
        ExamReactor reactor = manager.prepareReactor(testClass, null);
        try {
            addTestsToReactor(reactor, null);
            stagedReactor = manager.stageReactor();
        }
        catch (IOException | ExamConfigurationException exc) {
            throw Exceptions.unchecked(exc);
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
     * @param testClassInstance
     * @throws IOException
     * @throws ExamConfigurationException
     */
    private void addTestsToReactor(ExamReactor reactor, Object testClassInstance)
        throws IOException, ExamConfigurationException {

        if (manager.getSystemType().equals(Constants.EXAM_SYSTEM_CDI)) {
            return;
        }

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
            it.next();
            // probe.setAnchor( testClass );
            for (FrameworkMethod s : getTestClass().getAnnotatedMethods(Test.class)) {
                // record the method -> adress matching
                TestAddress address = probe.addTest(testClass, s.getMethod().getName(), index);
                manager.storeTestMethod(address, s);
            }
            index++;
        }
        reactor.addProbe(probe);
    }



    @Override
    public boolean shouldDelegateClass() {
        return !manager.getSystemType().equals(Constants.EXAM_SYSTEM_CDI) && !shouldDelegateMethod();
    }

    @Override
    public boolean shouldDelegateMethod() {
        return stagedReactor.getClass().getName().contains("PerMethod");
    }

    @Override
    public void beforeClassBlock(RunNotifier notifier) {
        LOG.debug("Driver beforeClassBlock");
        manager.beforeClass(stagedReactor, testClass);
    }

    @Override
    public void afterClassBlock(RunNotifier notifier) {
        LOG.debug("Driver afterClassBlock");
        manager.afterClass(stagedReactor, testClass);
    }

    @Override
    public void beforeMethodBlock(FrameworkMethod method) {
        LOG.debug("Driver beforeMethodBlock");
    }

    @Override
    public void afterMethodBlock(FrameworkMethod method) {
        LOG.debug("Driver afterMethodBlock");
    }

    @Override
    public Object processTestInstance(Object test) {
        LOG.debug("Driver processTestInstance");
        if (manager.getSystemType().equals(Constants.EXAM_SYSTEM_CDI)) {
            manager.inject(test);
        }
        return test;
    }

    @Override
    public void delegateClassBlock(RunNotifier notifier) {
        TestListener listener = new JUnitTestListener(notifier);
        try {
            stagedReactor.runTest(new TestDescription(testClass.getName(), null, 0), listener);
        }
        // CHECKSTYLE:SKIP : StagedExamReactor API
        catch (Exception exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    @Override
    public void delegateMethodBlock(FrameworkMethod method, RunNotifier notifier) {
        TestDescription description = new TestDescription(testClass.getName(), method.getName());

        LOG.debug("Invoke {}", description);
        try {
            JUnitTestListener testListener = new JUnitTestListener(notifier);
            stagedReactor.runTest(description, testListener);
        }
        // CHECKSTYLE:SKIP : StagedExamReactor API
        catch (Exception e) {
            Throwable t = ExceptionHelper.unwind(e);
            throw Exceptions.unchecked(t);
        }
    }

    @SuppressWarnings("unchecked")
    private Iterable<Object[]> allParameters() throws InitializationError {
        Object parameters;
        try {
            parameters = getParametersMethod().invokeExplosively(null);
        }
        // CHECKSTYLE:SKIP - JUnit API
        catch (Throwable t) {
            throw new InitializationError(t);
        }
        if (parameters instanceof Iterable) {
            return (Iterable<Object[]>) parameters;
        }
        else {
            throw parametersMethodReturnedWrongType();
        }
    }

    private FrameworkMethod getParametersMethod() throws InitializationError {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Parameters.class);
        for (FrameworkMethod each : methods) {
            if (each.isStatic() && each.isPublic()) {
                return each;
            }
        }

        throw new InitializationError("No public static parameters method on class "
            + getTestClass().getName());
    }

    private InitializationError parametersMethodReturnedWrongType() throws InitializationError {
        String className = getTestClass().getName();
        String methodName = getParametersMethod().getName();
        String message = MessageFormat.format("{0}.{1}() must return an Iterable of arrays.",
            className, methodName);
        return new InitializationError(message);
    }

    private TestClass getTestClass() {
        return base.getTestClass();
    }

    public List<Runner> getChildren() {
        return runners;
    }

    public Description describeChild(Runner child) {
        return child.getDescription();
    }

    public void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier);
    }

}
