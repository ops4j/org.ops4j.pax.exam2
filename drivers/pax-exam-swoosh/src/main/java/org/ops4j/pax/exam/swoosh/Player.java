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
package org.ops4j.pax.exam.swoosh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;
import org.ops4j.pax.exam.spi.driversupport.DefaultExamReactor;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

import static junit.framework.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Fully functional alternative Pax Exam Driver.
 * This lets your write fully functional setup-tests "in a tweet".
 *
 * Example :
 * <pre>
 *      new Player( new NativeTestContainerFactory() ).with( new PaxLoggingParts( "1.3.RC4" ) ).play( new BundleCheck().allResolved() );
 * </pre>
 *
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since April, 1st, 2011
 */
public class Player {

    private static Logger LOG = LoggerFactory.getLogger( Player.class );

    final private TestContainerFactory m_factory;
    final private Option[] m_parts;

    final private AbstractProbe m_tests;

    private static final StagedExamReactorFactory DEFAULT_STRATEGY = new AllConfinedStagedReactorFactory();

    public Player( TestContainerFactory containerFactory, Option... parts )
    {
        m_factory = containerFactory;
        m_parts = parts;
        m_tests = new AbstractProbe();
    }

    public Player( TestContainerFactory containerFactory )
    {
        this( containerFactory, new Option[ 0 ] );
    }

    public Player()
    {
        this( PaxExamRuntime.getTestContainerFactory() );
    }

    public Player with( Option... parts )
    {
        return new Player( m_factory, parts );
    }

    public Player test( Class c, Object... args)
        throws Exception
    {
        m_tests.add( c,args );
        return this;
    }

     public void play( )
        throws Exception
    {
        DefaultExamReactor reactor = new DefaultExamReactor( m_factory );
        reactor.addConfiguration( m_parts );
        reactor.addProbe(m_tests );

        StagedExamReactor stage = reactor.stage( DEFAULT_STRATEGY );

        for( TestAddress target : stage.getTargets() ) {
            try {

                // find stored args:
                Object[] args = ( (ParameterizedAddress) (target.root()) ).arguments();
                stage.invoke( target, args );

            } catch( Exception e ) {
                LOG.error( "Full Stacktrace for AssertionFailure: ", e );
                fail( e.getCause().getMessage() );
            }
        }
    }
}
