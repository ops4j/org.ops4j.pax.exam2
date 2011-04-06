package org.ops4j.pax.exam.swoosh;

import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.swoosh.probes.CountBundles;
import org.ops4j.pax.exam.swoosh.probes.WaitForService;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * An entire test harness in a tweet.
 */
public class PlayerTest {

    @Test
    public void minimal()
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
        new Player().test( CountBundles.class, 4 ).play();

    }

    @Test
    public void twoTests()
        throws Exception
    {
        new Player().test( WaitForService.class, ProbeInvoker.class.getName(), 5000 ).test( CountBundles.class, 4 ).play();

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
