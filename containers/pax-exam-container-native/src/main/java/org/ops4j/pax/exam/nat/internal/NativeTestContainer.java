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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TimeoutException;
import org.ops4j.pax.exam.options.ProvisionOption;

/**
 * @author Toni Menzel
 * @since Jan 7, 2010
 */
public class NativeTestContainer implements TestContainer {

    @Override
    public String toString()
    {
        return "NativeContainer:" + m_frameworkFactory.toString();
    }

    private static Logger LOG = LoggerFactory.getLogger( NativeTestContainer.class );
    private Framework m_framework;

    private Stack<Long> m_installed;

    final private Map<String, String> m_properties;
    final private List<ProvisionOption> m_bundles;
    final private FrameworkFactory m_frameworkFactory;
    private static final String PROBE_SIGNATURE_KEY = "Probe-Signature";
    private static final long TIMEOUT_IN_MILLIS = 5000;

    public NativeTestContainer( FrameworkFactory frameworkFactory, List<ProvisionOption> bundles, Map<String, String> properties )
    {
        m_bundles = bundles;
        m_properties = properties;
        m_frameworkFactory = frameworkFactory;
    }

    private <T> T getService( Class<T> serviceType, String filter, long timeout )
        throws TestContainerException
    {
        assert m_framework != null : "Framework should be up";
        assert serviceType != null : "serviceType not be null";

        long start = System.currentTimeMillis();

        LOG.info( "Aquiring Service " + serviceType.getName() + " " + ( filter != null ? filter : "" ) );

        do {
            try {
                ServiceReference[] reference = m_framework.getBundleContext().getServiceReferences( serviceType.getName(), filter );
                if( reference != null ) {

                    for( ServiceReference ref : reference ) {
                        return ( (T) m_framework.getBundleContext().getService( ref ) );
                    }
                }

                Thread.sleep( 200 );
            } catch( Exception e ) {
                LOG.error( "Some problem during looking up service from framework: " + m_framework, e );
            }
            // wait a bit
        } while( ( System.currentTimeMillis() ) < start + timeout );
        printAvailableAlternatives( serviceType );

        throw new TestContainerException( "Not found a matching Service " + serviceType.getName() + " for Filter:" + ( filter != null ? filter : "" ) );

    }

    private <T> void printAvailableAlternatives( Class<T> serviceType )
    {
        try {
            ServiceReference[] reference = m_framework.getBundleContext().getAllServiceReferences( serviceType.getName(), null );
            if( reference != null ) {
                LOG.warn( "Test Endpoints: " + reference.length );

                for( ServiceReference ref : reference ) {
                    LOG.warn( "Endpoint: " + ref );
                }
            }

        } catch( Exception e ) {
            LOG.error( "Some problem during looking up alternative service. ", e );
        }
    }

