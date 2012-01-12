/*
 * Copyright 2011 Harald Wellmann.
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
package org.ops4j.pax.exam.forked;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.osgi.framework.Constants.FRAMEWORK_BOOTDELEGATION;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.forked.provision.PlatformImpl;
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;
import org.ops4j.pax.exam.options.FrameworkStartLevelOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.ValueOption;
import org.ops4j.pax.exam.options.extra.RepositoryOption;
import org.ops4j.pax.exam.options.extra.VMOption;
import org.ops4j.pax.exam.options.extra.WorkingDirectoryOption;
import org.ops4j.pax.swissbox.framework.RemoteFramework;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TestContainer} which launches an OSGi framework in a forked Java VM to isolate the
 * framework parent class loader from the application class loader containing Pax Exam and
 * additional user classes.
 * <p>
 * The drawback of this container is that remote debugging is required to debug the tests executed
 * by the forked framework.
 * <p>
 * TODO support additional Exam options
 * 
 * @author Harald Wellmann
 * 
 */
public class ForkedTestContainer implements TestContainer
{
    private static Logger LOG = LoggerFactory.getLogger( ForkedTestContainer.class );

    private ExamSystem system;
    private ForkedFrameworkFactory frameworkFactory;
    private RemoteFramework remoteFramework;
    private PlatformImpl platform;

    public ForkedTestContainer( ExamSystem system, FrameworkFactory frameworkFactory )
    {
        this.system = system;
        this.frameworkFactory = new ForkedFrameworkFactory( frameworkFactory );
        this.platform = new PlatformImpl();
    }

    public void call( TestAddress address )
    {
        String filterExpression =
            "(&(objectClass=org.ops4j.pax.exam.ProbeInvoker)(Probe-Signature="
                    + address.root().identifier() + "))";
        try
        {
            remoteFramework.callService( filterExpression, "call" );
        }
        catch ( RemoteException exc )
        {
            throw new TestContainerException( exc );
        }
        catch ( BundleException exc )
        {
            throw new TestContainerException( exc );
        }
    }

    public long install( String location, InputStream stream )
    {
        try
        {
            return remoteFramework.installBundle( location );
        }
        catch ( RemoteException exc )
        {
            throw new TestContainerException( exc );
        }
        catch ( BundleException exc )
        {
            throw new TestContainerException( exc );
        }
    }

    public long install( InputStream stream )
    {
        try
        {
            long bundleId = remoteFramework.installBundle( "local", pack( stream ) );
            remoteFramework.startBundle( bundleId );
            return bundleId;
        }
        catch ( RemoteException exc )
        {
            throw new TestContainerException( exc );
        }
        catch ( BundleException exc )
        {
            throw new TestContainerException( exc );
        }
    }

    public TestContainer start()
    {
        try
        {
            system = system.fork( new Option[]{
                systemProperty( "java.protocol.handler.pkgs" ).value( "org.ops4j.pax.url" )
            } );
            List<String> vmArgs = createVmArguments();
            Map<String, String> systemProperties = createSystemProperties();
            Map<String, Object> frameworkProperties = createFrameworkProperties();
            remoteFramework = frameworkFactory.fork( vmArgs, systemProperties, frameworkProperties );
            remoteFramework.init();
            installAndStartBundles();
        }
        catch ( BundleException exc )
        {
            throw new TestContainerException( exc );
        }
        catch ( IOException exc )
        {
            throw new TestContainerException( exc );
        }
        catch ( InterruptedException exc )
        {
            throw new TestContainerException( exc );
        }
        catch ( NotBoundException exc )
        {
            throw new TestContainerException( exc );
        }
        return this;
    }

    public TestContainer stop()
    {
        try
        {
            remoteFramework.stop();
        }
        catch ( RemoteException exc )
        {
            throw new TestContainerException( exc );
        }
        catch ( BundleException exc )
        {
            throw new TestContainerException( exc );
        }
        frameworkFactory.join();
        return this;
    }

