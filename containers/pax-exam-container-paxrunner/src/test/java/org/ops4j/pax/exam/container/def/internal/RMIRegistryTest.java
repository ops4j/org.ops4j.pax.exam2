package org.ops4j.pax.exam.container.def.internal;

import org.junit.Test;

import static junit.framework.Assert.*;

/**
 *
 */
public class RMIRegistryTest {

    private static final Integer DEFAULTPORT = 1214;

    @Test
    public void testRepititveLaunch()
    {
        int old = new RMIRegistry( DEFAULTPORT, DEFAULTPORT + 1, DEFAULTPORT + 99 ).selectGracefully().getPort();
        for( int i = 0; i < 100; i++ ) {
            int port = new RMIRegistry( DEFAULTPORT, DEFAULTPORT + 1, DEFAULTPORT + 99 ).selectGracefully().getPort();
            assertTrue("Port stays the same",old == port);
            
        }
    }

    @Test
    public void testFailureDetermination()
    {

    }
}
