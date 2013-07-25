/*
 * Copyright 2011 Harald Wellmann
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
package org.ops4j.pax.exam.invoker.junit.internal;

import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;
import org.ops4j.pax.exam.util.Injector;

public class ContainerTestRunnerBuilder extends RunnerBuilder {

    private Injector injector;
    private Integer index;

    /**
     * Constructs a request for the given class which will be injected with dependencies from the
     * given bundle context by the given injector
     * 
     * @param testClass
     *            test class to be run
     * @param injector
     *            injector for injecting dependencies
     * @param index
     *            parameter set index (counting from 0). Only for parameterized tests, null
     *            otherwise.
     */
    public ContainerTestRunnerBuilder(Injector injector, Integer index) {
        this.injector = injector;
        this.index = index;
    }

    @Override
    // CHECKSTYLE:SKIP : base class API
    public Runner runnerForClass(Class<?> testClass) throws Throwable {
        if (index == null) {
            return new ContainerTestRunner(testClass, injector);
        }
        else {
            return new ParameterizedContainerTestRunner(testClass, injector, index);
        }
    }
}
