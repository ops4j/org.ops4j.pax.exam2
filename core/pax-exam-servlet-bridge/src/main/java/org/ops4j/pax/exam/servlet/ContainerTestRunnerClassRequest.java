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
package org.ops4j.pax.exam.servlet;

import org.junit.internal.requests.ClassRequest;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;
import org.ops4j.pax.exam.util.Injector;

public class ContainerTestRunnerClassRequest extends ClassRequest {

    private Class<?> testClass;
    private Injector injector;

    public ContainerTestRunnerClassRequest(Class<?> testClass, 
            Injector injector) {
        super(testClass);
        this.testClass = testClass;
        this.injector = injector;
    }

    @Override
    public Runner getRunner() {
        RunnerBuilder builder = new ContainerTestRunnerBuilder(injector);
        return builder.safeRunnerForClass(testClass);
    }

}
