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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.container.TestContainer;
import org.ops4j.pax.exam.spi.container.TestContainerException;
import org.ops4j.pax.exam.spi.container.TimeoutException;

/**
 * @author Toni Menzel
 * @since Jan 7, 2010
 */
public class NativeTestContainer implements TestContainer
{

    final private List<String> m_bundles;

    private Framework m_framework;

    public NativeTestContainer( Option[] options )
    {
        // catch all bundles
        m_bundles = new ArrayList<String>();

    }

    public <T> T getService( Class<T> serviceType )
        throws TestContainerException
    {
        // service should appear at certain point in time
        ServiceReference reference = m_framework.getBundleContext().getServiceReference( serviceType.getName() );
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
        return 0;
    }

    public long installBundle( String bundleLocation, byte[] bundle )
        throws TestContainerException
    {
        return 0;
    }

    public long installBundle( InputStream bundleUrl )
    {
        return 0;
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
            p.put( "org.osgi.framework.storage", System.getProperty( "user.home" ) + File.separator + "osgi" );
            FrameworkFactory factory = (FrameworkFactory) Class.forName( "org.apache.felix.framework.FrameworkFactory" ).newInstance();
            m_framework = factory.newFramework( p );
            m_framework.init();
            BundleContext context = m_framework.getBundleContext();
            for( String bundle : m_bundles )
            {
                context.installBundle( bundle );
            }
            m_framework.start();
            m_framework.waitForStop( 0 );
        } catch( Exception e )
        {
            e.printStackTrace();
        }
    }
}
