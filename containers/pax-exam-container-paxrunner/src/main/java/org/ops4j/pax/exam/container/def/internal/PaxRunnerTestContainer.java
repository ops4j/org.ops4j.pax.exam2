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
import java.util.UUID;

import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.CompositeCustomizer;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
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
import org.ops4j.pax.runner.platform.StoppableJavaRunner;

import static org.ops4j.pax.exam.Constants.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.*;

/**
 * {@link TestContainer} implementation using Pax Runner.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 09, 2008
 */
public class PaxRunnerTestContainer
    implements TestContainer {

    private static final Logger LOG = LoggerFactory.getLogger( PaxRunnerTestContainer.class );
    public static final int SYSTEM_BUNDLE = 0;
    
	private static final String RUNNER_TEST_CONTAINER = "PaxRunnerTestContainer.start";

    private boolean m_started = false;

    final private StoppableJavaRunner m_javaRunner;
    final private RMIRegistry m_reg;
    final private ExamSystem m_system;
    final private String m_frameworkName;
    /**
     * Underlying Test Target
     */
    private RBCRemoteTarget m_target;

    /**
     * Constructor.
     *
     * @param javaRunner java runner to be used to start up Pax Runner
     * @param host       RMI Hostname for this container.
     * @param port       RMI Port to be used (Registry must exist)
     * @param options    Options to be parsed.
     */
    public PaxRunnerTestContainer(
    		final ExamSystem system,
    		final StoppableJavaRunner javaRunner,
            final RMIRegistry registry,
            final FrameworkOption selectedFramework 
       		)
    {
        m_javaRunner = javaRunner;
        m_system = system;
        m_reg = registry;
        m_frameworkName = selectedFramework.getName();
    }

    /**
     * {@inheritDoc} Delegates to {@link RemoteBundleContextClient}.
     */
    public void setBundleStartLevel( final long bundleId, final int startLevel )
        throws TestContainerException
    {
        m_target.getClientRBC().setBundleStartLevel( bundleId, startLevel );
    }

    /**
     * {@inheritDoc}
     */
    public synchronized TestContainer start()
    {
        try {
            String name = m_system.createID( RUNNER_TEST_CONTAINER );
            
            ExamSystem subsystem = m_system.subsystem(
            		options(
	            		systemProperty( Constants.RMI_HOST_PROPERTY ).value( m_reg.getHost() ),
						systemProperty( Constants.RMI_PORT_PROPERTY ).value( "" + m_reg.getPort() ),
						systemProperty( Constants.RMI_NAME_PROPERTY ).value( name ))
					);
            m_target = new RBCRemoteTarget( name, m_reg.getPort() ,subsystem.getTimeout().getValue() );

            long startedAt = System.currentTimeMillis();
            URLUtils.resetURLStreamHandlerFactory();
            
            String[] arguments = ArgumentsBuilder.build(subsystem);
            printExtraBeforeStart( arguments );

            Run.start( m_javaRunner, arguments );
            LOG.info( "Test Container started in " + ( System.currentTimeMillis() - startedAt ) + " millis" );
            LOG.debug( "Wait for test container to finish its initialization " + ( m_system.getTimeout()  ) );
            waitForState( SYSTEM_BUNDLE, Bundle.ACTIVE, m_system.getTimeout().getValue() );

            //new CompositeCustomizer( argBuilder.getCustomizers() ).customizeEnvironment( argBuilder.getWorkingFolder() );

            m_started = true;
        } catch( IOException e ) {
            throw new RuntimeException( "Problem starting container" );
        }
        return this;
    }

    protected SystemPropertyOption addRMINameProperty(String name) {
        return systemProperty( Constants.RMI_NAME_PROPERTY ).value( name );
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
        LOG.info( "Shutting down the test container (Pax Runner)" );
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
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    private void waitForState( final long bundleId, final int state, final long timeoutInMillis )
        throws TimeoutException
    {

        m_target.getClientRBC().waitForState( bundleId, state, timeoutInMillis );

    }

    /**
     * Return the options required by this container implementation.
     *
     * @return local options
     */

    public synchronized void call( TestAddress address )
    {
        m_target.call( address );
    }

    public synchronized long install( InputStream stream )
    {
        return m_target.install( stream );
    }

    @Override
    public String toString()
    {
        return "PaxRunnerTestContainer{" + m_frameworkName + "}";
    }
}
