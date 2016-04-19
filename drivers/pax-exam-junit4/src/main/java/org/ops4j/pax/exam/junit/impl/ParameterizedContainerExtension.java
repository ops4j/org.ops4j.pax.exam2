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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;
import org.ops4j.pax.exam.junit.RunnerExtension;
import org.ops4j.pax.exam.util.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hwellmann
 *
 */
public class ParameterizedContainerExtension extends RunnerExtension {

    private static final Logger LOG = LoggerFactory.getLogger(ParameterizedContainerExtension.class);

    private final List<Runner> runners = new ArrayList<>();

    private ExtensibleSuite base;

    private Injector injector;

    public ParameterizedContainerExtension(Class<?> klass, Injector injector) throws InitializationError {
        this.injector = injector;
        Parameters parameters = getParametersMethod().getAnnotation(Parameters.class);
        createRunnersForParameters(allParameters(), parameters.name());
    }

    @Override
    public Object processTestInstance(Object test) {
        LOG.debug("Driver processTestInstance");
        injector.injectFields(test);
        return test;
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

    private void createRunnersForParameters(Iterable<Object[]> allParameters, String namePattern)
        throws InitializationError {
        try {
            int i = 0;
            for (Object[] parametersOfSingleTest : allParameters) {
                String name = nameFor(namePattern, i, parametersOfSingleTest);
                TestClassRunnerForParameters runner = new TestClassRunnerForParameters(
                    getTestClass().getJavaClass(), parametersOfSingleTest, name);
                runners.add(runner);
                ++i;
            }
        }
        catch (ClassCastException e) {
            throw parametersMethodReturnedWrongType();
        }
    }

    private String nameFor(String namePattern, int index, Object[] parameters) {
        String finalPattern = namePattern.replaceAll("\\{index\\}", Integer.toString(index));
        String name = MessageFormat.format(finalPattern, parameters);
        return "[" + name + "]";
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
