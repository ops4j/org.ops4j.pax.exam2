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
import static org.ops4j.pax.swissbox.core.ContextClassLoaderUtils.doWithClassLoader;

import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

import org.apache.xbean.osgi.bundle.util.DelegatingBundle;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.invoker.junit5.ProbeRunListener;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.swissbox.core.BundleClassLoader;
import org.ops4j.pax.swissbox.core.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 *
 * @author Harald Wellmann
 */
public class JUnit5ProbeInvoker implements ProbeInvoker {

    private BundleContext ctx;

    public JUnit5ProbeInvoker(BundleContext bundleContext, Injector injector) {
        this.ctx = bundleContext;
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) {
        try {
            runTestWithContextClassLoader(description, listener);
        }
        catch (Exception exc) {
            throw new TestContainerException(exc);
        }
    }

    private void runTestWithContextClassLoader(TestDescription description, TestListener listener)
        throws Exception {
        ClassLoader ccl = buildContextClassLoader();
        doWithClassLoader(ccl, () -> runTestWithJUnit5(description, listener));
    }

    /**
     * @return
     */
    private ClassLoader buildContextClassLoader() {
        Bundle junit5Bundle = BundleUtils.getBundle(ctx, "pax-exam-junit5-bundle");
        Bundle junit5InvokerBundle = BundleUtils.getBundle(ctx,
            "org.ops4j.pax.exam.invoker.junit5");
        DelegatingBundle delegatingBundle = new DelegatingBundle(
            Arrays.asList(ctx.getBundle(), junit5Bundle, junit5InvokerBundle));
        return new BundleClassLoader(delegatingBundle);
    }

    private Object runTestWithJUnit5(TestDescription description, TestListener listener) {
        Launcher launcher = LauncherFactory.create();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(toSelector(description))
            .configurationParameter("pax.exam.delegating", "true").build();
        launcher.registerTestExecutionListeners(new ProbeRunListener(listener));
        launcher.execute(request);
        return null;
    }

    /**
     * @param description
     */
    private DiscoverySelector toSelector(TestDescription description) {
        if (description.getMethodName() == null) {
            return DiscoverySelectors.selectClass(description.getClassName());
        }
        else {
            return DiscoverySelectors.selectMethod(description.getClassName(),
                description.getMethodName());
        }
    }

    @Override
    public void runTestClass(String description) {
        try (Socket socket = new Socket(InetAddress.getLocalHost(), getPort())) {
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            OutputStreamTestListener streamListener = new OutputStreamTestListener(oos);
            runTestWithContextClassLoader(TestDescription.parse(description), streamListener);
        }
        catch (Exception exc) {
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
