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
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;

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
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExamParameterized.class)
@ExamReactorStrategy(PerClass.class)
public class ParameterizedTest {

    private static int paramIndex;
    
    @Inject
    private Calculator calculator;

    private int a;
    private int b;
    private int sum;
    

    @Parameters
    public static List<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
            {2, 3, 5},
            {5, 6, 11},
            {6, 2, 8}
        });
    }

    @Configuration
    public static Option[] config() {
        return new Option[] {
            RegressionConfiguration.regressionDefaults(),
            mavenBundle("org.apache.felix", "org.apache.felix.scr", "1.6.2"),
            mavenBundle("org.ops4j.pax.exam.samples", "pax-exam-sample8-ds", Info.getPaxExamVersion()) };
    }

    public ParameterizedTest(int a, int b, int sum) {
        this.a = a;
        this.b = b;
        this.sum = sum;
    }
    
    @Test
    public void add() {        
        assertThat(calculator.add(a,  b), is(sum));
        
        // ordering of parameter sets must be preserved
        assertThat((Integer) getParameters().get(paramIndex)[0], is(a));
        paramIndex++;
    }
}
