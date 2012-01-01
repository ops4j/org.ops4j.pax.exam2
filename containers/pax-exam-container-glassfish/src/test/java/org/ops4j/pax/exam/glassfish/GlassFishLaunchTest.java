/*
 * Copyright 2012 Harald Wellmann
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
package org.ops4j.pax.exam.glassfish;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.CoreOptions.workingDirectory;

import javax.inject.Inject;

import org.glassfish.embeddable.GlassFish;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleContext;

@RunWith( JUnit4TestRunner.class )
@ExamReactorStrategy( AllConfinedStagedReactorFactory.class )
public class GlassFishLaunchTest
{
    @Inject
    private BundleContext bc;

    @Inject
    private GlassFish gf;

    @Configuration( )
    public Option[] config()
    {
        return options(
            workingDirectory( "/tmp/osgi-cache" ),
            // cleanCaches(false),
            url( "file:/home/hwellmann/gf/glassfish-3.1.1-orig/glassfish/modules/glassfish.jar" )
                .startLevel( 1 ),
            systemProperty( "osgi.console" ).value( "6666" ),
            systemProperty( "com.sun.aas.installRoot" ).value(
                "/home/hwellmann/gf/glassfish-3.1.1-orig/glassfish" ),
            systemProperty( "com.sun.aas.instanceRoot" ).value(
                "/home/hwellmann/gf/glassfish-3.1.1-orig/glassfish/domains/domain1" ),
            frameworkStartLevel( START_LEVEL_TEST_BUNDLE ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.exam.inject.link" ).startLevel( 5 ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.extender.service.link" ).startLevel(
                START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle( "org.slf4j", "jul-to-slf4j", "1.6.1" ),
            mavenBundle( "org.slf4j", "slf4j-api", "1.6.1" ),
            mavenBundle( "ch.qos.logback", "logback-core", "0.9.29" ),
            mavenBundle( "ch.qos.logback", "logback-classic", "0.9.29" ),
            systemProperty( "java.util.logging.config.file" ).value(
                PathUtils.getBaseDir() + "/src/main/resources/logging.properties" ),
            systemProperty( "GlassFish_Platform" ).value( "Equinox" ),
            frameworkProperty( "org.osgi.framework.bundle.parent" ).value( "framework" ),
            frameworkProperty( "osgi.resolver.preferSystemPackages" ).value( "false" ),
            frameworkProperty( "osgi.compatibility.bootdelegation" ).value( "false" ),
            systemPackages( "org.ops4j.pax.exam;version=2.4.0",
                "org.glassfish.embeddable;version=3.1", "org.glassfish.embeddable.spi;version=3.1" ),

            frameworkProperty( "felix.log.level" ).value( "3" ),
            junitBundles() );
    }

    @Test
    public void getBundleContext() throws InterruptedException
    {
        // Thread.sleep( 50000 );
        assertThat( bc, is( notNullValue() ) );
        assertThat( gf, is( notNullValue() ) );
    }
}
