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

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.util.Injector;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JUnit {@link Runner} which is aware of an {@link Injector} and a {@link BundleContext} for
 * injecting dependencies from the OSGi service registry.
 * 
 * @author Harald Wellmann
 * 
 */
public class ParameterizedContainerTestRunner extends BlockJUnit4ClassRunner {

    private static final Logger LOG = LoggerFactory
        .getLogger(ParameterizedContainerTestRunner.class);

    private Injector injector;
    private Object[] parameters;

    /**
     * Constructs a runner for the given class which will be injected with dependencies from the
     * given bundle context by the given injector
     * 
     * @param klass
     *            test class to be run
     * @param injector
     *            injector for injecting dependencies
     * @param index
     *            parameter set index (counting from 0).
     * @throws InitializationError when test class cannot be initialized           
     */
    public ParameterizedContainerTestRunner(Class<?> klass, Injector injector, int index)
        throws InitializationError {
        super(klass);
        this.injector = injector;
        try {
            Iterator<Object[]> it = allParameters().iterator();
            for (int i = 0; i <= index; i++) {
                parameters = it.next();
            }
        }
        // CHECKSTYLE:SKIP - JUnit API
        catch (Throwable t) {
            throw new InitializationError(Collections.singletonList(t));
        }
    }

    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = null;
        if (fieldsAreAnnotated()) {
            test = createTestUsingFieldInjection();
        }
        else {
            test = createTestUsingConstructorInjection();
        }
        injector.injectFields(test);
        return test;
    }

    private Object createTestUsingConstructorInjection() throws Exception {
        return getTestClass().getOnlyConstructor().newInstance(parameters);
    }

    private Object createTestUsingFieldInjection() throws Exception {
        List<FrameworkField> annotatedFieldsByParameter = getAnnotatedFieldsByParameter();
        if (annotatedFieldsByParameter.size() != parameters.length) {
            throw new Exception("Wrong number of parameters and @Parameter fields."
                + " @Parameter fields counted: " + annotatedFieldsByParameter.size()
                + ", available parameters: " + parameters.length + ".");
        }
        Object testClassInstance = getTestClass().getJavaClass().newInstance();
        for (FrameworkField each : annotatedFieldsByParameter) {
            Field field = each.getField();
            Parameter annotation = field.getAnnotation(Parameter.class);
            int index = annotation.value();
            try {
                field.set(testClassInstance, parameters[index]);
            }
            catch (IllegalArgumentException iare) {
                throw new Exception(getTestClass().getName() + ": Trying to set " + field.getName()
                    + " with the value " + parameters[index] + " that is not the right type ("
                    + parameters[index].getClass().getSimpleName() + " instead of "
                    + field.getType().getSimpleName() + ").", iare);
            }
        }
        return testClassInstance;
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        LOG.info("running {} in reactor", method.getName());
        super.runChild(method, notifier);
    }

    private List<FrameworkField> getAnnotatedFieldsByParameter() {
        return getTestClass().getAnnotatedFields(Parameter.class);
    }

    private boolean fieldsAreAnnotated() {
        return !getAnnotatedFieldsByParameter().isEmpty();
    }

    @SuppressWarnings("unchecked")
    // CHECKSTYLE:SKIP - JUnit API
    private Iterable<Object[]> allParameters() throws Throwable {
        Object params = getParametersMethod().invokeExplosively(null);
        if (params instanceof Iterable) {
            return (Iterable<Object[]>) params;
        }
        else {
            throw parametersMethodReturnedWrongType();
        }
    }

    private Exception parametersMethodReturnedWrongType() throws Exception {
        String className = getTestClass().getName();
        String methodName = getParametersMethod().getName();
        String message = MessageFormat.format("{0}.{1}() must return an Iterable of arrays.",
            className, methodName);
        return new Exception(message);
    }

    private FrameworkMethod getParametersMethod() throws Exception {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Parameters.class);
        for (FrameworkMethod each : methods) {
            if (each.isStatic() && each.isPublic()) {
                return each;
            }
        }

        throw new Exception("No public static parameters method on class "
            + getTestClass().getName());
    }

}
