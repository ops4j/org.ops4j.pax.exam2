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
package org.ops4j.pax.exam.container.def;

import static org.ops4j.pax.exam.Constants.WAIT_FOREVER;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TimeoutException;
import org.ops4j.pax.exam.container.remote.RBCRemoteTarget;
import org.ops4j.pax.exam.rbc.Constants;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It's an abstract implementation which use RBC client to communicate to osgi framework.
 * {@link TestContainer} implementation using Pax Runner.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @author Stephane Chomat
 * @since 0.3.0, December 09, 2008
 */
public abstract class AbstractTestContainer
    implements TestContainer {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractTestContainer.class );
    public static final int SYSTEM_BUNDLE = 0;

    protected boolean m_started = false;

    /**
     * Underlying Test Target
     */
    protected RBCRemoteTarget m_target;
    final protected String m_host;
    final protected int m_port;
    final protected Option[] m_options;

    /**
     * Constructor.
     *
     * @param javaRunner java runner to be used to start up Pax Runner
     */
    public AbstractTestContainer( String host,
                                   int port,
                                   Option[] options )
    {
        m_options = options;
        m_host = host;
        m_port = port;
        
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
     * @return a TestContainer this;
     */
    public synchronized TestContainer start()
    {
    	try {
	        String name = getUUID();
	        Option[] args = combine( m_options, systemProperty( Constants.RMI_NAME_PROPERTY ).value( name ) );
	        parseOption(m_host, m_port, args);
	        
			initRBCRemote( name, getRMITimeout() );
	        
        	startProcess();
	
	        LOG.info( "Wait for test container to finish its initialization "
	            + ( getStartTimeout() == WAIT_FOREVER ? "without timing out" : "for " + getStartTimeout() + " millis" ) );
	     
            waitForState( getSystemBundleId(), getSystemBundleState(), getStartTimeout() );
            
            customizer();
            m_started = true;
        }
        catch ( TimeoutException e )
        {
            throw new TimeoutException( "Test container did not initialize in the expected time of " + getStartTimeout()
                + " millis" );
        } catch( Throwable e ) {
            throw new RuntimeException( "Problem starting container", e );
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized TestContainer stop()
    {
        LOG.info( "Shutting down the test container (Pax Runner)" );
        try {
            if( m_started ) {
                cleanup();
                RemoteBundleContextClient remoteBundleContextClient = m_target.getClientRBC();
                if( remoteBundleContextClient != null ) {
                    remoteBundleContextClient.stop();

                }
                stopProcess();

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
    
    protected void initRBCRemote(String name, long timeout) {
    	 m_target = new RBCRemoteTarget( name, m_port, timeout );
    }

	protected int getSystemBundleState() {
		return Bundle.ACTIVE;
	}
	
	protected abstract String getInfo() ;
	
    
	protected void customizer() {
		// do nothing by default
	}

	protected abstract long getSystemBundleId();;

	protected abstract long getStartTimeout();

	/**
     * start the process
     */
    protected abstract void startProcess() throws Exception;

    /**
     * 
     * @return the timeout for the rbc client
     */
	protected abstract long getRMITimeout();

	/**
	 * Parse the option and set some fields.
	 * @param host the host
	 * @param port the port
	 * @param args the options
	 * 
	 * @throws IOException if need like bad configuration
	 */
	protected abstract void parseOption(String host, int port, Option[] args) throws IOException;

	/**
	 * stop the process
	 */
	protected abstract void stopProcess();

    /**
     * {@inheritDoc}
     */
    public synchronized void waitForState( final long bundleId, final int state, final long timeoutInMillis )
        throws TimeoutException
    {

        m_target.getClientRBC().waitForState( bundleId, state, timeoutInMillis );

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
        return m_target.install( stream );
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void cleanup()
    {
        // unwind installed bundles basically.
        m_target.cleanup();
    }
    
    protected String getUUID() {return UUID.randomUUID().toString();}
    
    protected void printExtraBeforeStart( String[] arguments )
    {
        LOG.info( "Starting up the test container ("+getInfo()+ " )" );
        LOG.info( "Arguments: ( " + arguments.length + ")" );
        for( String s : arguments ) {
            LOG.info( "#   " + s );
        }
    }
}
