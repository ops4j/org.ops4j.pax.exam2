/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.player;

import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.testforge.CountBundles;
import org.ops4j.pax.exam.testforge.WaitForService;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * An entire test harness in a tweet.
 */
public class PlayerTest {

    @Test( expected = TestContainerException.class )
    public void noTestAdded()
        throws Exception
    {
        new Player().play();

    }

    @Test
    public void minimalWait()
        throws Exception
    {
        new Player().test( WaitForService.class, ProbeInvoker.class.getName(), 5000 ).play();

    }

    @Test
    public void count()
        throws Exception
    {
        new Player().test( CountBundles.class, 6 ).play();

    }

    @Test
    public void twoTests()
        throws Exception
    {
        new Player().test( WaitForService.class, ProbeInvoker.class.getName(), 5000 ).test( CountBundles.class, 6 ).play();

    }

    @Test
    public void play1()
        throws Exception
    {
        new Player().with(
            options(
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-api" ).version( "1.6.1" ).startLevel( 1 ),
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-service" ).version( "1.6.1" ).start()
            )
        ).test( WaitForService.class, LogService.class.getName(), 5000 ).play();

    }

    @Test( expected = AssertionFailedError.class )
    public void missing()
        throws Exception
    {
        new Player().with(
            options(
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-api" ).version( "1.6.1" )
            )
        ).test( WaitForService.class, LogService.class.getName() ).play();

    }

    @Test
    public void play2()
        throws Exception
    {
        new Player().with(
            options(
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-api" ).version( "1.6.1" ),
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-service" ).version( "1.6.1" )
            )
        ).test( getClass(), LogService.class.getName() ).play();
    }

    public void probe( BundleContext ctx, String s )
    {
        System.out.println( "Hello World" );
    }
}
