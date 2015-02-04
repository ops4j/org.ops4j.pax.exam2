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
package org.ops4j.pax.exam.regression.maven;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.junit.Test;


public class WabSampleIT {

    @Test
    public void checkPlainTextFromWabServlet() throws InterruptedException {
        Client client = ClientBuilder.newClient();
        WebTarget resource = client.target("http://localhost:8181/wab/WABServlet");
        
        // The server is started by the exam-maven-plugin in a background process:
        // We need to make sure it has finished startup.
        
        int i = 0;
        int delay = 1000;
        int maxRetries = 5;

        String response = null;
        
        while (i < maxRetries) {
            i++;

            try {
                response = resource.request().get(String.class);
                break;
            }
            catch (ProcessingException exc) {                
                Thread.sleep(delay);
            }
        }
        
        
        assertThat(response, containsString("wab symbolic name : org.ops4j.pax.exam.samples.pax-exam-sample11-wab"));
    }
}
