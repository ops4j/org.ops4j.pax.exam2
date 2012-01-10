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

import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.osgi.framework.Constants.FRAMEWORK_BOOTDELEGATION;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;
import org.ops4j.pax.exam.options.FrameworkStartLevelOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.ValueOption;
import org.ops4j.pax.exam.options.extra.CleanCachesOption;
import org.ops4j.pax.exam.options.extra.RepositoryOption;
import org.ops4j.pax.swissbox.framework.ServiceLookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Toni Menzel
 * @author Harald Wellmann
 * @since Jan 7, 2010
 */
public class NativeTestContainer implements TestContainer
{

    final private static Logger LOG = LoggerFactory.getLogger( NativeTestContainer.class );
    final private static String PROBE_SIGNATURE_KEY = "Probe-Signature";
    final private Stack<Long> m_installed = new Stack<Long>();

    final private FrameworkFactory m_frameworkFactory;
    private ExamSystem m_system;

    volatile Framework m_framework;

    public NativeTestContainer( ExamSystem system, FrameworkFactory frameworkFactory ) throws IOException
    {
        m_frameworkFactory = frameworkFactory;
        m_system = system;
    }

    public synchronized void call( TestAddress address )
    {
        Map<String,String> props = new HashMap<String, String>();
        props.put(PROBE_SIGNATURE_KEY, address.root().identifier());
        ProbeInvoker service = ServiceLookup.getService( m_framework.getBundleContext(), ProbeInvoker.class, props );
        service.call( address.arguments() );
    }

    public synchronized long install( String location, InputStream stream )
    {
        try
        {
            Bundle b = m_framework.getBundleContext().installBundle( location, stream );
            m_installed.push( b.getBundleId() );
            LOG.debug( "Installed bundle " + b.getSymbolicName() + " as Bundle ID " + b.getBundleId() );
            setBundleStartLevel( b.getBundleId(), Constants.START_LEVEL_TEST_BUNDLE );
            b.start();
            return b.getBundleId();
        } catch ( BundleException e )
        {
            e.printStackTrace();
        }
        return -1;
    }
    
    public synchronized long install( InputStream stream )
    {
        return install("local",stream);
    }

    public synchronized void cleanup()
    {
        while ( (!m_installed.isEmpty()) )
        {
            try
            {
                Long id = m_installed.pop();
                Bundle bundle = m_framework.getBundleContext().getBundle( id );
                bundle.uninstall();
                LOG.debug( "Uninstalled bundle " + id );
            } catch ( BundleException e )
            {
                // Sometimes bundles go mad when install + uninstall happens too
                // fast.
            }
        }
    }

    public void setBundleStartLevel( long bundleId, int startLevel ) throws TestContainerException
    {
        StartLevel sl = ServiceLookup.getService( m_framework.getBundleContext(), StartLevel.class );
        sl.setBundleStartLevel( m_framework.getBundleContext().getBundle( bundleId ), startLevel );
    }

    public TestContainer start(  ) throws TestContainerException
    {
        ClassLoader parent = null;
        try
        {
            m_system = m_system.fork( new Option[] {
                systemPackage( "org.ops4j.pax.exam;version=" + skipSnapshotFlag( Info.getPaxExamVersion() ) ),
                systemProperty( "java.protocol.handler.pkgs").value( "org.ops4j.pax.url" )
            } );
            Map<String, Object> p = createFrameworkProperties();
            if (LOG.isDebugEnabled()) {
                logFrameworkProperties( p );
                logSystemProperties();
            }
            parent = Thread.currentThread().getContextClassLoader();
            m_framework = m_frameworkFactory.newFramework( p );
            m_framework.init();
            installAndStartBundles( m_framework.getBundleContext() );
        } catch ( Exception e )
        {
            throw new TestContainerException( "Problem starting test container.", e );
        } finally
        {
            if ( parent != null )
            {
                Thread.currentThread().setContextClassLoader( parent );
            }
        }
        return this;
    }

    private void logFrameworkProperties( Map<String, Object> p )
    {
        LOG.debug( "==== Framework properties:" );
        for (String key :  p.keySet()) {
            LOG.debug( "{} = {}", key, p.get( key ) );
        }
    }

