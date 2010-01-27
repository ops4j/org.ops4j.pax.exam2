/*
 * Copyright 2008 Toni Menzel.
 * Copyright 2008 Alin Dreghiciu
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
package org.ops4j.pax.exam.it;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.AppliesTo;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.RequiresConfiguration;
import org.ops4j.pax.exam.options.SystemPropertyOption;

/**
 * Currently Recommended Configuration Pattern for more complex setups (more than hello world stuff):
 * <p/>
 * First you put the very basic config somewhere and annotate it with @AppliesTo ( ".*")
 * <p/>
 * Then you write other configs with just @Configuration
 * <p/>
 * So, now, when you write your Tests you get the basic config from appliesTo.
 * But you can "request" additional configuration by using @RequiresConfiguration.
 * <p/>
 * This lets you mix and match configs as wanted.
 * <p/>
 * The named-AppliesTo usescase dictates a nameing schema to the tests.
 * If it is needed, it is possible.
 *
 * @author Toni Menzel (tonit)
 * @since Jan 8, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class ConfigurationAnnotationTest
{

    @Configuration
    @AppliesTo( { ".*" } )
    public static Option[] rootConfig()
    {
        return options(
            systemProperties( new SystemPropertyOption( "rootConfig" ).value( "true" ) )
        );
    }

    @Configuration
    public static Option[] standardConfig()
    {
        return options(
            systemProperties( new SystemPropertyOption( "standardConfig" ).value( "true" ) )
        );
    }

    @Configuration
    public static Option[] extraConfig()
    {
        return options(
            systemProperties( new SystemPropertyOption( "extraConfig" ).value( "true" ) )
        );
    }

    @Configuration
    @AppliesTo( { ".*test3.*", "test4" } )
    public static Option[] loggingConfig()
    {
        return options(
            systemProperties( new SystemPropertyOption( "loggingConfig" ).value( "true" ) )
        );
    }

    @Test
    public void test1( final BundleContext bundleContext )
    {
        assertEquals( "true", bundleContext.getProperty( "rootConfig" ) );
        assertEquals( "true", bundleContext.getProperty( "standardConfig" ) );
        assertEquals( "true", bundleContext.getProperty( "extraConfig" ) );
        assertNull( bundleContext.getProperty( "loggingConfig" ) );
    }

    @Test
    @RequiresConfiguration( ".*extraConfig.*" )
    public void test2( final BundleContext bundleContext )
    {
        assertEquals( "true", bundleContext.getProperty( "rootConfig" ) );
        assertEquals( "true", bundleContext.getProperty( "extraConfig" ) );
        assertNull( bundleContext.getProperty( "loggingConfig" ) );
        assertNull( bundleContext.getProperty( "standardConfig" ) );
    }

    @Test
    public void test4( final BundleContext bundleContext )
    {
        assertEquals( "true", bundleContext.getProperty( "rootConfig" ) );
        assertEquals( "true", bundleContext.getProperty( "standardConfig" ) );
        assertEquals( "true", bundleContext.getProperty( "extraConfig" ) );
        assertEquals( "true", bundleContext.getProperty( "loggingConfig" ) );
    }

    /*
    * @Note Toni, Feb, 03, 2009
    * What we have here currently is a merge:
    * It gets standardConfig because of RequiresConfiguration and loggingConfig because of AppliesTo.
    */

    @Test
    @RequiresConfiguration( ".*standardConfig.*" )
    public void test3( final BundleContext bundleContext )
    {
        assertEquals( "true", bundleContext.getProperty( "rootConfig" ) );
        assertEquals( "true", bundleContext.getProperty( "standardConfig" ) );
        assertNull( bundleContext.getProperty( "extraConfig" ) );
        assertEquals( "true", bundleContext.getProperty( "loggingConfig" ) );
    }

}

