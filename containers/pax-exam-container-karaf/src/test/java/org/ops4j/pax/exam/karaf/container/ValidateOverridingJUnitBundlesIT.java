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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.composite;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.KarafDistributionOption;
import org.osgi.framework.Bundle;

@RunWith(PaxExam.class)
public class ValidateOverridingJUnitBundlesIT extends Karaf4TestContainerIT {

    @Override
    @Configuration
    public Option[] config() {
        return new Option[] {
            composite(super.config()),
            KarafDistributionOption.overrideJUnitBundles(),
            mavenBundle().groupId("org.apache.servicemix.bundles").artifactId("org.apache.servicemix.bundles.hamcrest").version("1.3_1"),
            mavenBundle().groupId("org.ops4j.pax.tipi").artifactId("org.ops4j.pax.tipi.junit").version("4.12.0.1"),
            mavenBundle().groupId("org.ops4j.pax.exam").artifactId("pax-exam-invoker-junit").version("4.10.0")
        };
    }
    
    @Test
    public void testOverriddenJUnitBundlesPresent() throws Exception {  
        Bundle hamcrestBundle = findBundle("org.apache.servicemix.bundles.hamcrest");
        assertNotNull(hamcrestBundle);
        assertThat(hamcrestBundle.getVersion().toString(), equalTo("1.3.0.1"));
        
        Bundle junitBundle = findBundle("org.ops4j.pax.tipi.junit");
        assertNotNull(junitBundle);
        assertThat(junitBundle.getVersion().toString(), equalTo("4.12.0.1"));
        
        //As we have not included it, ensure org.ops4j.pax.tipi.hamcrest.core is not present
        assertNull(findBundle("org.ops4j.pax.tipi.hamcrest.core"));
    }
}
