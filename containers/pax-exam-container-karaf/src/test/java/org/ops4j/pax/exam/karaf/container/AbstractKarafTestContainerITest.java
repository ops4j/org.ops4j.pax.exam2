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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@RunWith(PaxExam.class)
public abstract class AbstractKarafTestContainerITest {

    private final MavenArtifactUrlReference KARAF_URL = maven("org.apache.karaf", "apache-karaf").type("zip");

    @Inject
    private BundleContext bc;

    @Configuration
    public Option[] config() {
        String karafVersion = karafVersion();
        return new Option[] {
            karafDistributionConfiguration().frameworkUrl(KARAF_URL.version(karafVersion))
                .karafVersion(karafVersion).useDeployFolder(false).unpackDirectory(new File("target/paxexam/unpack/")),
            configureConsole().startLocalConsole().ignoreRemoteShell(), logLevel(LogLevel.INFO)
        };
    }

    public String karafVersion() {
        ConfigurationManager cm = new ConfigurationManager();
        String karafVersion = cm.getProperty("pax.exam.karaf.version", getDefaultKarafVersion());
        return karafVersion;
    }

    protected abstract String getDefaultKarafVersion();

    @Test
    public void checkKarafSystemService() throws Exception {
        assertThat(bc, is(notNullValue()));
        ServiceReference<?> serviceRef = bc
            .getServiceReference("org.apache.karaf.system.SystemService");
        Object service = bc.getService(serviceRef);
        assertThat(service, is(notNullValue()));
    }
    
    public Bundle findBundle(String symbolicName) {
        Bundle[] bundles = bc.getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getSymbolicName().equals(symbolicName)) {
                return bundle;
            }
        }
    
        return null;
    }

}
