/*
 * Copyright 2013 Harald Wellmann
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

package org.ops4j.pax.exam.regression.karaf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExamParameterized;
import org.ops4j.pax.exam.sample8.ds.Calculator;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

@RunWith(PaxExamParameterized.class)
@ExamReactorStrategy(PerMethod.class)
public class ParameterizedConfigurationTest {

	private static final String SYSTEM_PROPERTY = "pax.parameterized.value";
    private static int paramIndex = 1;

    @Inject
    private Calculator calculator;

    private final int a;
    private final int b;
    private final int sum;
    private final String sp;


    @Parameters
    public static List<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
            {2, 3, 5, "a"},
            {5, 6, 11, "ab"},
            {6, 2, 8, "abc"}
        });
    }

    @Configuration
    public Option[] config() {
        return new Option[] {
            RegressionConfiguration.regressionDefaults(),
            mavenBundle("org.apache.felix", "org.apache.felix.scr", "1.6.2"),
            mavenBundle("org.ops4j.pax.exam.samples", "pax-exam-sample8-ds", Info.getPaxExamVersion()),
            systemProperty(SYSTEM_PROPERTY).value(this.sp)};
    }

    public ParameterizedConfigurationTest(int a, int b, int sum, String systemProperty) {
        this.a = a;
        this.b = b;
        this.sum = sum;
        this.sp = systemProperty;
    }

    @Test
    public void add() {
        assertThat(this.calculator.add(this.a, this.b), is(this.sum));

        // Each test runs in its own container (isolation).
        // So, even if we update a static field, other parameterized class instances
        // should not be impacted.
        assertThat(paramIndex, is(1));
        paramIndex++;

        // The system property (set in the PAX configuration) must match the internal field.
        String currentProperty = System.getProperty(SYSTEM_PROPERTY);
        assertThat(currentProperty, notNullValue());
        assertThat(currentProperty, is(this.sp));
    }
}
