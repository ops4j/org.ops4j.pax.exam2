/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.ops4j.pax.exam.rbc.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Stack;

import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.rbc.Constants;
import org.ops4j.pax.exam.rbc.internal.RemoteBundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link RemoteBundleContext} client, that takes away RMI handling.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 15, 2008
 */
public class RemoteBundleContextClient
{

    /**
     * JCL logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger( RemoteBundleContextClient.class );

    /**
     * RMI communication port.
     */
    final private Integer m_rmiPort;
    /**
     * Timeout for looking up the remote bundle context via RMI.
     */
    final private long m_rmiLookupTimeout;
    /**
     * Remote bundle context instance.
     */
    private RemoteBundleContext m_remoteBundleContext;

    private String m_host = null;

    private Stack<Long> m_installed;

    /**
     * Constructor.
     *
     * @param host             RMI Registry Host
     * @param rmiPort          RMI communication port (cannot be null)
     * @param rmiLookupTimeout timeout for looking up the remote bundle context via RMI (cannot be null)
     */
    public RemoteBundleContextClient( final String host,
                                      final Integer rmiPort,
                                      final long rmiLookupTimeout )
    {
        assert ( host != null ) : "Host should not be null";
        assert ( rmiPort != null ) : "rmiPort should not be null";

        m_rmiPort = rmiPort;
        m_host = host;
        m_rmiLookupTimeout = rmiLookupTimeout;
    }

    /**
     * Constructor.
     *
     * @param rmiPort          RMI communication port (cannot be null)
     * @param rmiLookupTimeout timeout for looking up the remote bundle context via RMI (cannot be null)
     */
    public RemoteBundleContextClient( final Integer rmiPort,
                                      final long rmiLookupTimeout )
    {
        this( null, rmiPort, rmiLookupTimeout );
    }

    /**
     * {@inheritDoc}
     */
    public <T> T getService( Class<T> serviceType, final String filter, final long timeout )
    {
        return (T) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{ serviceType },
            new InvocationHandler()
            {
                /**
                 * {@inheritDoc}
                 * Delegates the call to remote bundle context.
                 */
                public Object invoke( final Object proxy,
                                      final Method method,
                                      final Object[] params )
                    throws Throwable
                {
                    try
                    {
                        return getRemoteBundleContext().remoteCall(
                            method.getDeclaringClass(),
                            method.getName(),
                            method.getParameterTypes(),
                            filter,
                            timeout,
                            params
                        );
                    } catch( InvocationTargetException e )
                    {
                        throw e.getCause();
                    } catch( RemoteException e )
                    {
                        throw new RuntimeException( "Remote exception", e );
                    } catch( Exception e )
                    {
                        throw new RuntimeException( "Invocation exception", e );
                    }
                }
            }
        )
            ;
    }

    public long install( InputStream stream )
    {
        // turn this into a local url because we don't want pass the stream any further.
        try
        {
            //URI location = m_store.getLocation( m_store.store( stream ) );
            // pack as bytecode
            byte[] packed = pack( stream );
            if( m_installed == null )
            {
                m_installed = new Stack<Long>();
            }
            long id = getRemoteBundleContext().installBundle( "no", packed );
            m_installed.push( id );
            getRemoteBundleContext().startBundle( id );
            return id;
        } catch( IOException e )
        {
            throw new RuntimeException( e );
        } catch( BundleException e )
        {
            throw new RuntimeException( "Bundle cannot be installed", e );
        }
    }

    private byte[] pack( InputStream stream )
    {
        LOG.info( "Packing probe into memory for true RMI. Hopefully things will fill in.." );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            StreamUtils.copyStream( stream, out, true );
        } catch( IOException e )
        {

        }
        return out.toByteArray();
    }

    public void cleanup()
    {
        try
        {
            while( ( !m_installed.isEmpty() ) )
            {
                Long id = m_installed.pop();
                getRemoteBundleContext().uninstallBundle( id );
            }
        } catch( IOException e )
        {
            throw new RuntimeException( e );
        } catch( BundleException e )
        {
            throw new RuntimeException( "Bundle cannot be uninstalled", e );
        }

    }

    /**
     * {@inheritDoc}
     */
    public void setBundleStartLevel( final long bundleId,
                                     final int startLevel )
    {
        try
        {
            getRemoteBundleContext().setBundleStartLevel( bundleId, startLevel );
        } catch( RemoteException e )
        {
            throw new RuntimeException( "Remote exception", e );
        } catch( BundleException e )
        {
            throw new RuntimeException( "Start level cannot be set", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void start()
    {
        try
        {
            getRemoteBundleContext().startBundle( 0 );
        } catch( RemoteException e )
        {
            throw new RuntimeException( "Remote exception", e );
        } catch( BundleException e )
        {
            throw new RuntimeException( "System bundle cannot be started", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop()
    {
        try
        {
            getRemoteBundleContext().stopBundle( 0 );
            
            // TODO trear down rbc registry if we created it before:

        } catch( RemoteException e )
        {
            throw new RuntimeException( "Remote exception", e );
        } catch( BundleException e )
        {
            throw new RuntimeException( "System bundle cannot be stopped", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void waitForState( final long bundleId,
                              final int state,
                              final long timeoutInMillis )
    {
        try
        {
            getRemoteBundleContext().waitForState( bundleId, state, timeoutInMillis );
        } catch( Exception e )
        {
            throw new RuntimeException( "Remote exception", e );
        }
    }

    /**
     * Looks up the {@link RemoteBundleContext} via RMI. The lookup will timeout in the specified number of millis.
     *
     * @return remote bundle context
     */
    private RemoteBundleContext getRemoteBundleContext()
    {
        if( m_remoteBundleContext == null )
        {
            long startedTrying = System.currentTimeMillis();
            //!! Absolutely necesary for RMI class loading to work
            // TODO maybe use ContextClassLoaderUtils.doWithClassLoader
            Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
            LOG.info( "Waiting for remote bundle context.. on " + m_host + ":" + m_rmiPort + " timout: " + m_rmiLookupTimeout );
            // TODO create registry here
            Throwable reason = null;
            Registry registry = null;

            try
            {
                do
                {
                    if( registry == null )
                    {
                        try
                        {
                            if( m_host == null )
                            {
                                registry = LocateRegistry.getRegistry( m_rmiPort );
                            }
                            else
                            {
                                registry = LocateRegistry.getRegistry( m_host, m_rmiPort );
                            }
                        } catch( Exception e )
                        {
                            LOG.warn( "Registry not available", e );
                        }
                    }
                    else
                    {
                        try
                        {
                            m_remoteBundleContext = (RemoteBundleContext) registry.lookup( RemoteBundleContext.class.getName() );
                        } catch( ConnectException e )
                        {
                            reason = e;
                        } catch( NotBoundException e )
                        {
                            reason = e;
                        }
                    }
                }
                while( m_remoteBundleContext == null
                       && ( m_rmiLookupTimeout == Constants.WAIT_FOREVER
                            || System.currentTimeMillis() < startedTrying + m_rmiLookupTimeout ) );
            } catch( RemoteException e )
            {
                reason = e;
            }
            if( m_remoteBundleContext == null )
            {
                throw new RuntimeException( "Cannot get the remote bundle context", reason );
            }
            LOG.info(
                "Remote bundle context found after " + ( System.currentTimeMillis() - startedTrying ) + " millis"
            );
        }
        return m_remoteBundleContext;
    }

    /**
     * Getter.
     *
     * @return rmi port
     */
    public Integer getRmiPort()
    {
        return m_rmiPort;
    }

}
