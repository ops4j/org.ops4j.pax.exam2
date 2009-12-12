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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleException;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.rbc.internal.RemoteBundleContext;
import org.ops4j.pax.exam.spi.container.TestContainer;
import org.ops4j.pax.exam.spi.container.TestContainerException;
import org.ops4j.pax.exam.spi.container.TimeoutException;

/**
 * A {@link RemoteBundleContext} client, that takes away RMI handling.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 15, 2008
 */
public class RemoteBundleContextClient
    implements TestContainer
{

    /**
     * JCL logger.
     */
    private static final Log LOG = LogFactory.getLog( RemoteBundleContextClient.class );

    /**
     * RMI communication port.
     */
    private final Integer m_rmiPort;
    /**
     * Timeout for looking up the remote bundle context via RMI.
     */
    private final long m_rmiLookupTimeout;
    /**
     * Remote bundle context instance.
     */
    private RemoteBundleContext m_remoteBundleContext;

    /**
     * Constructor.
     *
     * @param rmiPort          RMI communication port (cannot be null)
     * @param rmiLookupTimeout timeout for looking up the remote bundle context via RMI (cannot be null)
     */
    public RemoteBundleContextClient( final Integer rmiPort,
                                      final long rmiLookupTimeout )
    {
        m_rmiPort = rmiPort;
        m_rmiLookupTimeout = rmiLookupTimeout;
    }

    /**
     * {@inheritDoc}
     */
    public <T> T getService( final Class<T> serviceType )
    {
        return getService( serviceType, Constants.NO_WAIT );
    }

    /**
     * {@inheritDoc}
     * Returns a dynamic proxy in place of the actual service, forwarding the calls via the remote bundle context.
     */
    @SuppressWarnings( "unchecked" )
    public <T> T getService( final Class<T> serviceType,
                             final long timeoutInMillis )
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
                            timeoutInMillis,
                            params
                        );
                    }
                    catch( InvocationTargetException e )
                    {
                        throw e.getCause();
                    }
                    catch( RemoteException e )
                    {
                        throw new TestContainerException( "Remote exception", e );
                    }
                    catch( Exception e )
                    {
                        throw new TestContainerException( "Invocation exception", e );
                    }
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     */
    public long installBundle( final String bundleUrl )
    {
        try
        {
            return getRemoteBundleContext().installBundle( bundleUrl );
        }
        catch( RemoteException e )
        {
            throw new TestContainerException( "Remote exception", e );
        }
        catch( BundleException e )
        {
            throw new TestContainerException( "Bundle cannot be installed", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public long installBundle( final String bundleLocation,
                               final byte[] bundle )
        throws TestContainerException
    {
        try
        {
            return getRemoteBundleContext().installBundle( bundleLocation, bundle );
        }
        catch( RemoteException e )
        {
            throw new TestContainerException( "Remote exception", e );
        }
        catch( BundleException e )
        {
            throw new TestContainerException( "Bundle cannot be installed", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startBundle( final long bundleId )
        throws TestContainerException
    {
        try
        {
            getRemoteBundleContext().startBundle( bundleId );
        }
        catch( RemoteException e )
        {
            throw new TestContainerException( "Remote exception", e );
        }
        catch( BundleException e )
        {
            throw new TestContainerException( "Bundle cannot be started", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleStartLevel( final long bundleId,
                                     final int startLevel )
        throws TestContainerException
    {
        try
        {
            getRemoteBundleContext().setBundleStartLevel( bundleId, startLevel );
        }
        catch( RemoteException e )
        {
            throw new TestContainerException( "Remote exception", e );
        }
        catch( BundleException e )
        {
            throw new TestContainerException( "Start level cannot be set", e );
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
        }
        catch( RemoteException e )
        {
            throw new TestContainerException( "Remote exception", e );
        }
        catch( BundleException e )
        {
            throw new TestContainerException( "System bundle cannot be started", e );
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
        }
        catch( RemoteException e )
        {
            throw new TestContainerException( "Remote exception", e );
        }
        catch( BundleException e )
        {
            throw new TestContainerException( "System bundle cannot be stopped", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void waitForState( final long bundleId,
                              final int state,
                              final long timeoutInMillis )
        throws TimeoutException
    {
        try
        {
            getRemoteBundleContext().waitForState( bundleId, state, timeoutInMillis );
        }
        catch( org.ops4j.pax.exam.rbc.internal.TimeoutException e )
        {
            throw new TimeoutException( e.getMessage() );
        }
        catch( RemoteException e )
        {
            throw new TestContainerException( "Remote exception", e );
        }
        catch( BundleException e )
        {
            throw new TestContainerException( "Bundle cannot be found", e );
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
            Throwable reason = null;
            try
            {
                final Registry registry = LocateRegistry.getRegistry( m_rmiPort );
                do
                {
                    try
                    {
                        m_remoteBundleContext =
                            (RemoteBundleContext) registry.lookup( RemoteBundleContext.class.getName() );
                    }
                    catch( ConnectException e )
                    {
                        reason = e;
                    }
                    catch( NotBoundException e )
                    {
                        reason = e;
                    }
                }
                while( m_remoteBundleContext == null
                       && ( m_rmiLookupTimeout == Constants.WAIT_FOREVER
                            || System.currentTimeMillis() < startedTrying + m_rmiLookupTimeout ) );
            }
            catch( RemoteException e )
            {
                reason = e;
            }
            if( m_remoteBundleContext == null )
            {
                throw new TestContainerException( "Cannot get the remote bundle context", reason );
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
