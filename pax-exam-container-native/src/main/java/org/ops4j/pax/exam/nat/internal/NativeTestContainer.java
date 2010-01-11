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
import org.apache.log4j.Logger;
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
import org.ops4j.pax.exam.spi.container.TestContainerFactory;
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

    public <T> T getService( Class<T> serviceType )
        throws TestContainerException
    {
        // service should appear at certain point in time
        LOG.info( "# Framework " + m_framework.getBundleContext().getBundle().getSymbolicName() );

        for( Bundle b : m_framework.getBundleContext().getBundles() )
        {
            LOG.debug( "+ " + b.getSymbolicName() + " in state " + b.getState() );
        }
        System.out.println( "---" );
        ServiceReference reference = m_framework.getBundleContext().getServiceReference( serviceType.getName() );

        if( reference == null )
        {
            return null;
        }
        return (T) m_framework.getBundleContext().getService( reference );
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
            m_framework.stop();

        } catch( BundleException e )
        {
            e.printStackTrace();
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
        try
        {
            Map p = new HashMap();
            String folder = System.getProperty( "user.home" ) + File.separator + "osgi";
            FileUtils.delete( new File( folder ) );
            p.put( "org.osgi.framework.storage", folder );

            // org/eclipse/osgi/launch/EquinoxFactory.class
            // org.apache.felix.framework.FrameworkFactory
            FrameworkFactory factory = (FrameworkFactory) DiscoverSingleton.find( FrameworkFactory.class );

            // FrameworkFactory factory = (FrameworkFactory) Class.forName( "org.eclipse.osgi.launch.EquinoxFactory" ).newInstance();
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

        } catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
