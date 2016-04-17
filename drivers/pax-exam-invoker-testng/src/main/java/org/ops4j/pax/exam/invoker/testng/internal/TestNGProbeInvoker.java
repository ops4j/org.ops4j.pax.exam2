/*
 * Copyright 2016 Harald Wellmann
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
package org.ops4j.pax.exam.invoker.testng.internal;

import static org.ops4j.pax.exam.Constants.EXAM_INVOKER_PORT;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.WrappedTestContainerException;
import org.ops4j.pax.exam.util.Exceptions;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.swissbox.core.ContextClassLoaderUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

public class TestNGProbeInvoker implements ProbeInvoker {

    private BundleContext ctx;
    private String clazz;
    private String method;
    private Injector injector;

    private Class<?> testClass;

    public TestNGProbeInvoker(String encodedInstruction, BundleContext bundleContext,
        Injector injector) {
        // parse class and method out of expression:
        String[] parts = encodedInstruction.split(";");
        clazz = parts[0];
        method = parts[1];
        ctx = bundleContext;
        this.injector = injector;
        this.testClass = loadClass(clazz);
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
                    invokeViaTestNG(m, index);
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
    private void invokeViaTestNG(final Method testMethod, Integer index) {

        TestNG testNG = new TestNG();
        testNG.setUseDefaultListeners(false);
        ContainerResultListener listener = new ContainerResultListener(null);
        XmlSuite suite = new XmlSuite();
        suite.setName("PaxExamInternal");
        XmlTest xmlTest = new XmlTest(suite);
        XmlClass xmlClass = new XmlClass(clazz);
        xmlTest.getClasses().add(xmlClass);
        XmlInclude xmlInclude = new XmlInclude(testMethod.getName());
        xmlClass.getIncludedMethods().add(xmlInclude);

        testNG.setXmlSuites(Arrays.asList(suite));
        testNG.run();

//        ObjectOutputStream oos = new ObjectOutputStream(os);
//        for (ITestResult result : listener.getFailedTests()) {
//            Exception exc = new WrappedTestContainerException(result.getThrowable());
//            oos.writeObject(exc);
//        }
//        if (listener.getFailedTests().isEmpty()) {
//            oos.writeObject("ok");
//        }

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
    public void runTest(final TestDescription description, final TestListener listener) {
        ClassLoader classLoader = ctx.getBundle().adapt(BundleWiring.class).getClassLoader();
        try {
            ContextClassLoaderUtils.doWithClassLoader(classLoader, new Callable<Void>() {

                @Override
                public Void call() {
                    runTestWithTestNG(description, listener);
                    return null;
                }
            });
        }
        // CHECKSTYLE:SKIP - doWithClassLoader API
        catch (Exception exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    private void runTestWithTestNG(TestDescription description, TestListener listener) {
        TestNG testNG = new TestNG();
        testNG.setVerbose(0);
        testNG.setUseDefaultListeners(false);
        ContainerResultListener resultListener = new ContainerResultListener(listener);
        testNG.addListener(resultListener);

        XmlSuite suite = new XmlSuite();
        suite.setName("PaxExamInternal");
        XmlTest xmlTest = new XmlTest(suite);
        XmlClass xmlClass = new XmlClass(clazz);
        xmlTest.getClasses().add(xmlClass);
        if (description.getMethodName() != null) {
            XmlInclude xmlInclude = new XmlInclude(description.getMethodName());
            xmlClass.getIncludedMethods().add(xmlInclude);
        }

        testNG.setXmlSuites(Collections.singletonList(suite));
        testNG.run();
    }



    @Override
    public void runTestClass(String description) {
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
