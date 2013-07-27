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

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.junit.impl.InjectingRunner;
import org.ops4j.pax.exam.junit.impl.ProbeRunner;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;

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
public class PaxExam extends Runner {
    
    private Runner delegate;
    private Class<?> testClass;
    
    public PaxExam(Class<?> klass) throws InitializationError {
        this.testClass = klass;
        createDelegate();
    }

    private void createDelegate() throws InitializationError {
        ReactorManager manager = ReactorManager.getInstance();
        if (manager.getSystemType().equals(Constants.EXAM_SYSTEM_CDI)) {
            delegate = new InjectingRunner(testClass);
        }
        else {
            delegate = new ProbeRunner(testClass);
        }
    }

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        delegate.run(notifier);
    }
}
