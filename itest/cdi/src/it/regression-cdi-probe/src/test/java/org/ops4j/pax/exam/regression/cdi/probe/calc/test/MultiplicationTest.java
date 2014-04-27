/*
 * Copyright 2013 Harald Wellmann
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

package org.ops4j.pax.exam.regression.cdi.probe.calc.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.jarProbe;
import static org.ops4j.pax.exam.CoreOptions.options;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.regression.cdi.probe.calc.BinaryOperation;
import org.ops4j.pax.exam.regression.cdi.probe.calc.Calculator;
import org.ops4j.pax.exam.regression.cdi.probe.calc.CalculatorImpl;
import org.ops4j.pax.exam.regression.cdi.probe.calc.Multiplication;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class MultiplicationTest {
    
    @Inject
    private Calculator calculator;
    
    @Configuration
    public Option[] config() {
        return options(jarProbe()
            .classes(Multiplication.class, BinaryOperation.class, Calculator.class, CalculatorImpl.class)
            .metaInfResource("src/test/resources/calc/beans.xml")
            );
    }
    
    @Test
    public void add() {
        assertThat(calculator.operate(2, 3), is(6));
    }

}
