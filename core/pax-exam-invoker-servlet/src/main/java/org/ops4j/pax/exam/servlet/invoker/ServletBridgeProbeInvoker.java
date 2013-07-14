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
package org.ops4j.pax.exam.servlet.invoker;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

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
    private WebResource testRunner;

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

    public void call(Object... args) {
        Class<?> testClass;
        try {
            testClass = getClass().getClassLoader().loadClass(clazz);
        }
        catch (ClassNotFoundException e) {
            throw new TestContainerException(e);
        }

        if (!(findAndInvoke(testClass, args))) {
            throw new TestContainerException(" Test " + method + " not found in test class "
                + testClass.getName());
        }
    }

    private boolean findAndInvoke(Class<?> testClass, Object... args) {
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
                    invokeViaServletBridge(testClass, m, index);
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
     * Invokes a given method of a given test class via the servlet bridge.
     * 
     * @param testClass
     * @param testMethod
     * @param parameterIndex
     *            index of parameter set (counting from 0) for parameterized test, null for plain
     *            tests
     * @throws TestContainerException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void invokeViaServletBridge(final Class<?> testClass, final Method testMethod,
        Integer parameterIndex) throws IOException, ClassNotFoundException {
        WebResource webResource = testRunner.queryParam("class", testClass.getName()) //
            .queryParam("method", testMethod.getName());
        if (parameterIndex != null) {
            webResource = webResource.queryParam("index", Integer.toString(parameterIndex));
        }
        InputStream is = webResource.get(InputStream.class);

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

    private WebResource getTestRunner(URI contextRoot) {
        URI uri = contextRoot.resolve("testrunner");
        Client client = Client.create();
        return client.resource(uri);
    }
}
