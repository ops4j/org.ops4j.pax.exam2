package org.ops4j.pax.exam.swoosh;

import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.ops4j.pax.exam.swoosh.probes.WaitForService;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * An entire test harness in a tweet.
 */
public class PlayerTest {

    @Test
    public void play1()
        throws Exception
    {
        new Player().with(
            options(
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-api" ).version( "1.6.1" ),
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-service" ).version( "1.6.1" )
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
