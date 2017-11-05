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
package org.ops4j.pax.exam.invoker.junit5.internal;

import static org.ops4j.pax.exam.Constants.EXAM_INVOKER_PORT;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.main.LauncherFactory;
import org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.invoker.junit5.ProbeRunListener;
import org.ops4j.pax.exam.util.Injector;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Harald Wellmann
 */
public class JUnit5ProbeInvoker implements ProbeInvoker {

    private BundleContext ctx;
    private Injector injector;

    private Class<?> testClass;

    public JUnit5ProbeInvoker(BundleContext bundleContext, Injector injector) {
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
    public void runTest(TestDescription description, TestListener listener) {
        runTestWithJUnit(description, listener);
    }

    private void runTestWithJUnit(TestDescription description, TestListener listener) {
        Launcher launcher = LauncherFactory.create();
        TestDiscoveryRequest request = TestDiscoveryRequestBuilder.request()
            .select(ClassSelector.forClass(loadClass(description.getClassName()))).build();
        launcher.registerTestExecutionListeners(new ProbeRunListener(listener));
        launcher.execute(request);
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
