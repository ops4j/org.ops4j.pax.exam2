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
package org.ops4j.pax.exam.regression.multi;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.equinox;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.knopflerfish;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.CoreOptions.when;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

/**
 * Default configuration for native container regression tests, overriding the default test
 * system configuration.
 * <p>
 * We do not need the Remote Bundle Context for Native Container, and we prefer unified logging
 * with logback.
 * <p>
 * To override the standard options, you need to set the configuration property {@code pax.exam.system = default}.
 * 
 * @author Harald Wellmann
 * @since Dec 2011
 */
public class RegressionConfiguration
{
    public static Option regressionDefaults()
    {        
        return composite(
            
            // copy most options from PaxExamRuntime.defaultTestSystemOptions(),
            // except RBC and Pax Logging
            bootDelegationPackage( "sun.*" ),
            cleanCaches(),

            url( "link:classpath:META-INF/links/org.ops4j.pax.exam.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.exam.inject.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.extender.service.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),            
            url( "link:classpath:META-INF/links/org.ops4j.base.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),            
            url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.core.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),            
            url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.extender.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),            
            url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.lifecycle.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),            
            url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.framework.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),            
            url( "link:classpath:META-INF/links/org.apache.geronimo.specs.atinject.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),            
            
            // add SLF4J and logback bundles
            mavenBundle("org.slf4j", "slf4j-api").versionAsInProject().startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle("ch.qos.logback", "logback-core").versionAsInProject().startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject().startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            
            // Set logback configuration via system property.
            // This way, both the driver and the container use the same configuration
            systemProperty("logback.configurationFile").value( "file:" + PathUtils.getBaseDir() +
            		"/src/test/resources/logback.xml" ),
            		
            // only for Pax Runner container
            when ( "paxrunner".equals( System.getProperty("pax.exam.container"))).useOptions(
                url( "link:classpath:META-INF/links/org.ops4j.pax.exam.rbc.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                mavenBundle("org.slf4j", "jcl-over-slf4j").versionAsInProject().startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                equinox(),      
                felix(),
                knopflerfish()),            		
            
            // not for Pax Runner container
            when ( ! "paxrunner".equals( System.getProperty("pax.exam.container"))).useOptions(
                frameworkStartLevel( START_LEVEL_TEST_BUNDLE ))
            );
    }
    
    public static boolean isPaxRunnerContainer() {
        return "paxrunner".equals( System.getProperty( "pax.exam.container" ) );
    }

    public static boolean isNativeContainer() {
        return "native".equals( System.getProperty( "pax.exam.container" ) );
    }

    public static boolean isEquinox() {
        return "equinox".equals( System.getProperty( "pax.exam.framework" ) );
    }

    public static boolean isFelix() {
        return "felix".equals( System.getProperty( "pax.exam.framework" ) );
    }

    public static boolean isKnopflerfish() {
        return "knopflerfish".equals( System.getProperty( "pax.exam.framework" ) );
    }
}
