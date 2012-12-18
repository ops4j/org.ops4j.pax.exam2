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
package org.ops4j.pax.exam.rbc.client.intern;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Stack;

import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.ops4j.pax.exam.rbc.internal.RemoteBundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RemoteBundleContextClientImpl implements RemoteBundleContextClient {

    // TODO duplicate
    private static final String PROBE_SIGNATURE_KEY = "Probe-Signature";

    private RemoteBundleContext remoteBundleContext = null;

    /**
     * JCL logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger( RemoteBundleContextClient.class );

    /**
     * Timeout for looking up the remote bundle context via RMI.
     */
    final private RelativeTimeout rmiLookupTimeout;
    /**
     * Remote bundle context instance.
     */
    // private RemoteBundleContext remoteBundleContext;

    final private Integer registry;

    final private Stack<Long> installed;
    final private String name;

    /**
     * Constructor.
     *
     * @param name             of container
     * @param registry         RMI registry to look at
     * @param rmiLookupTimeout timeout for looking up the remote bundle context via RMI (cannot be null)
     */
    public RemoteBundleContextClientImpl( final String name,
                                          final Integer registry,
                                          final RelativeTimeout timeout )
    {
        assert registry != null : "registry should not be null";

        this.registry = registry;
        this.name = name;
        rmiLookupTimeout = timeout;
        installed = new Stack<Long>();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings( "unchecked" )
    private <T> T getService( final Class<T> serviceType, final String filter, final RelativeTimeout timeout )
    {
        return (T) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{ serviceType },
            new InvocationHandler() {
                /**
                 * {@inheritDoc}
                 * Delegates the call to remote bundle context.
                 */
                public Object invoke( final Object proxy,
                                      final Method method,
                                      final Object[] params )
                    throws Throwable
                {
                    try {
                        return getRemoteBundleContext().remoteCall(
                            method.getDeclaringClass(),
                            method.getName(),
                            method.getParameterTypes(),
                            filter,
                            timeout,
                            params
                        );
                    } catch( InvocationTargetException e ) {
                        throw e.getCause();
                    } catch( RemoteException e ) {
                        throw new RuntimeException( "Remote exception", e );
                    } catch( Exception e ) {
                        throw new RuntimeException( "Invocation exception", e );
                    }
                }
            }
        )
            ;
    }

    public long install( String location, InputStream stream )
    {
        // turn this into a local url because we don't want pass the stream any further.
        try {
            //URI location = store.getLocation( store.store( stream ) );
            // pack as bytecode
            byte[] packed = pack( stream );

            long id = getRemoteBundleContext().installBundle( location, packed );
            installed.push( id );
            getRemoteBundleContext().startBundle( id );
            return id;
        } catch( IOException e ) {
            throw new RuntimeException( e );
        } catch( BundleException e ) {
            throw new RuntimeException( "Bundle cannot be installed", e );
        }
    }

    private byte[] pack( InputStream stream )
    {
        LOG.debug( "Packing probe into memory for true RMI. Hopefully things will fill in.." );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            StreamUtils.copyStream( stream, out, true );
        } catch( IOException e ) {

        }
        return out.toByteArray();
    }

    public void cleanup()
    {
        try {
            while( !installed.isEmpty() ) {
                Long id = installed.pop();
                getRemoteBundleContext().uninstallBundle( id );
            }
        } catch( IOException e ) {
            throw new RuntimeException( e );
        } catch( BundleException e ) {
            throw new RuntimeException( "Bundle cannot be uninstalled", e );
        }

    }

    /**
     * {@inheritDoc}
     */
    public void setBundleStartLevel( final long bundleId,
                                     final int startLevel )
    {
        try {
            getRemoteBundleContext().setBundleStartLevel( bundleId, startLevel );
        } catch( RemoteException e ) {
            throw new RuntimeException( "Remote exception", e );
        } catch( BundleException e ) {
            throw new RuntimeException( "Start level cannot be set", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void start()
    {
        try {
            getRemoteBundleContext().startBundle( 0 );
        } catch( RemoteException e ) {
            throw new RuntimeException( "Remote exception", e );
        } catch( BundleException e ) {
            throw new RuntimeException( "System bundle cannot be started", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop()
    {
        try {
            getRemoteBundleContext().stopBundle( 0 );


        } catch( RemoteException e ) {
            // If its gone, its gone. Cannot do much about it anyway.
            //throw new RuntimeException( "Remote exception", e );
        } catch( BundleException e ) {
            throw new RuntimeException( "System bundle cannot be stopped", e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void waitForState( final long bundleId,
                              final int state,
                              final RelativeTimeout timeout )

    {
        try {
            getRemoteBundleContext().waitForState( bundleId, state, timeout );
        } catch( RemoteException e ) {
            throw new RuntimeException( "waitForState", e );
        } catch( BundleException e ) {
            throw new RuntimeException( "waitForState", e );
        }
    }

    /**
     * Looks up the {@link RemoteBundleContext} via RMI. The lookup will timeout in the specified number of millis.
     *
     * @return remote bundle context
     */
    private synchronized RemoteBundleContext getRemoteBundleContext()
    {
        if( remoteBundleContext == null ) {

            //!! Absolutely necesary for RMI class loading to work
            // TODO maybe use ContextClassLoaderUtils.doWithClassLoader
            Thread.currentThread().setContextClassLoader( this.getClass().getClassLoader() );
            LOG.info( "Waiting for remote bundle context.. on " + registry + " name: " + name + " timout: " + rmiLookupTimeout );
            // TODO create registry here
            Throwable reason = null;
            long startedTrying = System.currentTimeMillis();

            try {
                do {
                    try {
                        Registry reg = LocateRegistry.getRegistry( registry );
                        remoteBundleContext = (RemoteBundleContext) reg.lookup( name );
                    } catch( Exception e ) {
                        reason = e;
                    }

                }
                while( remoteBundleContext == null && ( rmiLookupTimeout.isNoTimeout() || System.currentTimeMillis() < startedTrying + rmiLookupTimeout.getValue() ) );
            } catch( Exception e ) {

                //throw new RuntimeException( "Cannot get the remote bundle context", e );
            }
            if( remoteBundleContext == null ) {
                throw new RuntimeException( "Cannot get the remote bundle context", reason );
            }
            LOG.debug( "Remote bundle context found after " + ( System.currentTimeMillis() - startedTrying ) + " millis" );
        }
        return remoteBundleContext;

    }

    public void call( TestAddress address )
    {
        String filterExpression = "(" + PROBE_SIGNATURE_KEY + "=" + address.root().identifier() + ")";
        ProbeInvoker service = getService( ProbeInvoker.class, filterExpression, rmiLookupTimeout );
        service.call( address.arguments() );
    }

    public String getName()
    {
        return name;
    }
}
