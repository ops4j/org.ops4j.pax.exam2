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

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.junit.ExtensibleRunner;
import org.ops4j.pax.exam.junit.RunnerExtension;
import org.ops4j.pax.exam.junit.impl.JUnitTestListener;
import org.ops4j.pax.exam.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hwellmann
 *
 */
public class MockDriverExtension extends RunnerExtension {

    private static final Logger LOG = LoggerFactory.getLogger(MockDriverExtension.class);

    private Class<?> testClass;

    private ExtensibleRunner base;

    public MockDriverExtension(Class<?> klass) throws InitializationError {
        this.testClass = klass;
    }

    public ParentRunner<FrameworkMethod> getBase() {
        return base;
    }

    public void setBase(ExtensibleRunner base) throws InitializationError {
        this.base = base;
    }

    @Override
    public boolean shouldDelegateClass() {
        return !shouldDelegateMethod();
    }

    @Override
    public boolean shouldDelegateMethod() {
        return false;
    }

    @Override
    public void beforeClassBlock(RunNotifier notifier) {
        LOG.debug("Driver beforeClassBlock");
    }

    @Override
    public void afterClassBlock(RunNotifier notifier) {
        LOG.debug("Driver afterClassBlock");
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
        return test;
    }

    @Override
    public void delegateClassBlock(RunNotifier notifier) {

        try {
            ExtensibleRunner runner = new ExtensibleRunner(testClass, new MockContainerExtension());
            JUnitCore junit = new JUnitCore();
            JUnitTestListener testListener = new JUnitTestListener(notifier);
            junit.addListener(new MockRunListener(testListener));
            junit.run(runner);
        }
        catch (Exception exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    @Override
    public void delegateMethodBlock(FrameworkMethod method, RunNotifier notifier) {
        try {
            ExtensibleRunner runner = new ExtensibleRunner(testClass, new MockContainerExtension());
            runner.filter(Filter.matchMethodDescription(toDescription(method)));
            JUnitCore junit = new JUnitCore();
            JUnitTestListener testListener = new JUnitTestListener(notifier);
            junit.addListener(new MockRunListener(testListener));
            junit.run(runner);
        }
        catch (InitializationError | NoTestsRemainException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    private Description toDescription(FrameworkMethod method) {
        return Description.createTestDescription(method.getDeclaringClass(), method.getName());
    }
}
