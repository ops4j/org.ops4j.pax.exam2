/*
 * Copyright 2011 Harald Wellmann.
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

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.propagateSystemProperty;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureSecurity;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;

import java.io.File;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.configs.CustomProperties;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;

/**
 * Default configuration for native container regression tests, overriding the default test system
 * configuration.
 * <p>
 * We do not need the Remote Bundle Context for Native Container, and we prefer unified logging with
 * logback.
 * <p>
 * To override the standard options, you need to set the configuration property
 * {@code pax.exam.system = default}.
 * 
 * @author Harald Wellmann
 * @since Dec 2011
 */
public class RegressionConfiguration {

    public static final String HTTP_PORT = "9080";

    public static Option regressionDefaults() {
        return regressionDefaults("target/exam");
    }

    public static Option regressionDefaults(String unpackDir) {
        return composite(
            karafDistributionConfiguration().frameworkUrl(mvnKarafDist())
                .unpackDirectory(unpackDir == null ? null : new File(unpackDir)),
            
            configureConsole().ignoreLocalConsole(),
            configureConsole().ignoreRemoteShell(),
            configureSecurity().disableKarafMBeanServerBuilder(),
            
            when(isEquinox()).useOptions(
                editConfigurationFilePut(CustomProperties.KARAF_FRAMEWORK, "equinox"),
                propagateSystemProperty("pax.exam.framework"),
                systemProperty("osgi.console").value("6666"),
                systemProperty("osgi.console.enable.builtin").value("true")));
    }

    public static boolean isEquinox() {
        return "equinox".equals(System.getProperty("pax.exam.framework"));
    }

    public static boolean isFelix() {
        return "felix".equals(System.getProperty("pax.exam.framework"));
    }
    
    public static MavenArtifactUrlReference mvnKarafDist() {
        return maven().groupId("org.apache.karaf")
            .artifactId("apache-karaf").type("tar.gz").versionAsInProject();
    }
    
    public static MavenArtifactUrlReference featureRepoStandard() {
        return maven().groupId("org.apache.karaf.features").artifactId("standard").type("xml")
            .classifier("features").versionAsInProject();
    }

}
