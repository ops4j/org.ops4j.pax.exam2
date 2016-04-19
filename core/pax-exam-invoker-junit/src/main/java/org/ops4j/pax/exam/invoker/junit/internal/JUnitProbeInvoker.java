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

import static org.ops4j.pax.exam.Constants.EXAM_INVOKER_PORT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.WrappedTestContainerException;
import org.ops4j.pax.exam.util.Exceptions;
import org.ops4j.pax.exam.util.Injector;
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
    private String method;
    private Injector injector;

    private Class<?> testClass;

    public JUnitProbeInvoker(String encodedInstruction, BundleContext bundleContext,
        Injector injector) {
        this.ctx = bundleContext;
        this.injector = injector;
    }

    private Class<?> loadClass(String className) {
        try {
            return ctx.getBundle().loadClass(className);
        }
        catch (ClassNotFoundException e) {
            throw new TestContainerException(e);
        }
    }

    @Override
    public void call(Object... args) {
        if (!(findAndInvoke(args))) {
            throw new TestContainerException(
                " Test " + method + " not found in test class " + testClass.getName());
        }
    }

    private boolean findAndInvoke(Object... args) {
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
                    invokeViaJUnit(m, index);
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
    private void invokeViaJUnit(final Method testMethod, Integer index) {
        try {
            ParentRunner<?> runner = createRunner(index);
            Description methodDescription = Description.createTestDescription(testClass, method);
            runner.filter(Filter.matchMethodDescription(methodDescription));
            JUnitCore junit = new JUnitCore();
            Result result = junit.run(runner);
            List<Failure> failures = result.getFailures();
            if (!failures.isEmpty()) {
                throw createTestContainerException(failures.toString(),
                    failures.get(0).getException());
            }
        }
        catch (Exception exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    private ParentRunner<?> createRunner(Integer index) throws InitializationError {
        if (index == null) {
            return new ContainerTestRunner(testClass, injector);
        }
        else {
            return new ParameterizedContainerTestRunner(testClass, injector, index);
        }
    }

    private ParentRunner<?> createRunner(TestDescription description) throws InitializationError {
        testClass = loadClass(description.getClassName());
        Integer index = description.getIndex();
        if (index == null) {
            return new ContainerTestRunner(testClass, injector);
        }
        else {
            ParameterizedSuite runner = new ParameterizedSuite(testClass, injector);
            return runner;
        }
    }

    /**
     * Creates exception for test failure and makes sure it is serializable.
     *
     * @param message
     * @param ex
     * @return serializable exception
     */
    private TestContainerException createTestContainerException(String message, Throwable ex) {
        return isSerializable(ex) ? new TestContainerException(message, ex)
            : new WrappedTestContainerException(message, ex);
    }

    /**
     * Check if given exception is serializable by doing a serialization and checking the exception
     *
     * @param ex
     *            exception to check
     * @return if the given exception is serializable
     */
    private boolean isSerializable(Throwable ex) {
        try {
            new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(ex);
            return true;
        }
        // CHECKSTYLE:SKIP
        catch (Throwable ex2) {
            return false;
        }
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) {
        runTestWithJUnit(description, listener);
    }

    private void runTestWithJUnit(TestDescription description, TestListener listener) {
        try {
            ParentRunner<?> runner = createRunner(description);
            if (description.getMethodName() != null) {
                Description methodName = Description
                    .createTestDescription(description.getClassName(), description.getMethodName());
                runner.filter(Filter.matchMethodDescription(methodName));
            }

            JUnitCore junit = new JUnitCore();
            junit.addListener(new ProbeRunListener(listener));
            junit.run(runner);
        }
        catch (Exception exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    @Override
    public void runTestClass(String description) {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), getPort())) {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            OutputStreamTestListener streamListener = new OutputStreamTestListener(oos);
            runTestWithJUnit(TestDescription.parse(description), streamListener);
        }
        catch (IOException exc) {
            new TestContainerException(exc);
        }
    }

    private int getPort() {
        String port = ctx.getProperty(EXAM_INVOKER_PORT);
        if (port == null) {
            throw new TestContainerException(
                "System property " + EXAM_INVOKER_PORT + " is not set");
        }
        try {
            return Integer.parseInt(port);
        }
        catch (NumberFormatException exc) {
            throw new TestContainerException(
                "Cannot parse value of system property " + EXAM_INVOKER_PORT, exc);
        }
    }
}
