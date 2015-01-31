/*
 * Copyright 2014 Harald Wellmann.
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
package org.ops4j.pax.exam.testng.perclass;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import javax.inject.Inject;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.sample8.ds.Calculator;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({ PaxExam.class })
public class CalculatorTest {

    @Inject
    private Calculator calculator;


    @Configuration
    public Option[] config() {
        return options(

            mavenBundle("org.ops4j.pax.exam.samples", "pax-exam-sample8-ds", "4.2.0"),
            mavenBundle("org.apache.felix", "org.apache.felix.scr", "1.6.2"),
            mavenBundle("org.testng", "testng", "6.8.17"),
            mavenBundle("com.beust", "jcommander", "1.27"),
            bundle("mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.hamcrest.core/1.3.0.1"),
            bundle("mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.junit/4.12.0.1"),
            systemProperty("osgi.console").value("6666"));
    }

    @Test
    public void getCalculatorService() {
        assertNotNull(calculator);
        assertEquals(calculator.add(5, 7), 12);
    }
}
