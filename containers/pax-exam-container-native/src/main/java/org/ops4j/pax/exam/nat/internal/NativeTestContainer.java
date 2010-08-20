/*
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
package org.ops4j.pax.exam.nat.internal;

import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TimeoutException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * @author Toni Menzel
 * @since Jan 7, 2010
 */
public class NativeTestContainer implements TestContainer
{

    private static Logger LOG = LoggerFactory.getLogger( NativeTestContainer.class );
    final private List<String> m_bundles;

    private Framework m_framework;
    private Stack<Long> m_installed;

    public NativeTestContainer( ArrayList<String> bundles )
    {
        m_bundles = bundles;
    }

    public <T> T getService( Class<T> serviceType, String filter, long timeout )
        throws TestContainerException
    {
        T service = null;
        long start = System.currentTimeMillis();

        LOG.debug( "Aquiring Service " + serviceType.getName() + " " + ( filter != null ? filter : "" ) );

        do
        {
            try
            {
                ServiceReference[] reference = m_framework.getBundleContext().getServiceReferences( serviceType.getName(), filter );
                if( reference != null )
                {

                    for( ServiceReference ref : reference )
                    {
                        return ( (T) m_framework.getBundleContext().getService( ref ) );
                    }
                }

            } catch( Exception e )
            {
                LOG.error( "Some problem during looking up service. " + e );
            }
        } while( ( System.currentTimeMillis() ) < start + timeout );

        LOG.debug( "Not found a matching Service " + serviceType.getName() + " for Filter:" + ( filter != null ? filter : "" ) );

        return null;
    }

    public long install( InputStream stream )
    {

        try
        {
            if( m_installed == null )
            {
                m_installed = new Stack<Long>();
            }
            Bundle b = m_framework.getBundleContext().installBundle( "local", stream );
            m_installed.push( b.getBundleId() );
            LOG.debug( "Installed bundle " + b.getSymbolicName() + " as Bundle ID " + b.getBundleId() );

            // stream.close();
            b.start();
            return b.getBundleId();
        } catch( BundleException e )
        {
            e.printStackTrace();
        }
        return -1;
    }

    public void cleanup()
    {
        if( m_installed != null )
        {
            while( ( !m_installed.isEmpty() ) )
            {
                try
                {
                    Long id = m_installed.pop();
                    Bundle bundle = m_framework.getBundleContext().getBundle( id );
                    bundle.uninstall();
                    LOG.debug( "Uninstalled bundle " + id );
                } catch( BundleException e )
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setBundleStartLevel( long bundleId, int startLevel )
        throws TestContainerException
    {
        try
        {
            m_framework.getBundleContext().getBundle( bundleId ).start( startLevel );
        } catch( BundleException e )
        {
            e.printStackTrace();
        }
    }

    public TestContainer stop()
        throws TimeoutException
    {
        if( m_framework != null )
        {

            try
            {
                LOG.debug( "Framework goes down.." );
                m_framework.stop();
                m_framework.waitForStop( 1000 );


            } catch( BundleException e )
            {
                e.printStackTrace();
            } catch( InterruptedException e )
            {

            }
        }
        else
        {
            throw new IllegalStateException( "Framework does not exist. Called start() before ? " );
        }
        return this;
    }

    public void waitForState( long bundleId, int state, long timeoutInMillis )
        throws TimeoutException
    {
        // look for a certain state in fw
    }

    public TestContainer start()
        throws TimeoutException
    {
        ClassLoader parent = null;
        try
        {
            final Map<String, String> p = new HashMap<String, String>();
            String folder = System.getProperty( "user.home" ) + File.separator + "osgi";
            LOG.debug( "Cache folder set to " + folder );
            FileUtils.delete( new File( folder ) );
            // load default stuff

            p.put( "org.osgi.framework.storage", folder );
            //  System.setProperty( "org.osgi.vendor.framework", "org.ops4j.pax.exam" );

            p.put( "org.osgi.framework.system.packages.extra", "org.ops4j.pax.exam.raw.extender;version=" + skipSnapshotFlag( Info.getPaxExamVersion() ) );
            // TODO fix ContextClassLoaderUtils.doWithClassLoader() and replace logic with it.
            parent = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader( null );

            FrameworkFactory factory = (FrameworkFactory) DiscoverSingleton.find( FrameworkFactory.class );

            m_framework = factory.newFramework( p );

            m_framework.init();

            BundleContext context = m_framework.getBundleContext();
            for( String bundle : m_bundles )
            {
                Bundle b = context.installBundle( bundle );
                LOG.debug( "Installed bundle " + b.getSymbolicName() + " as Bundle ID " + b.getBundleId() );

            }
            m_framework.start();
            for( Bundle b : m_framework.getBundleContext().getBundles() )
            {
                b.start();
                LOG.debug( "Started: " + b.getSymbolicName() );
            }
            Thread.currentThread().setContextClassLoader( parent );

        } catch( Exception e )
        {
            e.printStackTrace();
        } finally
        {
            if( parent != null )
            {
                Thread.currentThread().setContextClassLoader( parent );

            }
        }
        return this;
    }

    private String skipSnapshotFlag( String version )
    {
        int idx = version.indexOf( "-" );
        if( idx >= 0 )
        {
            return version.substring( 0, idx );
        }
        else
        {
            return version;
        }
    }

    private FrameworkFactory getFrameworkFactory( String factoryClass )
        throws ClassNotFoundException

    {
        return null;//return (FrameworkFactory) Class.forName( factoryClass );
    }

    private String getFelixFactory()
    {
        return "org.apache.felix.framework.FrameworkFactory";
    }

    private String getEquinoxFactory()
    {
        return "org.eclipse.osgi.launch.EquinoxFactory";
    }


}
