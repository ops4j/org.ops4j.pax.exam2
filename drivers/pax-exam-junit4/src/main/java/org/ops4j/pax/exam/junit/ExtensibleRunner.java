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

import java.util.List;

import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * @author hwellmann
 *
 */
public class ExtensibleRunner extends BlockJUnit4ClassRunner {

    private SpyingFilter spyingFilter;

    private RunnerExtension extension;

    public ExtensibleRunner(Class<?> klass, RunnerExtension extension) throws InitializationError {
        super(klass);
        this.extension = extension;
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {
        final Statement statement = super.classBlock(notifier);
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                extension.beforeClassBlock(notifier);
                try {
                    if (extension.shouldDelegateClass()) {
                        extension.delegateClassBlock(notifier);
                    }
                    else {
                        statement.evaluate();
                    }
                }
                finally {
                    extension.afterClassBlock(notifier);
                }
            }
        };
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        spyingFilter = new SpyingFilter(filter);
        super.filter(spyingFilter);
    }

    @Override
    protected Statement methodBlock(final FrameworkMethod method) {
        final Statement statement = super.methodBlock(method);
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                extension.beforeMethodBlock(method);
                try {
                    statement.evaluate();
                }
                finally {
                    extension.afterMethodBlock(method);
                }
            }
        };
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (!extension.shouldDelegateClass()) {
            if (extension.shouldDelegateMethod()) {
                extension.delegateMethodBlock(method, notifier);
            }
            else {
                super.runChild(method, notifier);
            }
        }
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();
        return extension.processTestInstance(test);
    }

    @Override
    public List<FrameworkMethod> getChildren() {
        return super.getChildren();
    }
}
