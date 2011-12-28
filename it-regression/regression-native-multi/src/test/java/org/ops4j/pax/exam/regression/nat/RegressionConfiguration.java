package org.ops4j.pax.exam.regression.nat;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;

import org.ops4j.pax.exam.Option;

public class RegressionConfiguration
{
    public static Option regressionDefaults()
    {
        return composite(
            bootDelegationPackage( "sun.*" ),
            frameworkStartLevel( START_LEVEL_TEST_BUNDLE ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.exam.inject.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.extender.service.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.osgi.compendium.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.apache.geronimo.specs.atinject.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle("org.slf4j", "slf4j-api").versionAsInProject().startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle("ch.qos.logback", "logback-core").versionAsInProject().startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle("ch.qos.logback", "logback-classic").versionAsInProject().startLevel( START_LEVEL_SYSTEM_BUNDLES ),            
            systemProperty("logback.configurationFile").value( "file:src/test/resources/logback.xml" )
            );
    }


}