    private void logSystemProperties()
    {
        LOG.debug( "==== System properties:" );
        SortedMap<Object,Object> map = new TreeMap<Object, Object>(System.getProperties());
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            LOG.debug( "{} = {}", entry.getKey(), entry.getValue()  );
        }
    }

    public TestContainer stop()
    {
        if ( m_framework != null )
        {
            try
            {
                cleanup();
                stopOrAbort();
                m_framework = null;
                m_system.clear();
            }
            catch ( BundleException e )
            {
                LOG.warn( "Problem during stopping fw.", e );
            } 
            catch ( InterruptedException e )
            {
                LOG.warn( "InterruptedException during stopping fw.", e );
            }
        } 
        else
        {
            LOG.warn( "Framework does not exist. Called start() before ? " );
        }
        return this;
    }

    private void stopOrAbort() throws BundleException, InterruptedException
    {
        m_framework.stop();
        long timeout = m_system.getTimeout().getValue();
        Thread stopper = new Stopper( timeout );
        stopper.start();
        stopper.join(timeout + 500);
        
        // If the framework is not stopped, then we're in trouble anyway, so we do not worry 
        // about stopping the worker thread.
        
        if (m_framework.getState() != Framework.RESOLVED)
        {
            String message = "Framework has not yet stopped after " +
                    timeout + " ms. waitForStop did not return";
            throw new TestContainerException( message );            
        }
    }

    private Map<String, Object> createFrameworkProperties( ) throws IOException
    {
        final Map<String, Object> p = new HashMap<String, Object>();
        CleanCachesOption cleanCaches = m_system.getSingleOption( CleanCachesOption.class );
        if (cleanCaches != null && cleanCaches.getValue() != null && cleanCaches.getValue()) {
            p.put( FRAMEWORK_STORAGE_CLEAN,  FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT );            
        }

        p.put( FRAMEWORK_STORAGE, m_system.getTempFolder().getAbsolutePath() );
        p.put( FRAMEWORK_SYSTEMPACKAGES_EXTRA, buildString( m_system.getOptions( SystemPackageOption.class ) ) );
        p.put( FRAMEWORK_BOOTDELEGATION, buildString( m_system.getOptions( BootDelegationOption.class ) ) );
        
        for ( FrameworkPropertyOption option : m_system.getOptions( FrameworkPropertyOption.class ) )
        {
            p.put( option.getKey(), option.getValue() );
        }

        for ( SystemPropertyOption option : m_system.getOptions( SystemPropertyOption.class ) )
        {
            System.setProperty( option.getKey(), option.getValue() );
        }
        
        System.setProperty("org.ops4j.pax.url.mvn.repositories", buildString( m_system.getOptions ( RepositoryOption.class)));
        return p;
    }

    private String buildString( ValueOption<?>[] options )
    {
        return buildString( new String[0], options, new String[0] );
    }

    @SuppressWarnings("unused")
    private String buildString( String[] prepend, ValueOption<?>[] options )
    {
        return buildString( prepend, options, new String[0] );
    }

    @SuppressWarnings("unused")
    private String buildString( ValueOption<?>[] options, String[] append )
    {
        return buildString( new String[0], options, append );
    }

    private String buildString( String[] prepend, ValueOption<?>[] options, String[] append )
    {
        StringBuilder builder = new StringBuilder();
        for ( String a : prepend )
        {
            builder.append( a );
            builder.append( "," );
        }
        for ( ValueOption<?> option : options )
        {
            builder.append( option.getValue() );
            builder.append( "," );
        }
        for ( String a : append )
        {
            builder.append( a );
            builder.append( "," );
        }
        if ( builder.length() > 0 )
        {
            return builder.substring( 0, builder.length() - 1 );
        } else
        {
            return "";
        }
    }

    private void installAndStartBundles( BundleContext context ) throws BundleException
    {
        m_framework.start();
        StartLevel sl = ServiceLookup.getService(  context, StartLevel.class );
        for ( ProvisionOption<?> bundle : m_system.getOptions( ProvisionOption.class ) )
        {
            Bundle b = context.installBundle( bundle.getURL() );
            int startLevel = getStartLevel( bundle );
            sl.setBundleStartLevel( b, startLevel );
            if ( bundle.shouldStart() )  
            {
                b.start();
                LOG.debug( "+ Install (start@{}) {}", startLevel, bundle );
            }
            else
            {
                LOG.debug( "+ Install (no start) {}", bundle );
            }
        }

        int startLevel = m_system.getSingleOption( FrameworkStartLevelOption.class ).getStartLevel();
        LOG.debug( "Jump to startlevel: " + startLevel );
        sl.setStartLevel( startLevel );
        // Work around for FELIX-2942
        final CountDownLatch latch = new CountDownLatch( 1 );
        context.addFrameworkListener( new FrameworkListener()
        {
            public void frameworkEvent( FrameworkEvent frameworkEvent )
            {
                switch (frameworkEvent.getType())
                {
                case FrameworkEvent.STARTLEVEL_CHANGED :
                    latch.countDown();
                }
            }
        } );
        try
        {
            final long timeout = m_system.getTimeout().getLowerValue();
            if ( !latch.await( timeout, TimeUnit.MILLISECONDS )) {
                 // Framework start level has not reached yet, so report an error to cause the test process to abort
                final String message = "Framework is yet to reach target start level " + startLevel + " after " +
                        timeout + " ms. Current start level is " + sl.getStartLevel();
                throw new TestContainerException(message);
            }
        } catch ( InterruptedException e )
        {
            throw new TestContainerException( e );
        }
    }


    private int getStartLevel( ProvisionOption<?> bundle )
    {
        Integer start = bundle.getStartLevel();
        if ( start == null )
        {
            start = Constants.START_LEVEL_DEFAULT_PROVISION;
        }
        return start;
    }

    private String skipSnapshotFlag( String version )
    {
        int idx = version.indexOf( "-" );
        if ( idx >= 0 )
        {
            return version.substring( 0, idx );
        } else
        {
            return version;
        }
    }

    @Override
    public String toString()
    {
        return "NativeContainer:" + m_frameworkFactory.toString();
    }
    
    
    /**
     * Worker thread for shutting down the framework. 
     * We'd expect Framework.waitForStop(timeout) to return after the given timeout, but this
     * is not the case with Equinox (tested on 3.6.2 and 3.7.0), so we use this worker thread
     * to avoid blocking the main thread.
     * 
     * @author Harald Wellmann
     */
    private class Stopper extends Thread
    {
        private final long timeout;

        private Stopper( long timeout )
        {
            this.timeout = timeout;
        }

        @Override
        public void run()
        {
            try
            {
                FrameworkEvent frameworkEvent = m_framework.waitForStop( timeout );
                if( frameworkEvent.getType() != FrameworkEvent.STOPPED )
                {
                    LOG.error( "Framework has not yet stopped after {} ms. " +
                            "waitForStop returned: {}", timeout, frameworkEvent );
                }
            }
            catch ( InterruptedException exc )
            {
                LOG.error( "Stopper thread was interrupted" );
            }
        }
    }
}
