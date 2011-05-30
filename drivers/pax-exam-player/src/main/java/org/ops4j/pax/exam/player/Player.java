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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.ExceptionHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.ExxamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import static org.ops4j.pax.exam.spi.container.PaxExamRuntime.*;
import org.ops4j.pax.exam.spi.driversupport.DefaultExamReactor;
import org.ops4j.pax.exam.spi.probesupport.intern.TestProbeBuilderImpl;
import org.ops4j.pax.exam.spi.reactors.EagerSingleStagedReactorFactory;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;

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

    private static final StagedExamReactorFactory DEFAULT_STRATEGY = new EagerSingleStagedReactorFactory();
    final private TestContainerFactory m_factory;
    final private Option[] m_parts;
    final private TestProbeBuilder m_builder;
   
    public Player( TestContainerFactory containerFactory, Option... parts )
        throws IOException
    {
        Store<InputStream> store = StoreFactory.defaultStore();
        Properties p = new Properties();
        m_factory = containerFactory;
        m_parts = parts;
        m_builder = new TestProbeBuilderImpl( p, store );
    }

    public Player( TestContainerFactory containerFactory )
        throws IOException
    {
        this( containerFactory, new Option[ 0 ] );
    }

    public Player()
        throws IOException
    {
        this( getTestContainerFactory() );
    }

    public Player with( Option... parts )
        throws IOException
    {
        return new Player( m_factory, parts );
    }

    public Player test( Class clazz, Object... args )
        throws Exception
    {
        m_builder.addTest( clazz, args );
        return this;
    }

    public void play() throws IOException
    {
        play( DEFAULT_STRATEGY );
    }

    public void play( StagedExamReactorFactory strategy ) throws IOException
    {
        ExxamReactor reactor = new DefaultExamReactor( createTestSystem( ), m_factory );
        reactor.addConfiguration(  m_parts  );
        reactor.addProbe( m_builder.build() );

        StagedExamReactor stagedReactor = reactor.stage( strategy );

        for( TestAddress target : stagedReactor.getTargets() ) {
            try {
                stagedReactor.invoke( target );
            } catch( Exception e ) {
                Throwable t = ExceptionHelper.unwind( e );
                fail( t.getMessage() );
            }
        }
    }
}
