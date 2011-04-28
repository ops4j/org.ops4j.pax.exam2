package org.ops4j.pax.exam.player;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;

import org.junit.Test;
import org.ops4j.pax.exam.TestContainer;
import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;

public class LocalPlayerTest {

    @Test
    public void playLocal()
        throws Exception
    {
        new Player(new LocalTestProbeBuilderImpl()).with(
            options(
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-api" ).version( "1.6.1" ),
                mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-service" ).version( "1.6.1" )
            )
        ).test( getClass(), LogService.class.getName() ).play(new LocalAllConfinedStagedReactorFactory());
    }

    public void probe( TestContainer ctx, String s )
    {
        System.out.println( "Hello World: "+s );
        ctx.waitForState(3, Bundle.ACTIVE, 10000);
    }
}
