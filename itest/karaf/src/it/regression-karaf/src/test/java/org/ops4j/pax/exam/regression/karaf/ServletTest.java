/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.exam.regression.karaf;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.regression.karaf.RegressionConfiguration.featureRepoStandard;
import static org.ops4j.pax.exam.regression.karaf.RegressionConfiguration.regressionDefaults;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;
import static org.ops4j.pax.tinybundles.core.TinyBundles.withBnd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.inject.Inject;

import org.apache.karaf.features.BootFinished;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.regression.karaf.servlet.EchoServlet;
import org.ops4j.pax.exam.regression.karaf.servlet.ServletActivator;
import org.ops4j.pax.web.service.spi.ServletEvent;
import org.ops4j.pax.web.service.spi.ServletListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

@RunWith(PaxExam.class)
public class ServletTest {
    @Inject
    protected BundleContext bundleContext;

    /**
     * To make sure the tests run only when the boot features are fully installed
     */
    @Inject
    BootFinished bootFinished;

    @Configuration
    public Option[] config() {
        return new Option[]{
            regressionDefaults("target/paxexam/unpack2/"),
            features(featureRepoStandard(), "http"),
            // set the system property for pax web
            editConfigurationFilePut("etc/org.ops4j.pax.web.cfg", "org.osgi.service.http.port",
                RegressionConfiguration.HTTP_PORT),
            streamBundle(createTestBundle())

        };
    }

    @Before
    public void registerListener() {
        ServletListener webListener = new ServletListener() {
            @Override
            public void servletEvent(ServletEvent event) {
                System.out.println(event);
                if (event.getType() == ServletEvent.DEPLOYED && EchoServlet.ALIAS.equals(event.getAlias())) {
                    notifyMe();
                }
            }
        };
        Dictionary<String, ?> properties = new Hashtable<String, String>();
        bundleContext.registerService(ServletListener.class, webListener, properties);
    }

    private void notifyMe() {
        synchronized (this) {
            this.notify();
        }
    }

    private void waitForServlet() throws InterruptedException {
        synchronized (this) {
            wait(1000);
        }
    }

    @Test
    public void testService() throws Exception {
        waitForServlet();
        System.out.println("Trying to get url");
        URL url = new URL("http://localhost:" + RegressionConfiguration.HTTP_PORT + "/test/services");
        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);
        writeRequest(conn, "This is a test");
        String line = readResponse(conn);
        assertEquals("Got a wrong response", "This is a test", line);
    }

    private void writeRequest(URLConnection conn, String requestMessage) throws IOException {
        try (OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream())) {
            wr.write(requestMessage);
        }
    }

    private String readResponse(URLConnection conn) throws IOException {
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            return rd.readLine();
        }
    }

    private InputStream createTestBundle() {
        return bundle()
            .add(EchoServlet.class)
            .add(ServletActivator.class)
            .set(Constants.BUNDLE_ACTIVATOR, ServletActivator.class.getName())
            .set(Constants.BUNDLE_MANIFESTVERSION, "2")
            .build(withBnd());
    }
}
