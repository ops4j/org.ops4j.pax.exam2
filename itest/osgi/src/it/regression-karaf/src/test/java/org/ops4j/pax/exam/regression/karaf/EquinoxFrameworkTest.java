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

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.configureConsole;
import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.editConfigurationFilePut;
import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.CoreOptions.maven;

import javax.inject.Inject;

import org.apache.karaf.system.FrameworkType;
import org.apache.karaf.system.SystemService;
import org.apache.karaf.tooling.exam.options.configs.CustomProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;

@RunWith(PaxExam.class)
public class EquinoxFrameworkTest {
    
    @Inject
    SystemService systemService;

    @Configuration
    public Option[] config() {
        return new Option[]{
            karafDistributionConfiguration().frameworkUrl(
                maven("org.apache.karaf", "apache-karaf", "3.0.0.RC1").type("zip")),
            configureConsole().ignoreLocalConsole(),        
            editConfigurationFilePut(CustomProperties.KARAF_FRAMEWORK, "equinox") };
    }

    @Test
    public void test() throws Exception {
        Assert.assertEquals(FrameworkType.equinox, systemService.getFramework());
    }

}
