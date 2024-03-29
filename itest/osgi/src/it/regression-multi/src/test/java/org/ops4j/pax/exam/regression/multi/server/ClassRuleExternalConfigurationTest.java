/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.regression.multi.server;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.isKnopflerfish;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.pax.exam.junit.PaxExamServer;

@Ignore // outdated
public class ClassRuleExternalConfigurationTest {

    @ClassRule
    public static PaxExamServer exam = new PaxExamServer(WabSampleConfiguration.class);
    
    private String url;

    @Before
    public void waitForShutdown() throws InterruptedException {
        assumeTrue(!isKnopflerfish());
        Thread.sleep(3000);
        String port = System.getProperty("pax.exam.itest.http.port", "18181");
        url = String.format("http://localhost:%s/wab/WABServlet", port);
    }

    @Test
    public void checkWabSymbolicName() {
        Client client = ClientBuilder.newClient();
        WebTarget resource = client.target(url);
        String response = resource.request().get(String.class);
        assertThat(response, containsString("wab symbolic name : org.ops4j.pax.exam.samples.pax-exam-sample11-wab"));
    }

    @Test
    public void checkWabVersion() {
        Client client = ClientBuilder.newClient();
        WebTarget resource = client.target(url);
        String response = resource.request().get(String.class);
        assertThat(response, containsString("wab version :"));
        // TODO check version
    }
}
