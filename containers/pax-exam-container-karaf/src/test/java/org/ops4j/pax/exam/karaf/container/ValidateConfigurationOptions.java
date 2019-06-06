/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ops4j.pax.exam.karaf.container;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.replaceConfigurationFile;
import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.service.cm.ConfigurationAdmin;

@RunWith(PaxExam.class)
public class ValidateConfigurationOptions extends Karaf4TestContainerITest {

    @Inject
    ConfigurationAdmin configurationAdmin;

    @Override
    @Configuration
    public Option[] config() {
        File source1 = new File("target/test-classes/etc/source1.cfg");
        File source2 = new File("target/test-classes/etc/source2.cfg");
        return new Option[] {
            composite(super.config()),
            // put new
            editConfigurationFilePut("etc/test.put.cfg", "param1", "value1"),
            // put over existing
            editConfigurationFilePut("etc/test.put.cfg", "param2", "value1"),
            editConfigurationFilePut("etc/test.put.cfg", "param2", "value2"),
            // extend new
            editConfigurationFileExtend("etc/test.put.cfg", "params1", "value1"),
            // extend existing
            editConfigurationFilePut("etc/test.put.cfg", "params2", "value1"),
            editConfigurationFileExtend("etc/test.put.cfg", "params2", "value2"),
            // put new source file
            composite(editConfigurationFilePut("etc/test.put.file.cfg", source1)),
            // put and then replace file
            composite(editConfigurationFilePut("etc/test.replace.cfg", source1)),
            replaceConfigurationFile("etc/test.replace.cfg", source2),
            // put over replaced file
            editConfigurationFilePut("etc/test.replace.cfg", "param6", "value6"),
            // extend over replaced file
            editConfigurationFileExtend("etc/test.replace.cfg", "param4", "value5"),
            // put null over replaced file
            editConfigurationFilePut("etc/test.replace.cfg", "param5", null)
        };
    }

    private void assertConfig(String pid, String property, Object expected) throws IOException {
        org.osgi.service.cm.Configuration conf = configurationAdmin.getConfiguration(pid);
        Dictionary<String, Object> properties = conf.getProperties();
        Object actual = properties.get(property);
        assertEquals(expected, actual);
    }

    @Test
    public void testConfiguration() throws IOException {
        assertConfig("test.put", "param1", "value1");
        assertConfig("test.put", "param2", "value2");
        assertConfig("test.put", "params1", "value1");
        assertConfig("test.put", "params2", "value1,value2");
        assertConfig("test.put.file", "param3", "value3");
        assertConfig("test.replace", "param1", "value11");
        assertConfig("test.replace", "param6", "value6");
        assertConfig("test.replace", "param4", "value4,value5");
    }
}
