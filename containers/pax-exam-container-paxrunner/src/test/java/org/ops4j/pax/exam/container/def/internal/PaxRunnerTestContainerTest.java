package org.ops4j.pax.exam.container.def.internal;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Matchers;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.ops4j.pax.runner.platform.StoppableJavaRunner;

import static org.mockito.Mockito.*;

/**
 * Tests for PaxRunnerTestContainer
 */
public class PaxRunnerTestContainerTest {

    private static final Logger LOG = LoggerFactory.getLogger( PaxRunnerTestContainerTest.class );

    // @Test
    public void testLifecycleWithRBC()
        throws Exception
    {
        Long timeout = 20000L;

        StoppableJavaRunner runner = mock( StoppableJavaRunner.class );

        RemoteBundleContextClient client = mock( RemoteBundleContextClient.class );

        RMIRegistry rmiRegistry = new RMIRegistry( 10000 );
        TestContainer testContainer = new PaxRunnerTestContainer( runner, rmiRegistry.getHost(),rmiRegistry.getPort(),new Option[]{ } );

        // START PHASE
        testContainer.start();
        verify( runner ).exec( Matchers.<String[]>anyVararg(), Matchers.<String[]>anyVararg(), anyString(), Matchers.<String[]>anyVararg(), anyString(), any( File.class ) );
        verify( client ).waitForState( PaxRunnerTestContainer.SYSTEM_BUNDLE, Bundle.ACTIVE, timeout );

        // STOP PHASE
        testContainer.stop();
        verify( client ).stop();
        verify( runner ).shutdown();


    }

    /**
     * This involves the entire underlying Pax Runner + RBC Stack, So we just hope that goes through.
     *
     * @throws Exception In case of a problem
     */
    @Test
    public void rbcTest()
        throws Exception
    {
        TestContainer testContainer = new PaxRunnerTestContainerFactory().parse()[ 0 ];
        testContainer.start();
        testContainer.stop();
    }

    /**
     * This is because we had issues with proper RMI handling in the past. This test is very routh but about to cover that.
     *
     * @throws Exception In case of problems
     */
    @Test
    public void restartTest()
        throws Exception
    {
        TestContainer testContainer = new PaxRunnerTestContainerFactory().parse()[ 0 ];
        for( int i = 0; i <= 10; i++ ) {
            LOG.info( "----------------Container start nr.: " + i );
            testContainer.start();
            LOG.info( "----------------Container stop nr.: " + i );
            testContainer.stop();
        }


    }

    @Test
    public void mutlipleFactories()
        throws Exception
    {
        TestContainer testContainer = new PaxRunnerTestContainerFactory().parse()[ 0 ];
        TestContainer testContainer2 = new PaxRunnerTestContainerFactory().parse()[ 0 ];

        for( int i = 0; i <= 5; i++ ) {
            LOG.info( "----------------Container start nr.: " + i );
            testContainer.start();
            LOG.info( "----------------Container2 start nr.: " + i );
            testContainer2.start();
            LOG.info( "----------------Container stop nr.: " + i );
            testContainer.stop();
            LOG.info( "----------------Container stop2 nr.: " + i );
            testContainer2.stop();
        }


    }

    private File getCache()
        throws IOException
    {
        File f = File.createTempFile( "pax", "exam" );
        f.delete();
        f.mkdirs();
        return f;
    }
}
