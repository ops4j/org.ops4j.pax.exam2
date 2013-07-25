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

import java.lang.reflect.Method;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.BundleContext;

/**
 * A ProbeInvoker which delegates the test method invocation to JUnit.
 * <p>
 * By doing so, JUnit can handle {@code @Before}, {@code @After} and {@code @Rule} annotations in
 * the usual way.
 * <p>
 * The test method to be executed is defined by an encoded instruction from
 * {@code org.ops4j.pax.exam.spi.intern.DefaultTestAddress}.
 * 
 * @author Harald Wellmann
 * @since 2.3.0, August 2011
 */
public class JUnitProbeInvoker implements ProbeInvoker {

    private BundleContext ctx;
    private String clazz;
    private String method;

    private Injector injector;

    public JUnitProbeInvoker(String encodedInstruction, BundleContext bundleContext) {
        // parse class and method out of expression:
        String[] parts = encodedInstruction.split(";");
        clazz = parts[0];
        method = parts[1];
        ctx = bundleContext;
        injector = ServiceLookup.getService(ctx, Injector.class);
    }

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

    private boolean findAndInvoke(Class<?> testClass, Object...args) {
        Integer index = null;
        try {
            /*
             * If args are present, we expect exactly one integer argument, defining the index of
             * the parameter set for a parameterized test.
             */
            if (args.length > 0) {
                if (!(args[0] instanceof Integer)) {
                    throw new TestContainerException("Integer argument expected");
                }
                index = (Integer) args[0];
            }

            // find matching method
            for (Method m : testClass.getMethods()) {
                if (m.getName().equals(method)) {
                    // we assume its correct:
                    invokeViaJUnit(testClass, m, index);
                    return true;
                }
            }
        }
        catch (NoClassDefFoundError e) {
            throw new TestContainerException(e);
        }
        return false;
    }

    /**
     * Invokes a given method of a given test class via {@link JUnitCore} and injects dependencies
     * into the instantiated test class.
     * <p>
     * This requires building a {@code Request} which is aware of an {@code Injector} and a
     * {@code BundleContext}.
     * 
     * @param testClass
     * @param testMethod
     * @throws TestContainerException
     */
    private void invokeViaJUnit(final Class<?> testClass, final Method testMethod, Integer index) {
        Request classRequest = new ContainerTestRunnerClassRequest(testClass, injector, index);
        Description methodDescription = Description.createTestDescription(testClass, method);
        Request request = classRequest.filterWith(methodDescription);
        JUnitCore junit = new JUnitCore();
        Result result = junit.run(request);
        List<Failure> failures = result.getFailures();
        if (!failures.isEmpty()) {
            throw new TestContainerException(failures.toString(), failures.get(0).getException());
        }
    }
}
