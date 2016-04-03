/*
 * Copyright 2012 Harald Wellmann
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
package org.ops4j.pax.exam.testng.servlet.invoker;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;

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
 * @since 3.0.0, Jan 2011
 */
public class ServletBridgeProbeInvoker implements ProbeInvoker {

    private String clazz;
    private String method;
    private WebTarget testRunner;

    public ServletBridgeProbeInvoker(String encodedInstruction) {
        try {
            // parse class and method out of expression:
            String[] parts = encodedInstruction.split(";");
            if (parts.length != 3) {
                throw new TestContainerException("invalid test instruction: " + encodedInstruction);
            }
            clazz = parts[0];
            method = parts[1];
            URI contextRoot = new URI(parts[2]);
            this.testRunner = getTestRunner(contextRoot);
        }
        catch (URISyntaxException exc) {
            throw new TestContainerException(exc);
        }
    }

    @Override
    public void call(Object... args) {
        Class<?> testClass;
        try {
            testClass = getClass().getClassLoader().loadClass(clazz);
        }
        catch (ClassNotFoundException e) {
            throw new TestContainerException(e);
        }

        if (!(findAndInvoke(testClass))) {
            throw new TestContainerException(" Test " + method + " not found in test class "
                + testClass.getName());
        }
    }

    private boolean findAndInvoke(Class<?> testClass) {
        try {
            // find matching method
            for (Method m : testClass.getMethods()) {
                if (m.getName().equals(method)) {
                    // we assume its correct:
                    invokeViaJUnit(testClass, m);
                    return true;
                }
            }
        }
        catch (NoClassDefFoundError e) {
            throw new TestContainerException(e);
        }
        catch (IOException e) {
            throw new TestContainerException(e);
        }
        catch (ClassNotFoundException e) {
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
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void invokeViaJUnit(final Class<?> testClass, final Method testMethod)
        throws IOException, ClassNotFoundException {
        InputStream is = testRunner.queryParam("class", testClass.getName())
            .queryParam("method", testMethod.getName()).request().get(InputStream.class);

        ObjectInputStream ois = new ObjectInputStream(is);
        Object object = ois.readObject();
        if (object instanceof Throwable) {
            Throwable t = (Throwable) object;
            throw new TestContainerException(t);
        }
        else if (object instanceof String) {
            // ok
        }
        else {
            throw new IllegalStateException();
        }
    }

    private WebTarget getTestRunner(URI contextRoot) {
        URI uri = contextRoot.resolve("testrunner");
        Client client = ClientBuilder.newClient();
        return client.target(uri);
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) {
        // TODO Auto-generated method stub

    }

}
