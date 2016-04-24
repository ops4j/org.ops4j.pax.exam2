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
package org.ops4j.pax.exam.jaxrs2.invoker;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.spi.listener.TestListenerTask;


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
public class JaxRs2ProbeInvoker implements ProbeInvoker {

    private WebTarget testRunner;

    public JaxRs2ProbeInvoker(String encodedInstruction) {
        try {
            // parse class and method out of expression:
            String[] parts = encodedInstruction.split(";");
            if (parts.length != 3) {
                throw new TestContainerException("invalid test instruction: " + encodedInstruction);
            }
            URI contextRoot = new URI(parts[2]);
            this.testRunner = getTestRunner(contextRoot);
        }
        catch (URISyntaxException exc) {
            throw new TestContainerException(exc);
        }
    }

    private WebTarget getTestRunner(URI contextRoot) {
        URI uri = contextRoot.resolve("testrunner");
        Client client = ClientBuilder.newClient();
        return client.target(uri);
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) {
        WebTarget target = testRunner.queryParam("class", description.getClassName());
        if (description.getMethodName() != null) {
            target = target.queryParam("method", description.getMethodName());
        }
        if (description.getIndex() != null) {
            target = target.queryParam("index", description.getIndex());
        }
        Future<InputStream> is = target.request().async().get(InputStream.class);
        TestListenerTask task = new TestListenerTask(is, listener);
        task.run();
    }

    @Override
    public void runTestClass(String description) {
        // TODO Auto-generated method stub

    }
}