    private byte[] pack( InputStream stream )
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            StreamUtils.copyStream( stream, out, true );
        }
        catch ( IOException e )
        {

        }
        return out.toByteArray();
    }

    private Map<String, Object> createFrameworkProperties() throws IOException
    {
        final Map<String, Object> p = new HashMap<String, Object>();
        p.put( FRAMEWORK_STORAGE, system.getTempFolder().getAbsolutePath() );
        SystemPackageOption[] systemPackageOptions = system.getOptions( SystemPackageOption.class );
        if (systemPackageOptions.length > 0) {
            p.put( FRAMEWORK_SYSTEMPACKAGES_EXTRA, buildString( systemPackageOptions ) );
        }
        p.put( FRAMEWORK_BOOTDELEGATION,
            buildString( system.getOptions( BootDelegationOption.class ) ) );

        for ( FrameworkPropertyOption option : system.getOptions( FrameworkPropertyOption.class ) )
        {
            p.put( option.getKey(), option.getValue() );
        }

        for ( SystemPropertyOption option : system.getOptions( SystemPropertyOption.class ) )
        {
            System.setProperty( option.getKey(), option.getValue() );
        }
        return p;
    }
    
    private List<String> createVmArguments()
    {
        VMOption[] options = system.getOptions( VMOption.class );
        List<String> args = new ArrayList<String>();
        for (VMOption option : options)
        {
            args.add(option.getOption());
        }
        return args;
        
    }

    private Map<String, String> createSystemProperties()
    {
        Map<String, String> p = new HashMap<String, String>();
        for ( SystemPropertyOption option : system.getOptions( SystemPropertyOption.class ) )
        {
            p.put( option.getKey(), option.getValue() );
        }
        
        RepositoryOption[] repositories = system.getOptions ( RepositoryOption.class);
        if (repositories.length != 0)
        {
            System.setProperty("org.ops4j.pax.url.mvn.repositories", buildString( repositories ));
        }
        
        return p;
    }

    private String buildString( ValueOption<?>[] options )
    {
        return buildString( new String[0], options, new String[0] );
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
        if( builder.length() > 0 )
        {
            return builder.substring( 0, builder.length() - 1 );
        }
        else
        {
            return "";
        }
    }

    private void installAndStartBundles() throws BundleException, RemoteException
    {
        remoteFramework.start();
        for ( ProvisionOption<?> bundle : system.getOptions( ProvisionOption.class ) )
        {
            String localUrl = downloadBundle( bundle.getURL() );
            long bundleId = remoteFramework.installBundle( localUrl );
            int startLevel = getStartLevel( bundle );
            remoteFramework.setBundleStartLevel( bundleId, startLevel );
            if( bundle.shouldStart() )
            {
                remoteFramework.startBundle( bundleId );
                LOG.debug( "+ Install (start@{}) {}", startLevel, bundle );
            }
            else
            {
                LOG.debug( "+ Install (no start) {}", bundle );
            }
        }

        int startLevel = system.getSingleOption( FrameworkStartLevelOption.class ).getStartLevel();
        LOG.debug( "Jump to startlevel: " + startLevel );
        remoteFramework.setFrameworkStartLevel( startLevel );
        
        // FIXME listen for a startup event instead of sleeping
        try
        {
            Thread.sleep( 1000 );
        }
        catch ( InterruptedException exc )
        {
            throw new TestContainerException( exc );
        }
    }

    private String downloadBundle( String url )
    {
        try
        {
            URL realUrl = new URL( url );
            if (realUrl.getProtocol().equals( "reference" )) {
                return url;
            }
            File localBundle =
                platform.download( system.getTempFolder(), realUrl, url, false, true, true, false );
            return localBundle.toURI().toURL().toString();
        }
        catch ( MalformedURLException exc )
        {
            throw new TestContainerException( exc );
        }
    }

    private int getStartLevel( ProvisionOption<?> bundle )
    {
        Integer start = bundle.getStartLevel();
        if( start == null )
        {
            start = org.ops4j.pax.exam.Constants.START_LEVEL_DEFAULT_PROVISION;
        }
        return start;
    }
}
