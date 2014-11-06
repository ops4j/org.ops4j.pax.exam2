/*
 * Copyright 2012 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.testng.perclass;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.propagateSystemProperty;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import javax.inject.Inject;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.sample9.pde.HelloService;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.ops4j.pax.exam.util.PathUtils;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(PaxExam.class)
public class GreeterTest {

    @Inject
    private HelloService helloService;

    @Configuration
    public Option[] createConfiguration() {
        return options(
            propagateSystemProperty("pax.exam.regression.rmi"),
            mavenBundle("org.testng", "testng", "6.8.8"),
            mavenBundle("com.beust", "jcommander", "1.27"),
            bundle("mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.hamcrest.core/1.3.0.1"),
            bundle("mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.junit/4.11.0.1"),
            url("reference:file:" + PathUtils.getBaseDir() + "/target/pax-exam-sample9-pde/"),

            systemProperty("osgi.console").value("6666"));
    }

    @Test
    public void getInjectedService() {
        assertNotNull(helloService);
        assertEquals(helloService.getMessage(), "Hello Pax!");
    }
}
