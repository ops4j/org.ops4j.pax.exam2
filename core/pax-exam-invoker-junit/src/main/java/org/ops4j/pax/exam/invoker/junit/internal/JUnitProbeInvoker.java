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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestFilter;
import org.ops4j.pax.exam.TestListener;
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
    private Injector injector;

    private Class<?> testClass;

    public JUnitProbeInvoker(BundleContext bundleContext, Injector injector) {
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
            TestFilter customFilter = description.getFilter();
            if (customFilter != null) {
                runner.filter(new Filter() {
                    @Override
                    public boolean shouldRun(Description description) {
                        return customFilter.getUniqueIds().contains(String.valueOf(description.hashCode()));
                    }
                    @Override
                    public String describe() {
                        return customFilter.getDescription();
                    }
                });
            }

            JUnitCore junit = new JUnitCore();
            junit.addListener(new ProbeRunListener(listener));
            junit.run(runner);
        }
        catch (InitializationError | NoTestsRemainException exc) {
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
