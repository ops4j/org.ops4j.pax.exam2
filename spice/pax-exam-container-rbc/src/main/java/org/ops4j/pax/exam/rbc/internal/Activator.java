/*
 * Copyright 2008 Toni Menzel
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.rbc.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Callable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.rbc.Constants;
import org.ops4j.pax.swissbox.core.ContextClassLoaderUtils;

/**
 * Registers the an instance of RemoteTestRunnerService as RMI service using a port set by system property
 * pax.exam.communication.port.
 *
 * Test
 *
 * @author Toni Menzel (tonit)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since Jun 2, 2008
 */
public class Activator
    implements BundleActivator {

    private static final Log LOG = LogFactory.getLog( Activator.class );

    private static final int MAXRETRYCOUNT = 14;
    private static final String MSG_RETRY = "RBC bind stuff failed before. Will retry again perhaps.";
    private static final int PORT_RBC_FROM = 22412;
    private static final int PORT_RBC_TO = PORT_RBC_FROM + 100;

    /**
     * RMI registry.
     */
    private Registry m_registry;
    /**
     * Strong reference to {@link RemoteBundleContext}.
     * !Note: this must be here otherwise JVM will garbage collect it and this will result in an
     * java.rmi.NoSuchObjectException: no such object in table
     */
    private RemoteBundleContext m_remoteBundleContext;
    private Thread m_registerRBCThread;

    /**
     * {@inheritDoc}
     */
    public synchronized void start( final BundleContext bundleContext )
        throws Exception
    {
        //!! Absolutely necessary for RMIClassLoading to work
        m_registerRBCThread = new Thread( new Runnable() {

            public void run()
            {
                int retries = 0;
                boolean valid = false;
                do {
                    retries++;
                    valid = register( bundleContext );
                    if( !valid ) {
                        try {
                            LOG.info( MSG_RETRY );
                            Thread.sleep( 500 );
                        } catch( InterruptedException e ) {
                            e.printStackTrace();
                        }
                    }
                } while( !Thread.currentThread().isInterrupted() && !valid && retries < MAXRETRYCOUNT );
            }
        }
        );
        m_registerRBCThread.start();

    }

    private boolean register( final BundleContext bundleContext )
    {
        try {
            ContextClassLoaderUtils.doWithClassLoader(
                null, // getClass().getClassLoader()
                new Callable<Object>()

                {
                    public Object call()
                        throws Exception
                    {
                        // try to find port from property
                        int port = getPort();
                        String host = getHost();
                        String name = getName();

                        LOG.debug( "Trying to find registry on [host=" + host + " port=" + port + "]" );
                        m_registry = LocateRegistry.getRegistry( getHost(), getPort() );

                        bindRBC( m_registry, name, bundleContext );
                        LOG.info( "(++) Container with name " + name + " has added its RBC" );

                        return null;
                    }
                }

            );
            return true;
        } catch( Exception e ) {
            LOG.warn( "Registration of RBC failed: ", e );
        }
        return false;
    }

    private void bindRBC( Registry registry, String name, BundleContext bundleContext )
        throws RemoteException, BundleException
    {
        Integer objectPort = new FreePort( PORT_RBC_FROM, PORT_RBC_TO ).getPort();
        LOG.debug( "Now Binding " + RemoteBundleContext.class.getSimpleName() + " as name=" + name + " to RMI registry" );
        Remote remoteStub = UnicastRemoteObject.exportObject( m_remoteBundleContext = new RemoteBundleContextImpl( bundleContext.getBundle( 0 ).getBundleContext() ), objectPort );
        registry.rebind( getName(), remoteStub );
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void stop( BundleContext bundleContext )
        throws Exception
    {
        m_registerRBCThread.interrupt();
        String name = getName();
        m_registry.unbind( name );
        UnicastRemoteObject.unexportObject( m_remoteBundleContext, true );

        // UnicastRemoteObject.unexportObject( m_registry, true );
        m_registry = null;
        m_remoteBundleContext = null;
        LOG.info( "(--) Container with name " + name + " has removed its RBC" );
    }

    /**
     * @return the port where {@link RemoteBundleContext} is being exposed as an RMI service.
     *
     * @throws BundleException - If communication port cannot be determined
     */
    private int getPort()
        throws BundleException
    {
        // The port is usually given by starting client (owner of this process).
        try {
            return Integer.parseInt( System.getProperty( Constants.RMI_PORT_PROPERTY ) );
        } catch( NumberFormatException e ) {
            throw new BundleException(
                "Cannot determine rmi registry port. Ensure that property "
                + Constants.RMI_PORT_PROPERTY
                + " is set to a valid Integer."
            );
        }
    }

    private String getHost()
        throws BundleException
    {
        return System.getProperty( Constants.RMI_HOST_PROPERTY );

    }

    private String getName()
        throws BundleException
    {
        return System.getProperty( Constants.RMI_NAME_PROPERTY );

    }
}
