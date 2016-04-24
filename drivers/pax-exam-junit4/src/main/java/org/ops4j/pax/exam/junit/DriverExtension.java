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
package org.ops4j.pax.exam.junit;

import java.io.IOException;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.impl.JUnitTestListener;
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
public class DriverExtension extends RunnerExtension {

    private static final Logger LOG = LoggerFactory.getLogger(DriverExtension.class);

    private Class<?> testClass;

    private ReactorManager manager;

    private StagedExamReactor stagedReactor;

    private ExtensibleRunner base;

    /**
     * @throws InitializationError
     *
     */
    public DriverExtension(Class<?> klass) throws InitializationError {
        this.testClass = klass;
    }

    /**
     * @return the base
     */
    public ParentRunner<FrameworkMethod> getBase() {
        return base;
    }

    /**
     * @param base the base to set
     * @throws InitializationError
     */
    public void setBase(ExtensibleRunner base) throws InitializationError {
        this.base = base;
        LOG.info("creating PaxExam runner for {}", testClass);
        try {
            Object testClassInstance = testClass.newInstance();
            manager = ReactorManager.getInstance();
            ExamReactor examReactor = manager.prepareReactor(testClass, testClassInstance);
            addTestsToReactor(examReactor, testClassInstance);
            stagedReactor = manager.stageReactor();
        }
        catch (InstantiationException | IllegalAccessException | IOException | ExamConfigurationException exc) {
            throw new InitializationError(exc);
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
        TestProbeBuilder probe = manager.createProbeBuilder(testClassInstance);
        probe.addTest(testClass);
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
            stagedReactor.runTest(new TestDescription(testClass.getName()), listener);
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
        JUnitTestListener testListener = new JUnitTestListener(notifier);
        try {
            stagedReactor.runTest(description, testListener);
        }
        // CHECKSTYLE:SKIP : StagedExamReactor API
        catch (Exception exc) {
            testListener.testStarted(description);
            testListener.testFailure(new TestFailure(description, exc));
            testListener.testFinished(description);
        }
    }
}
