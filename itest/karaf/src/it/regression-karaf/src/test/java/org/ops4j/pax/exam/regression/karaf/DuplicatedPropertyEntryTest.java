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

package org.ops4j.pax.exam.regression.karaf;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.ops4j.pax.exam.regression.karaf.RegressionConfiguration.regressionDefaults;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.osgi.service.cm.ConfigurationAdmin;

@RunWith(PaxExam.class)
public class DuplicatedPropertyEntryTest extends TestBase {

    @Inject
    private ConfigurationAdmin configAdmin;

    @Configuration
    public Option[] config() {
        return new Option[]{
            regressionDefaults(unpackDirectory()),
            editConfigurationFileExtend("etc/tests.cfg", "mykey", "myvalue1"),
            editConfigurationFileExtend("etc/tests.cfg", "mykey", "myvalue2") };
    }

    @Test
    public void testConfiguration_shouldHaveWrittenTheLaterOne() throws Exception {
        org.osgi.service.cm.Configuration configuration = configAdmin.getConfiguration("tests");
        assertEquals("myvalue2", configuration.getProperties().get("mykey"));
    }
}
