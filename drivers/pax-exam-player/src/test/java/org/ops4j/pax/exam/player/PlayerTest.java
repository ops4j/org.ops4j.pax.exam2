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

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import junit.framework.AssertionFailedError;

import org.junit.Test;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.testforge.CountBundles;
import org.ops4j.pax.exam.testforge.WaitForService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * An entire test harness in a tweet.
 */
public class PlayerTest {
    
    private static final int NUM_EXAM_BUNDLES = 14;
    
    /**
     * WARNING: Do NOT use the same as PaxExamRuntime, or the test will fail.
     */
    private static final String PAX_LOGGING_VERSION = "1.6.1";

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
        System.setProperty("osgi.console", "6666");
        new Player().test( CountBundles.class, NUM_EXAM_BUNDLES ).play();

    }

    @Test
    public void twoTests()
        throws Exception
    {
        new Player().test( WaitForService.class, ProbeInvoker.class.getName(), 5000 ).test( CountBundles.class, NUM_EXAM_BUNDLES ).play();

    }

    @Test
    public void play1()
        throws Exception
    {
        new Player().with(
            options(
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-service" ).version( PAX_LOGGING_VERSION ).start()
            )
        ).test( WaitForService.class, LogService.class.getName(), 5000 ).play();

    }

    @Test( expected = AssertionFailedError.class )
    public void missing()
        throws Exception
    {
        new Player().with(
            options(
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-api" ).version( PAX_LOGGING_VERSION )
            )
        ).test( WaitForService.class, LogService.class.getName() ).play();

    }

    @Test
    public void play2()
        throws Exception
    {
        new Player().with(
            options(
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-api" ).version( PAX_LOGGING_VERSION ),
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-service" ).version( PAX_LOGGING_VERSION )
            )
        ).test( getClass(), LogService.class.getName() ).play();
    }

    public void probe( BundleContext ctx, String s )
    {
        for (Bundle b: ctx.getBundles()) {
            System.out.println( b.getSymbolicName() + " " + b.getState() );

        }
    }
}
