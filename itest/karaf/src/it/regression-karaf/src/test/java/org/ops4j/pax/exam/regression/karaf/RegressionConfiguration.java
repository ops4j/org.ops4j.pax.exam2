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
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;

import java.io.File;

import org.ops4j.pax.exam.ConfigurationManager;
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

    public static Option regressionDefaults() {
        return regressionDefaults(null);        
    }

    public static Option regressionDefaults(String unpackDir) {
        return composite(

            karafDistributionConfiguration().frameworkUrl(mvnKarafDist()).karafVersion(karafVersion()).
                unpackDirectory(unpackDir == null ? null : new File(unpackDir)).useDeployFolder(false),                
            
                configureConsole().ignoreLocalConsole(),
            
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
            .artifactId("apache-karaf").type("zip").version(karafVersion());
    }
    
    public static String karafVersion() {
        ConfigurationManager cm = new ConfigurationManager();
        String karafVersion = cm.getProperty("pax.exam.karaf.version", "3.0.0.RC1");
        return karafVersion;
    }
}
