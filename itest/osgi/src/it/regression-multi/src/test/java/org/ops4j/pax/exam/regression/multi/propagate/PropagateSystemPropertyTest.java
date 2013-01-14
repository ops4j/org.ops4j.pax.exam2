/*
 * Copyright 2013 Harald Wellmann
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
package org.ops4j.pax.exam.regression.multi.propagate;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.propagateSystemProperties;
import static org.ops4j.pax.exam.CoreOptions.propagateSystemProperty;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.isNativeContainer;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class PropagateSystemPropertyTest {

    @Configuration
    public Option[] config() {
        System.setProperty("prop1", "value1");
        System.setProperty("prop2", "");
        System.setProperty("prop4", "value4");
        System.setProperty("prop5", "value5");
        
        return options(regressionDefaults(), //
            systemProperty("prop5").value("override"), //
            propagateSystemProperty("prop1"), // 
            propagateSystemProperties("prop2", "prop3", "prop5"), //
            junitBundles());
    }

    @Before
    public void setUp() {
        assumeTrue(!isNativeContainer());
    }

    @Test
    public void nonEmptyPropertyIsPropagated() {
        assertThat(System.getProperty("prop1"), is("value1"));
    }

    @Test
    public void emptyPropertyIsPropagated() {
        assertThat(System.getProperty("prop2"), is(""));
    }

    @Test
    public void unsetPropertyIsPropagatedAsNull() {
        assertThat(System.getProperty("prop3"), is(nullValue()));
    }

    @Test
    public void propertySetButNotPropagatedIsNull() {
        assertThat(System.getProperty("prop4"), is(nullValue()));
    }

    @Test
    public void systemPropertyOptionOverridesPropagation() {
        assertThat(System.getProperty("prop5"), is("override"));
    }
}