    public void call( TestAddress address )
        throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        String filterExpression = "(" + PROBE_SIGNATURE_KEY + "=" + address.root().identifier() + ")";
        ProbeInvoker service = getService( ProbeInvoker.class, filterExpression, TIMEOUT_IN_MILLIS );
        service.call();
    }

    public long install( InputStream stream )
    {
        try {
            if( m_installed == null ) {
                m_installed = new Stack<Long>();
            }
            Bundle b = m_framework.getBundleContext().installBundle( "local", stream );
            m_installed.push( b.getBundleId() );
            LOG.debug( "Installed bundle " + b.getSymbolicName() + " as Bundle ID " + b.getBundleId() );

            // stream.close();
            b.start();
            return b.getBundleId();
        } catch( BundleException e ) {
            e.printStackTrace();
        }
        return -1;
    }

    public void cleanup()
    {
        if( m_installed != null ) {
            while( ( !m_installed.isEmpty() ) ) {
                try {
                    Long id = m_installed.pop();
                    Bundle bundle = m_framework.getBundleContext().getBundle( id );
                    bundle.uninstall();
                    LOG.debug( "Uninstalled bundle " + id );
                } catch( BundleException e ) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setBundleStartLevel( long bundleId, int startLevel )
        throws TestContainerException
    {
        try {
            m_framework.getBundleContext().getBundle( bundleId ).start( startLevel );
        } catch( BundleException e ) {
            e.printStackTrace();
        }
    }

    public TestContainer stop()
    {
//        if (1==1) throw new RuntimeException( "stop has been called." );
        if( m_framework != null ) {

            try {
                LOG.debug( "Framework goes down.." );
                cleanup();
                m_framework.stop();
                m_framework.waitForStop( 1000 );
                m_framework = null;

            } catch( BundleException e ) {
                LOG.warn( "Problem during stopping fw.", e );
            } catch( InterruptedException e ) {
                LOG.warn( "InterruptedException during stopping fw.", e );
            }
        }
        else {
            LOG.warn( "Framework does not exist. Called start() before ? " );
        }
        return this;
    }

    public void waitForState( long bundleId, int state, long timeoutInMillis )
        throws TimeoutException
    {
        // look for a certain state in fw
    }

    public TestContainer start()
        throws TestContainerException
    {
        ClassLoader parent = null;
        try {
            final Map<String, String> p = new HashMap<String, String>( m_properties );
            String folder = p.get( "org.osgi.framework.storage" );
            if( folder == null ) {
                folder = System.getProperty( "org.osgi.framework.storage" );
            }
            if( folder == null ) {
                //folder = System.getProperty( "user.home" ) + File.separator + "osgi";
                folder = getCache();
            }
            LOG.debug( "Cache folder set to " + folder );
            FileUtils.delete( new File( folder ) );
            // load default stuff

            p.put( "org.osgi.framework.storage", folder );
            //  System.setProperty( "org.osgi.vendor.framework", "org.ops4j.pax.exam" );

            p.put( "org.osgi.framework.system.packages.extra", "org.ops4j.pax.exam;version=" + skipSnapshotFlag( Info.getPaxExamVersion() ) );

            parent = Thread.currentThread().getContextClassLoader();
            //Thread.currentThread().setContextClassLoader( null );

            m_framework = m_frameworkFactory.newFramework( p );
            m_framework.init();

            installAndStartBundles( m_framework.getBundleContext() );

            Thread.currentThread().setContextClassLoader( parent );

        } catch( Exception e ) {
            throw new TestContainerException( "Problem starting test container.", e );
        } finally {
            if( parent != null ) {
                Thread.currentThread().setContextClassLoader( parent );
            }
        }
        return this;
    }

    private void installAndStartBundles( BundleContext context )
        throws BundleException
    {

        m_framework.start();

        StartLevel sl;
        while( ( sl = (StartLevel) context.getService( context.getServiceReference( StartLevel.class.getName() ) ) ) == null ) {
            System.out.println( "Find SL.." );
        }

        if( sl == null ) {
            throw new TestContainerException( "No Startlevel Service ?" );
        }
        for( ProvisionOption bundle : m_bundles ) {
            Bundle b = null;
            LOG.info( "Install " + bundle );
            b = context.installBundle( bundle.getURL() );
            sl.setBundleStartLevel( b, getStartLevel( bundle ) );
            b.start();
        }
        sl.setStartLevel( Constants.START_LEVEL_TEST_BUNDLE );

    }

    private int getStartLevel( ProvisionOption bundle )
    {
        Integer start = bundle.getStartLevel();
        if( start == null ) {
            start = Constants.START_LEVEL_DEFAULT_PROVISION;
        }
        return start;
    }

    private String skipSnapshotFlag( String version )
    {
        int idx = version.indexOf( "-" );
        if( idx >= 0 ) {
            return version.substring( 0, idx );
        }
        else {
            return version;
        }
    }

    private String getCache()
        throws IOException
    {
        File base = new File( "target" );
        base.mkdir();
        File f = File.createTempFile( "examtest", ".dir", base );
        f.delete();
        f.mkdirs();
        LOG.info( "Caching" + " to " + f.getAbsolutePath() );
        return f.getAbsolutePath();
    }


}
