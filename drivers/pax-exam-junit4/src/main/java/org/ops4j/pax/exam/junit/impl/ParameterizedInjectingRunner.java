/*
 * Copyright 2013 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.junit.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;

/**
 * JUnit runner for parameterized Pax Exam tests without an invoker. This runner is
 * used for CDI mode only.
 * <p> 
 * See {@link Parameterized} for more details on specifying parameter sets.
 * <p>
 * Based on {@code org.junit.runners.Parameterized}.
 * 
 * @author Harald Wellmann
 * 
 */
public class ParameterizedInjectingRunner extends Suite {

    private static final List<Runner> NO_RUNNERS = Collections.<Runner> emptyList();

    private final ArrayList<Runner> runners = new ArrayList<Runner>();

    private ReactorManager manager;

    private StagedExamReactor stagedReactor;

    public ParameterizedInjectingRunner(Class<?> klass) throws InitializationError {
        super(klass, NO_RUNNERS);

        manager = ReactorManager.getInstance();
        manager.setAnnotationHandler(new JUnitLegacyAnnotationHandler());

        try {
            manager.prepareReactor(klass, null);
            stagedReactor = manager.stageReactor();

            Parameters parameters = getParametersMethod().getAnnotation(Parameters.class);
            createRunnersForParameters(allParameters(), parameters.name());
        }
        catch (InstantiationException exc) {
            throw new InitializationError(exc);
        }
        catch (IllegalAccessException exc) {
            throw new InitializationError(exc);
        }
        catch (InvocationTargetException exc) {
            throw new InitializationError(exc);
        }
        catch (IOException exc) {
            throw new InitializationError(exc);
        }
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }

    @Override
    public void run(RunNotifier notifier) {
        Class<?> testClass = getTestClass().getJavaClass();
        try {
            manager.beforeClass(stagedReactor, testClass);
            super.run(notifier);
        }
        // CHECKSTYLE:SKIP : catch all wanted
        catch (Throwable e) {
            // rethrowing the exception does not help, we have to use the notifier here
            Description description = Description.createSuiteDescription(testClass);
            notifier.fireTestFailure(new Failure(description, e));
        }
        finally {
            manager.afterClass(stagedReactor, testClass);
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
}
