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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Dictionary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.startlevel.StartLevel;

import static org.ops4j.lang.NullArgumentException.*;

/**
 * {@link RemoteBundleContext} implementaton.
 *
 * @author Toni Menzel (tonit)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.1.0, June 10, 2008
 */
public class RemoteBundleContextImpl
    implements RemoteBundleContext, Serializable
{

    /**
     * JCL Logger.
     */
    private static final Log LOG = LogFactory.getLog( RemoteBundleContextImpl.class );
    /**
     * Bundle context (cannot be null).
     */
    private final transient BundleContext m_bundleContext;

    /**
     * Constructor.
     *
     * @param bundleContext bundle context (cannot be null)
     *
     * @throws IllegalArgumentException - If bundle context is null
     */
    public RemoteBundleContextImpl( final BundleContext bundleContext )
    {
        validateNotNull( bundleContext, "Bundle context" );
        m_bundleContext = bundleContext;
    }

    /**
     * {@inheritDoc}
     */
    public Object remoteCall( final Class<?> serviceType,
                              final String methodName,
                              final Class<?>[] methodParams,
                              String filter,
                              final long timeoutInMillis,
                              final Object... actualParams )
        throws NoSuchServiceException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
        LOG.info( "Remote call of [" + serviceType.getName() + "." + methodName + "]" );

        return serviceType.getMethod( methodName, methodParams ).invoke(
            getService( serviceType, filter, timeoutInMillis ),
            actualParams
        );
    }

    /**
     * {@inheritDoc}
     */
    public long installBundle( final String bundleUrl )
        throws BundleException
    {
        LOG.info( "Install bundle from URL [" + bundleUrl + "]" );
        return m_bundleContext.installBundle( bundleUrl ).getBundleId();
    }

    /**
     * {@inheritDoc}
     */
    public long installBundle( final String bundleLocation,
                               final byte[] bundle )
        throws BundleException
    {
        LOG.info( "Install bundle [" + bundleLocation + "] from byte array" );
        final ByteArrayInputStream inp = new ByteArrayInputStream( bundle );
        try
        {
            return m_bundleContext.installBundle( bundleLocation, inp ).getBundleId();
        }
        finally
        {
            try
            {
                inp.close();
            }
            catch( IOException e )
            {
                // ignore.
            }
        }
    }

    public void uninstallBundle( long id )
        throws BundleException
    {
        LOG.info( "Uninstall bundle [" + id + "] " );
        try
        {
            m_bundleContext.getBundle( id ).uninstall();
        }
        catch( BundleException e )
        {
            LOG.error( "Problem uninstalling " + id, e );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void startBundle( long bundleId )
        throws BundleException
    {
        startBundle( m_bundleContext.getBundle( bundleId ) );
    }

    /**
     * {@inheritDoc}
     */
    public void stopBundle( long bundleId )
        throws BundleException
    {
        m_bundleContext.getBundle( bundleId ).stop();
    }

    /**
     * {@inheritDoc}
     */
    public void setBundleStartLevel( long bundleId, int startLevel )
        throws RemoteException, BundleException
    {
        try
        {
            final StartLevel startLevelService = getService( StartLevel.class, null, 0 );
            startLevelService.setBundleStartLevel( m_bundleContext.getBundle( bundleId ), startLevel );
        }
        catch( NoSuchServiceException e )
        {
            throw new BundleException( "Cannot get the start level service to set bundle start level" );
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
        final Bundle bundle = m_bundleContext.getBundle( bundleId );
        if( timeoutInMillis == NO_WAIT && bundle.getState() < state )
        {
            throw new TimeoutException(
                "There is no waiting timeout set and bundle has state '" + bundleStateToString( bundle.getState() )
                + "' not '" + bundleStateToString( state ) + "' as expected"
            );
        }
        long startedTrying = System.currentTimeMillis();
        do
        {
            try
            {
                Thread.sleep( 50 );
            }
            catch( InterruptedException e )
            {
                Thread.currentThread().interrupt();
                break;
            }
        }
        while( bundle.getState() < state
               && ( timeoutInMillis == WAIT_FOREVER
                    || System.currentTimeMillis() < startedTrying + timeoutInMillis ) );
        if( bundle.getState() < state )
        {
            throw new TimeoutException(
                "Timeout passed and bundle has state '" + bundleStateToString( bundle.getState() )
                + "' not '" + bundleStateToString( state ) + "' as expected"
            );
        }
    }

    /**
     * Lookup a service in the service registry.
     *
     * @param serviceType     service class
     * @param timeoutInMillis number of milliseconds to wait for service before failing
     *                        TODO timeout is not used!
     *
     * @return a service published under the required service type
     *
     * @throws NoSuchServiceException - If service cannot be found in the service registry
     */
    private <T> T getService( final Class<T> serviceType,
                              String filter,
                              final long timeoutInMillis )
        throws NoSuchServiceException
    {
        LOG.info( "Look up service [" + serviceType.getName() + "] filter [" + filter + "], timeout in " + timeoutInMillis + " millis" );
        final ServiceReference[] ref;
        try
        {
            ref = m_bundleContext.getServiceReferences( serviceType.getName(), filter );

            if( ref != null )
            {
                LOG.info( "Found " + ref.length + " matching services for " + serviceType.getName() + " filter " + filter );

                final Object service = m_bundleContext.getService( ref[ 0 ] );
                if( service == null )
                {
                    throw new NoSuchServiceException( serviceType );
                }
                return (T) service;
            }
            else
            {
                throw new NoSuchServiceException( serviceType );
            }
        } catch( InvalidSyntaxException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Starts a bundle.
     *
     * @param bundle bundle to be started
     *
     * @throws BundleException - If bundle cannot be started
     */
    private void startBundle( final Bundle bundle )
        throws BundleException
    {
        // Don't start if bundle already active
        int bundleState = bundle.getState();
        if( bundleState == Bundle.ACTIVE )
        {
            return;
        }

        // Don't start if bundle is a fragment bundle
        Dictionary bundleHeaders = bundle.getHeaders();
        if( bundleHeaders.get( Constants.FRAGMENT_HOST ) != null )
        {
            return;
        }

        // Start bundle
        bundle.start();

        bundleState = bundle.getState();
        if( bundleState != Bundle.ACTIVE )
        {
            long bundleId = bundle.getBundleId();
            String bundleName = bundle.getSymbolicName();
            String bundleStateStr = bundleStateToString( bundleState );
            throw new BundleException(
                "Bundle (" + bundleId + ", " + bundleName + ") not started (still " + bundleStateStr + ")"
            );
        }
    }

    /**
     * Coverts a bundle state to its string form.
     *
     * @param bundleState bundle state
     *
     * @return bundle state as string
     */
    private static String bundleStateToString( int bundleState )
    {
        switch( bundleState )
        {
            case Bundle.ACTIVE:
                return "active";
            case Bundle.INSTALLED:
                return "installed";
            case Bundle.RESOLVED:
                return "resolved";
            case Bundle.STARTING:
                return "starting";
            case Bundle.STOPPING:
                return "stopping";
            case Bundle.UNINSTALLED:
                return "uninstalled";
            default:
                return "unknown (" + bundleState + ")";
        }
    }


}