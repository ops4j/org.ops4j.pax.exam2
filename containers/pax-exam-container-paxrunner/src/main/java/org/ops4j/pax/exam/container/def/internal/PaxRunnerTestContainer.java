/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.container.def.internal;

import java.io.IOException;
import java.io.InputStream;

import org.ops4j.pax.exam.options.ServerModeOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TimeoutException;
import org.ops4j.pax.exam.container.remote.RBCRemoteTarget;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.exam.rbc.Constants;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.handler.internal.URLUtils;
import org.ops4j.pax.runner.platform.DefaultJavaRunner;
import org.ops4j.pax.runner.platform.StoppableJavaRunner;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * {@link TestContainer} implementation using Pax Runner.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 09, 2008
 */
public class PaxRunnerTestContainer implements TestContainer {

    private static final String RUNNER_TEST_CONTAINER = "PaxRunnerTestContainer.start";
    private static final Logger LOG = LoggerFactory.getLogger( PaxRunnerTestContainer.class );
    private static final boolean BLOCKING_RUNNER_INTERNALLY = true;
    public static final int SYSTEM_BUNDLE = 0;

    final private StoppableJavaRunner m_javaRunner;
    final private RMIRegistry m_reg;
    final private FrameworkOption m_selectedFramework;

    private boolean m_started = false;

    /**
     * Underlying Test Target
     */
    private RBCRemoteTarget m_target;

    final private ExamSystem m_system;

    /**
     * Constructor.
     *
     * @param system            the PaxExam System to be used
     * @param registry          rmiRegistry information to be used in this container (which uses remoting).
     * @param selectedFramework framework to be started in this container.
     */
    public PaxRunnerTestContainer(
        final ExamSystem system,
        final RMIRegistry registry,
        final FrameworkOption selectedFramework )
    {
        m_javaRunner = new AsyncJavaRunner( new DefaultJavaRunner( BLOCKING_RUNNER_INTERNALLY ) );
        m_reg = registry;
        m_system = system;
        m_selectedFramework = selectedFramework;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized TestContainer start()
    {
        try {
            String name = m_system.createID( RUNNER_TEST_CONTAINER );

            ExamSystem m_subsystem = m_system.fork(
                options(
                    systemProperty( Constants.RMI_HOST_PROPERTY ).value( m_reg.getHost() ),
                    systemProperty( Constants.RMI_PORT_PROPERTY ).value( "" + m_reg.getPort() ),
                    systemProperty( Constants.RMI_NAME_PROPERTY ).value( name )
                )
            );
            m_target = new RBCRemoteTarget( name, m_reg.getPort(), m_subsystem.getTimeout() );

            long startedAt = System.currentTimeMillis();
            URLUtils.resetURLStreamHandlerFactory();

            String[] arguments = ArgumentsBuilder.build( m_subsystem, m_selectedFramework );
            printExtraBeforeStart( arguments );

            Run.start( m_javaRunner, arguments );
            LOG.debug( "Test Container started in " + ( System.currentTimeMillis() - startedAt ) + " millis" );
            LOG.info( "Wait for test container to finish its initialization " + ( m_subsystem.getTimeout() ) );

            if( m_subsystem.getOptions( ServerModeOption.class ).length == 0 ) {
                waitForState( SYSTEM_BUNDLE, Bundle.ACTIVE, m_subsystem.getTimeout() );
            }
            else {
                LOG.info( "System runs in Server Mode. Which means, not Test facility bundles available on target system." );
            }

            // Unsupported currently
            // new CompositeCustomizer( argBuilder.getCustomizers() ).customizeEnvironment( argBuilder.getWorkingFolder() );
            m_started = true;
        } catch( IOException e ) {
            throw new RuntimeException( "Problem starting container" );
        }
        return this;
    }

    private void printExtraBeforeStart( String[] arguments )
    {
        LOG.debug( "Starting up the test container (Pax Runner " + Info.getPaxRunnerVersion() + " )" );
        LOG.debug( "Pax Runner Arguments: ( " + arguments.length + ")" );
        for( String s : arguments ) {
            LOG.debug( "#   " + s );
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized TestContainer stop()
    {
        LOG.debug( "Shutting down the test container (Pax Runner)" );
        try {
            if( m_started ) {
                m_target.stop();
                RemoteBundleContextClient remoteBundleContextClient = m_target.getClientRBC();
                if( remoteBundleContextClient != null ) {
                    remoteBundleContextClient.stop();

                }
                if( m_javaRunner != null ) {
                    m_javaRunner.shutdown();
                }
            }
            else {
                throw new RuntimeException( "Container never came up" );
            }
        } finally {
            m_started = false;
            m_target = null;
            m_system.clear();
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    private void waitForState( final long bundleId, final int state, final RelativeTimeout timeout )
        throws TimeoutException
    {
        m_target.getClientRBC().waitForState( bundleId, state, timeout );

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void call( TestAddress address )
    {
        m_target.call( address );
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long install( InputStream stream )
    {
        return install( "local", stream );
    }

    /**
     * {@inheritDoc}
     */
    public synchronized long install( String location, InputStream stream )
    {
        return m_target.install( location, stream );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return "PaxRunnerTestContainer{" + m_selectedFramework.getName() + "}";
    }
}
