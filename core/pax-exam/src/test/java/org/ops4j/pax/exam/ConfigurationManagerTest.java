/*
 * Copyright 2013 Harald Wellmann.
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
package org.ops4j.pax.exam;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;


public class ConfigurationManagerTest {
    
    @Before
    public void before() {
        System.clearProperty("exam.test.properties");
        System.clearProperty("exam.test.key1");
        System.clearProperty("exam.test.key2");
    }
    
    @Test
    public void loadSystemProperties() {
        ConfigurationManager cm = new ConfigurationManager();
        assertThat(System.getProperty("exam.test.key1"), is(nullValue()));
        assertThat(System.getProperty("exam.test.key2"), is(nullValue()));
        String cp = System.getProperty("java.class.path");
        
        cm.loadSystemProperties("exam.test.props");
        
        assertThat(System.getProperty("exam.test.key1"), is("value1"));
        assertThat(System.getProperty("exam.test.key2"), is("value2"));
        assertThat(System.getProperty("java.class.path"), is(cp));
    }

    @Test
    public void loadSystemPropertiesOverwrite() {
        ConfigurationManager cm = new ConfigurationManager();
        System.setProperty("exam.test.key1", "old1");
        System.setProperty("exam.test.key2", "old2");
        
        cm.loadSystemProperties("exam.test.props");
        
        assertThat(System.getProperty("exam.test.key1"), is("value1"));
        assertThat(System.getProperty("exam.test.key2"), is("value2"));
    }

    @Test
    public void loadSystemPropertiesFromEnvironment() {
        Assume.assumeThat(System.getenv("EXAM_TEST_PROPS"), is("props/test.properties"));
        
        ConfigurationManager cm = new ConfigurationManager();
        System.setProperty("exam.test.key1", "old1");
        System.setProperty("exam.test.key2", "old2");
        
        cm.loadSystemProperties("exam.test.props.env");
        
        assertThat(System.getProperty("exam.test.key1"), is("value1"));
        assertThat(System.getProperty("exam.test.key2"), is("value2"));
    }
}
