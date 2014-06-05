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
package org.ops4j.pax.exam.regression.multi.ds;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.sample8.ds.Calculator;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;

/**
 * Test case for PAXEXAM-406. Calculator is a Declarative Services components which has an injected
 * dependency on another DS Component Addition.
 * <p>
 * Pax Exam injection is based on {@link ServiceLookup} which failed to keep a reference to the
 * given service after returning in Pax Exam 2.4.0 with Pax Swissbox 1.5.0.
 * <p>
 * The current test fails with Pax Swissbox 1.5.0, as the injected service in CalculatorImpl is
 * null. The test passes with Pax Swissbox 1.5.1.
 * 
 * @author Harald Wellmann
 */
@RunWith(PaxExam.class)
public class DeclarativeServicesTest {

    @Inject
    private Calculator calculator;

    @Configuration
    public Option[] config() {
        return options(regressionDefaults(),
            mavenBundle("org.ops4j.pax.exam.samples", "pax-exam-sample8-ds").versionAsInProject(),
            mavenBundle("org.apache.felix", "org.apache.felix.scr", "1.6.2"), junitBundles());
    }

    @Test
    public void getCalculatorService() {
        assertThat(calculator, is(notNullValue()));
        assertThat(calculator.add(5, 7), is(12));
    }
}
