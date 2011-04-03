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

import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;
import org.ops4j.pax.exam.spi.driversupport.DefaultExamReactor;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

import static junit.framework.Assert.*;

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

    final private TestContainerFactory m_factory;
    final private Parts[] m_parts;

    private static final StagedExamReactorFactory DEFAULT_STRATEGY = new AllConfinedStagedReactorFactory();

    public Player( TestContainerFactory containerFactory, Parts... parts )
    {
        m_factory = containerFactory;
        m_parts = parts;
    }

    public Player( TestContainerFactory containerFactory )
    {
        this( containerFactory, new Parts[ 0 ] );
    }

    public Player()
    {
        this( PaxExamRuntime.getTestContainerFactory() );
    }

    public Player with( Parts... parts )
    {
        return new Player( m_factory, parts );
    }

    public void play( TestProbeProvider... providers )
        throws Exception
    {
        DefaultExamReactor reactor = new DefaultExamReactor( m_factory );

        for( Parts part : m_parts ) {
            reactor.addConfiguration( part.parts() );
        }

        for( TestProbeProvider p : providers ) {
            reactor.addProbe( p );
        }

        StagedExamReactor stage = reactor.stage( DEFAULT_STRATEGY );

        for( TestAddress target : stage.getTargets() ) {
            try {
                stage.invoke( target );
            } catch( Exception e ) {
                fail( e.getCause().getMessage() );
            }
        }
    }
}
