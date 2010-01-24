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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.spi.container.TestContainer;
import org.ops4j.pax.exam.spi.container.TestContainerException;
import org.ops4j.pax.exam.spi.container.TimeoutException;

/**
 * @author Toni Menzel
 * @since Jan 7, 2010
 */
public class NativeTestContainer implements TestContainer
{

    private static final Log LOG = LogFactory.getLog( NativeTestContainer.class );
    final private List<String> m_bundles;

    private Framework m_framework;

    public NativeTestContainer( Option[] options )
    {
        // install url handlers:
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );

        // catch all bundles
        m_bundles = new ArrayList<String>();
        for( Option option : options )
        {
            if( option instanceof ProvisionOption )
            {
                m_bundles.add( ( (ProvisionOption) ( (ProvisionOption) option ) ).getURL() );
            }
        }

    }

    public <T> T getService( Class<T> serviceType, String filter )
        throws TestContainerException
    {
        List<T> services = getServices( serviceType, filter );
        if( services == null )
        {
            return null;
        }
        else
        {
            return services.get( 0 );
        }
    }

    public <T> List<T> getServices( Class<T> serviceType, String filter )
        throws TestContainerException
    {
        try
        {
            ServiceReference[] reference = m_framework.getBundleContext().getServiceReferences( serviceType.getName(), filter );
            if( reference == null )
            {
                return null;
            }

            List<T> res = new ArrayList<T>();
            for( ServiceReference ref : reference )
            {
                res.add( (T) m_framework.getBundleContext().getService( ref ) );
            }

            return res;

        } catch( Exception e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T getService( Class<T> serviceType )
        throws TestContainerException
    {
        try
        {
            // service should appear at certain point in time
            LOG.info( "# Framework " + m_framework.getBundleContext().getBundle().getSymbolicName() );

            for( Bundle b : m_framework.getBundleContext().getBundles() )
            {
                LOG.debug( "+ " + b.getSymbolicName() + " in state " + b.getState() );
            }
            System.out.println( "---" );
            Thread.sleep( 1000 );
            System.out.println( "looking for " + serviceType.getName() );
            BundleContext ctx = m_framework.getBundleContext();
            ServiceReference[] reference3 = ctx.getServiceReferences( null, null );

            ServiceReference[] reference = ctx.getServiceReferences( serviceType.getName(), "(objectClass=*)" );

            if( reference == null )
            {
                return null;
            }
            return (T) m_framework.getBundleContext().getService( reference[ 0 ] );

        } catch( Exception e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T getService( Class<T> serviceType, long timeoutInMillis )
        throws TestContainerException
    {
        return getService( serviceType );
    }

    public long installBundle( String bundleUrl )
        throws TestContainerException
    {
        try
        {
            return installBundle( bundleUrl, new URL( bundleUrl ).openStream() );
        } catch( IOException e )
        {
            throw new TestContainerException( e );
        }
    }

    public long installBundle( InputStream inp )
        throws TestContainerException
    {
        long time = System.nanoTime();
        return installBundle( "PaxExamAdhocBundle" + time, inp );
    }

    public long installBundle( String bundleLocation, byte[] bundle )
        throws TestContainerException
    {
        return installBundle( bundleLocation, new ByteArrayInputStream( bundle ) );
    }

    public long installBundle( String location, InputStream stream )
    {
        try
        {
            Bundle b = m_framework.getBundleContext().installBundle( location, stream );
            LOG.debug( "Installed bundle " + location + " as Bundle ID " + b.getBundleId() );

            // stream.close();
            b.start();
            return b.getBundleId();
        } catch( BundleException e )
        {
            e.printStackTrace();
        }
        return -1;
    }

    public void startBundle( long bundleId )
        throws TestContainerException
    {

    }

    public void stopBundle( long bundleId )
        throws TestContainerException
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setBundleStartLevel( long bundleId, int startLevel )
        throws TestContainerException
    {
        // overwrite startlevels of bundles to start.
    }

    public void stop()
        throws TimeoutException
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

    public void waitForState( long bundleId, int state, long timeoutInMillis )
        throws TimeoutException
    {
        // look for a certain state in fw
    }

    public void start()
        throws TimeoutException
    {
        ClassLoader parent = null;
        try
        {
            final Map<String, String> p = new HashMap<String, String>();
            String folder = System.getProperty( "user.home" ) + File.separator + "osgi";
            FileUtils.delete( new File( folder ) );
            p.put( "org.osgi.framework.storage", folder );
            p.put( "org.osgi.framework.system.packages.extra", "org.ops4j.pax.exam.raw.extender;version=2.0.0.SNAPSHOT" );

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

    public <T> List<T> getServices( Class<T> serviceType, String filter, long timeoutInMillis )
        throws TestContainerException
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
