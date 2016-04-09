/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.raw.extender.intern;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.BundleContext;

/**
 * Turns a instruction into a service call. Currently used with encoded instructions from
 * org.ops4j.pax.exam.spi.container.ClassMethodTestAddress
 *
 * @author Toni Menzel
 * @since Dec 4, 2009
 */
public class ProbeInvokerImpl implements ProbeInvoker {

    private BundleContext ctx;
    private String clazz;
    private String method;

    private Injector injector;

    public ProbeInvokerImpl(String encodedInstruction, BundleContext bundleContext) {
        // parse class and method out of expression:
        String[] parts = encodedInstruction.split(";");
        clazz = parts[0];
        method = parts[1];
        ctx = bundleContext;

        // acquire (optional) injector
        // TODO replace system property by core configuration option
        // boolean inject = "true".equals(System.getProperty( "pax.exam.inject" ));
        boolean inject = true;
        if (inject) {
            injector = ServiceLookup.getService(ctx, Injector.class);
        }
        else {
            injector = new NoOpInjector();
        }
    }

    @Override
    public void call(Object... args) {
        Class<?> testClass;
        try {
            testClass = ctx.getBundle().loadClass(clazz);
        }
        catch (ClassNotFoundException e) {
            throw new TestContainerException(e);
        }

        if (!(findAndInvoke(testClass, args))) {
            throw new TestContainerException(" Test " + method + " not found in test class "
                + testClass.getName());
        }
    }

    private boolean findAndInvoke(Class<?> testClass, Object... params) {
        try {
            // find matching method
            for (Method m : testClass.getMethods()) {
                if (m.getName().equals(method)) {
                    // we assume its correct:
                    injectContextAndInvoke(testClass.newInstance(), m, params);
                    return true;
                }
            }
        }
        catch (NoClassDefFoundError e) {
            throw new TestContainerException(e);
        }
        catch (InstantiationException e) {
            throw new TestContainerException(e);
        }
        catch (IllegalAccessException e) {
            throw new TestContainerException(e);
        }
        return false;
    }

    /**
     * Invokes the bundle context (if possible and required) and executes the regression method.
     *
     * TODO this is a trimmed down minimal version that does not support any junit before/afters or
     * self made injection. The only thing you get here is a parameter injection for BundleContext
     * types.
     *
     * @param testInstance
     *            an instance of the regression class
     * @param testMethod
     *            regression method
     * @param params
     *
     * @throws TestContainerException
     *             - Re-thrown from reflection invokation
     */
    private void injectContextAndInvoke(final Object testInstance, final Method testMethod,
        Object[] params) {
        final Class<?>[] paramTypes = testMethod.getParameterTypes();
        injector.injectFields(testInstance);
        try {
            // runBefores( testInstance );
            if (paramTypes.length == 0) {
                testMethod.invoke(testInstance);
            }
            else {
                Object[] parameters = injectHook(testMethod, params);
                testMethod.invoke(testInstance, parameters);
            }
        }
        catch (InvocationTargetException e) {
            throw new TestContainerException(e);

        }
        catch (IllegalAccessException e) {
            throw new TestContainerException(e);
        }
    }

    /**
     * This method practcally makes sure the method that is going to be invoked has the right types
     * and instances injected as parameters.
     *
     * The following rules apply: You either have no arguments. You have arguments, then
     * BundleContext must be your first. Parameters with @Inject Annotation must come next. All
     * remaining arguments are set the params values.
     *
     * @param testMethod
     *            method in question
     * @param params
     *            derived from caller. Addditional injections may apply
     *
     * @return filled parameters ready for method invokation
     */
    private Object[] injectHook(Method testMethod, Object[] params) {
        // skip all injections
        Class<?>[] paramTypes = testMethod.getParameterTypes();
        Object[] ret = new Object[paramTypes.length];
        Annotation[][] paramAnnotations = testMethod.getParameterAnnotations();
        int paramCursor = 0;

        for (int i = 0; i < ret.length; i++) {
            if (i == 0) {
                ret[0] = ctx;
            }
            else {
                if (paramAnnotations[i].length > 0) {
                    // skip
                    throw new RuntimeException("Parameter " + i + " on " + testMethod.getName()
                        + " has Annotation. Not supported until Pax Exam 2.1");
                }
                else {
                    if (params.length > paramCursor) {
                        ret[i] = params[paramCursor++];
                    }
                    else {
                        // set default to null
                        ret[i] = null;
                    }
                }
            }
        }

        return ret;
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) {
        // TODO Auto-generated method stub
    }

    @Override
    public void runTestClass(String description) {
        // TODO Auto-generated method stub
    }
}
