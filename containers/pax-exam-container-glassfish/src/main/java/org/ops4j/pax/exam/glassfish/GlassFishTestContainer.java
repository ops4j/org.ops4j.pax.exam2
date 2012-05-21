/*
 * Copyright 2012 Harald Wellmann
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
package org.ops4j.pax.exam.glassfish;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemPackages;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.swissbox.framework.ServiceLookup.getService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogManager;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.ops4j.io.FileUtils;
import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.ProbeInvokerFactory;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDirectory;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.glassfish.zip.ZipInstaller;
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;
import org.ops4j.pax.exam.options.FrameworkStartLevelOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.UrlDeploymentOption;
import org.ops4j.pax.exam.options.ValueOption;
import org.ops4j.pax.exam.util.PathUtils;
import org.ops4j.spi.ServiceProviderFinder;
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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Harald Wellmann
 * @since Jan 2012
 */
public class GlassFishTestContainer implements TestContainer
{
    // TODO make this configurable
    // The only reason for not using full profile is reduced download time ;-)
    public static final String GLASSFISH_WEB_DISTRIBUTION_URL =
        "mvn:org.glassfish.main.distributions/web/3.1.2/zip";

    private static final Logger LOG = LoggerFactory.getLogger( GlassFishTestContainer.class );
    private static final String PROBE_SIGNATURE_KEY = "Probe-Signature";
    private static final String PROBE_APPLICATION_NAME = "Pax-Exam-Probe";
    private static final String HTTP_PORT_XPATH = "/domain/configs/config/network-config/network-listeners/network-listener[@name='http-listener-1']/@port";

    
    private Stack<Long> installed = new Stack<Long>();
    private Stack<String> deployed = new Stack<String>();

    private FrameworkFactory frameworkFactory;
    private ExamSystem system;

    private Framework framework;
    private StartLevel sl;
    private GlassFish glassFish;
    private String glassFishHome;
    private BundleContext bc;

    private boolean isJavaEE;

    private GlassFishRuntime glassFishRuntime;

    private TestDirectory testDirectory;

    private File configTarget;

    public GlassFishTestContainer( ExamSystem system, FrameworkFactory frameworkFactory )
    {
        this.frameworkFactory = frameworkFactory;
        this.system = system;
        this.testDirectory = TestDirectory.getInstance();
        
    }

    public synchronized void call( TestAddress address )
    {
        if( isJavaEE )
        {
            TestInstantiationInstruction instruction = testDirectory.lookup( address );
            ProbeInvokerFactory probeInvokerFactory =
                ServiceProviderFinder.loadUniqueServiceProvider( ProbeInvokerFactory.class );
            ProbeInvoker invoker =
                probeInvokerFactory.createProbeInvoker( null, instruction.toString() );
            invoker.call( address.arguments() );
        }
        else
        {
            Map<String, String> filterProps = new HashMap<String, String>();
            filterProps.put( PROBE_SIGNATURE_KEY, address.root().identifier() );
            ProbeInvoker service =
                getService( framework.getBundleContext(), ProbeInvoker.class, filterProps );
            service.call( address.arguments() );
        }
    }

    public synchronized long install( String location, InputStream stream )
    {
        if (isJavaEE)
        {
            try
            {
                LOG.info( "installing probe from stream" );
                Deployer deployer = glassFish.getDeployer();
                File tempFile = File.createTempFile( "pax-exam", ".war" );
                tempFile.deleteOnExit();
                StreamUtils.copyStream( stream, new FileOutputStream( tempFile ), true );
                deployer.deploy( tempFile, "--name", PROBE_APPLICATION_NAME, "--contextroot", PROBE_APPLICATION_NAME );
//                deployer.deploy( stream, "--name", "Pax-Exam-Probe", "--contextroot", "Pax-Exam-Probe"  );
                deployed.push( PROBE_APPLICATION_NAME );
            }
            catch ( GlassFishException exc )            
            {
                throw new TestContainerException( exc );
            }
            catch ( IOException exc )
            {
                throw new TestContainerException( exc );
            }
            return -1;
        }
        try
        {
            Bundle b = framework.getBundleContext().installBundle( location, stream );
            installed.push( b.getBundleId() );
            LOG.debug( "Installed bundle " + b.getSymbolicName() + " as Bundle ID "
                    + b.getBundleId() );
            setBundleStartLevel( b.getBundleId(), Constants.START_LEVEL_TEST_BUNDLE );
            b.start();
            return b.getBundleId();
        }
        catch ( BundleException e )
        {
            e.printStackTrace();
        }
        return -1;
    }

    public synchronized long install( InputStream stream )
    {
        return install( "local", stream );
    }

    public void deployModules()
    {
        UrlDeploymentOption[] deploymentOptions = system.getOptions( UrlDeploymentOption.class );
        int numModules = 0;
        for( UrlDeploymentOption option : deploymentOptions )
        {
            numModules++;
            if (option.getName() == null) {
                option.name( "app" + numModules );
            }            
            deployModule( option );
        }
    }

    private void deployModule( UrlDeploymentOption option )
    {
        try
        {
            String url = option.getURL();
            LOG.info( "deploying module {}", url );
            URI uri = new URL( url ).toURI();
            String applicationName = option.getName();
            String contextRoot = option.getContextRoot();
            if (contextRoot == null) {
                contextRoot = applicationName;
            }
            Deployer deployer = glassFish.getDeployer();
            deployer.deploy( uri, "--name", applicationName, "--contextroot", applicationName );
            deployed.push( applicationName );
            LOG.info( "deployed module {}", url );
        }
        catch ( IOException exc )
        {
            throw new TestContainerException( exc );
        }
        catch ( GlassFishException exc )
        {
            throw new TestContainerException( exc );
        }
        catch ( URISyntaxException exc )
        {
            throw new TestContainerException( exc );
        }
    }

    public synchronized void cleanup()
    {
        undeployModules();
        try {
            glassFish.stop();       
            glassFishRuntime.shutdown();
        }
        catch ( GlassFishException exc )
        {
            throw new TestContainerException( exc );
        }
        while( !installed.isEmpty() )
        {
            try
            {
                Long id = installed.pop();
                Bundle bundle = framework.getBundleContext().getBundle( id );
                bundle.uninstall();
                LOG.debug( "Uninstalled bundle " + id );
            }
            catch ( BundleException e )
            {
                // Sometimes bundles go mad when install + uninstall happens too
                // fast.
            }
        }
    }

    private void undeployModules()
    {
        try
        {
            Deployer deployer = glassFish.getDeployer();
            while (! deployed.isEmpty())
            {
                String applicationName = deployed.pop();
                deployer.undeploy( applicationName );
            }
        }
        catch ( GlassFishException exc )
        {
            throw new TestContainerException( exc );
        }
    }

    public void setBundleStartLevel( long bundleId, int startLevel ) throws TestContainerException
    {
        BundleContext context = framework.getBundleContext();
        StartLevel sl = getService( context, StartLevel.class );
        sl.setBundleStartLevel( context.getBundle( bundleId ), startLevel );
    }

    public TestContainer start() throws TestContainerException
    {
        try
        {
            installContainer();
            system = system.fork( buildContainerOptions() );
            Map<String, Object> p = createFrameworkProperties();
            if( LOG.isDebugEnabled() )
            {
                logFrameworkProperties( p );
                logSystemProperties();
            }
            framework = frameworkFactory.newFramework( p );
            framework.start();
            bc = framework.getBundleContext();
            sl = getService( bc, StartLevel.class );

            Option[] earlyOptions = options(
                url( "file:" + glassFishHome + "/glassfish/modules/glassfish.jar" ).startLevel( 1 )
                );

            LogManager.getLogManager().readConfiguration();
            List<Bundle> bundles = new ArrayList<Bundle>();
            for( Option option : earlyOptions )
            {
                ProvisionOption<?> bundle = (ProvisionOption<?>) option;
                Bundle b = bc.installBundle( bundle.getURL() );
                installed.push( b.getBundleId() );
                bundles.add( b );
                int startLevel = getStartLevel( bundle );
                sl.setBundleStartLevel( b, startLevel );
            }
            for( Bundle bundle : bundles )
            {
                bundle.start();
            }

            glassFish = getService( bc, GlassFish.class );
            glassFishRuntime = glassFish.getService( GlassFishRuntime.class );
            String portNumber = getPortNumber( configTarget );
            testDirectory.setAccessPoint( new URI("http://localhost:" + portNumber + "/Pax-Exam-Probe/" ) );
            installAndStartBundles( bc );

            deployModules();
        }
        catch ( Exception e )
        {
            throw new TestContainerException( "Problem starting test container.", e );
        }
        return this;
    }

    private void installContainer() throws IOException
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        ConfigurationManager cm = new ConfigurationManager();
        String systemType = cm.getProperty( Constants.EXAM_SYSTEM_KEY );
        isJavaEE = Constants.EXAM_SYSTEM_JAVAEE.equals( systemType );
        glassFishHome = cm.getProperty( "pax.exam.server.home" );
        if( glassFishHome == null )
        {
            throw new TestContainerException(
                "System property pax.exam.server.home must be set to GlassFish install root" );
        }
        File gfHome = new File( glassFishHome );
        configTarget = new File( glassFishHome, "glassfish/domains/domain1/config/domain.xml" );
        File installDir = gfHome;
        if( installDir.exists() )
        {
            File bootBundle = new File( installDir, "glassfish/modules/glassfish.jar" );
            if( bootBundle.exists() )
            {
                LOG.info( "using GlassFish installation in {}", glassFishHome );
            }
            else
            {
                String msg =
                    String.format( "%s exists, but %s does not. " +
                            "This does not look like a valid GlassFish installation.",
                        glassFishHome, bootBundle );
                throw new TestContainerException( msg );
            }
        }
        else
        {
            LOG.info( "installing GlassFish in {}", glassFishHome );
            URL url = new URL( GLASSFISH_WEB_DISTRIBUTION_URL );
            File gfParent = gfHome.getParentFile();
            File tempInstall = new File( gfParent, UUID.randomUUID().toString() );
            ZipInstaller installer = new ZipInstaller( url, tempInstall.getAbsolutePath() );
            installer.downloadAndInstall();
            new File( tempInstall, "glassfish3" ).renameTo( gfHome );

            installConfiguration();
        }
    }

    private void installConfiguration()
    {
        File configSource = new File( PathUtils.getBaseDir(), "src/test/resources/domain.xml" );
        if( configSource.exists() )
        {
            try
            {
                FileUtils.copyFile( configSource, configTarget, null );
            }
            catch ( IOException exc )
            {
                throw new TestContainerException( "error copying GlassFish domain.xml", exc );
            }
        }
    }

    /**
     * Reads the first port number from the domain.xml configuration.
     * @param domainConfig
     * @return
     */
    private String getPortNumber(File domainConfig) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(domainConfig);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xPath = xpf.newXPath();
            String port = xPath.evaluate(HTTP_PORT_XPATH, doc);
            return port;
        }
        catch (ParserConfigurationException exc) {
            throw new IllegalArgumentException(exc);
        }
        catch (SAXException exc) {
            throw new IllegalArgumentException(exc);
        }
        catch (IOException exc) {
            throw new IllegalArgumentException(exc);
        }
        catch (XPathExpressionException exc) {
            throw new IllegalArgumentException(exc);
        }
    }
  
    
    private Option[] buildContainerOptions()
    {
        return new Option[]{
            systemPackages(
                "org.ops4j.pax.exam;version=" + skipSnapshotFlag( Info.getPaxExamVersion() ),
                "org.ops4j.pax.exam.options;version=" + skipSnapshotFlag( Info.getPaxExamVersion() ),
                "org.ops4j.pax.exam.util;version=" + skipSnapshotFlag( Info.getPaxExamVersion() ),
                "org.glassfish.embeddable;version=3.1",
                "org.glassfish.embeddable.spi;version=3.1" ),
            systemProperty( "java.protocol.handler.pkgs" ).value( "org.ops4j.pax.url" ),
            systemProperty( "com.sun.aas.installRoot" ).value( glassFishHome + "/glassfish" ),
            systemProperty( "com.sun.aas.instanceRoot" ).value(
                glassFishHome + "/glassfish/domains/domain1" ),
            systemProperty( "java.util.logging.config.file" ).value(
                PathUtils.getBaseDir() + "/src/test/resources/logging.properties" ),
            systemProperty( "logback.configurationFile" ).value(
                "file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml" ),
            systemProperty( "GlassFish_Platform" ).value( "Equinox" ),
            frameworkProperty( "org.osgi.framework.bundle.parent" ).value( "framework" ),
            frameworkProperty( "osgi.resolver.preferSystemPackages" ).value( "false" ),
            frameworkProperty( "osgi.compatibility.bootdelegation" ).value( "false" ),
            frameworkStartLevel( START_LEVEL_TEST_BUNDLE ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.exam.inject.link" )
                .startLevel( START_LEVEL_TEST_BUNDLE ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.extender.service.link" )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.ops4j.base.link" ).startLevel(
                START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.core.link" ).startLevel(
                START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.extender.link" ).startLevel(
                START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.framework.link" )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.lifecycle.link" )
               .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/ch.qos.logback.classic.link" )
               .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/ch.qos.logback.core.link" )
               .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            url( "link:classpath:META-INF/links/org.slf4j.api.link" )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
        };
    }

    private void logFrameworkProperties( Map<String, Object> p )
    {
        LOG.debug( "==== Framework properties:" );
        for( String key : p.keySet() )
        {
            LOG.debug( "{} = {}", key, p.get( key ) );
        }
    }

    private void logSystemProperties()
    {
        LOG.debug( "==== System properties:" );
        SortedMap<Object, Object> map = new TreeMap<Object, Object>( System.getProperties() );
        for( Map.Entry<Object, Object> entry : map.entrySet() )
        {
            LOG.debug( "{} = {}", entry.getKey(), entry.getValue() );
        }
    }

    public TestContainer stop()
    {
        if( framework != null )
        {
            try
            {
                cleanup();
                stopOrAbort();
                framework = null;
                system.clear();
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
        framework.stop();
        long timeout = system.getTimeout().getValue();
        Thread stopper = new Stopper( timeout );
        stopper.start();
        stopper.join( timeout + 500 );

        // If the framework is not stopped, then we're in trouble anyway, so we do not worry
        // about stopping the worker thread.

        if( framework.getState() != Framework.RESOLVED )
        {
            String message = "Framework has not yet stopped after " +
                    timeout + " ms. waitForStop did not return";
            throw new TestContainerException( message );
        }
    }

    private Map<String, Object> createFrameworkProperties() throws IOException
    {
        final Map<String, Object> p = new HashMap<String, Object>();
        p.put( org.osgi.framework.Constants.FRAMEWORK_STORAGE, system.getTempFolder()
            .getAbsolutePath() );
        p.put( org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
            buildString( system.getOptions( SystemPackageOption.class ) ) );
        p.put( org.osgi.framework.Constants.FRAMEWORK_BOOTDELEGATION,
            buildString( system.getOptions( BootDelegationOption.class ) ) );

        for( FrameworkPropertyOption option : system.getOptions( FrameworkPropertyOption.class ) )
        {
            p.put( option.getKey(), option.getValue() );
        }

        for( SystemPropertyOption option : system.getOptions( SystemPropertyOption.class ) )
        {
            System.setProperty( option.getKey(), option.getValue() );
        }
        return p;
    }

    private String buildString( ValueOption<?>[] options )
    {
        return buildString( new String[0], options, new String[0] );
    }

    @SuppressWarnings( "unused" )
    private String buildString( String[] prepend, ValueOption<?>[] options )
    {
        return buildString( prepend, options, new String[0] );
    }

    @SuppressWarnings( "unused" )
    private String buildString( ValueOption<?>[] options, String[] append )
    {
        return buildString( new String[0], options, append );
    }

    private String buildString( String[] prepend, ValueOption<?>[] options, String[] append )
    {
        StringBuilder builder = new StringBuilder();
        for( String a : prepend )
        {
            builder.append( a );
            builder.append( "," );
        }
        for( ValueOption<?> option : options )
        {
            builder.append( option.getValue() );
            builder.append( "," );
        }
        for( String a : append )
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

    private void installAndStartBundles( BundleContext context ) throws BundleException
    {
        framework.start();
        for( ProvisionOption<?> bundle : system.getOptions( ProvisionOption.class ) )
        {
            installAndStartBundle( context, bundle );
        }

        int startLevel = system.getSingleOption( FrameworkStartLevelOption.class ).getStartLevel();
        LOG.debug( "Jump to startlevel: " + startLevel );
        sl.setStartLevel( startLevel );
        // Work around for FELIX-2942
        final CountDownLatch latch = new CountDownLatch( 1 );
        context.addFrameworkListener( new FrameworkListener()
        {
            public void frameworkEvent( FrameworkEvent frameworkEvent )
            {
                switch( frameworkEvent.getType() )
                {
                    case FrameworkEvent.STARTLEVEL_CHANGED:
                        latch.countDown();
                }
            }
        } );
        try
        {
            final long timeout = system.getTimeout().getLowerValue();
            if( !latch.await( timeout, TimeUnit.MILLISECONDS ) )
            {
                // Framework start level has not reached yet, so report an error to cause the test
                // process to abort
                final String message =
                    "Framework is yet to reach target start level " + startLevel + " after " +
                            timeout + " ms. Current start level is " + sl.getStartLevel();
                throw new TestContainerException( message );
            }
        }
        catch ( InterruptedException e )
        {
            throw new TestContainerException( e );
        }
    }

    private void installAndStartBundle( BundleContext context, ProvisionOption<?> bundle )
        throws BundleException
    {
        Bundle b = context.installBundle( bundle.getURL() );
        int startLevel = getStartLevel( bundle );
        sl.setBundleStartLevel( b, startLevel );
        if( bundle.shouldStart() )
        {
            b.start();
            LOG.debug( "+ Install (start@{}) {}", startLevel, bundle );
        }
        else
        {
            LOG.debug( "+ Install (no start) {}", bundle );
        }
    }

    private int getStartLevel( ProvisionOption<?> bundle )
    {
        Integer start = bundle.getStartLevel();
        if( start == null )
        {
            start = Constants.START_LEVEL_DEFAULT_PROVISION;
        }
        return start;
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

    @Override
    public String toString()
    {
        return "GlassFishContainer:" + frameworkFactory.toString();
    }

    /**
     * Worker thread for shutting down the framework. We'd expect Framework.waitForStop(timeout) to
     * return after the given timeout, but this is not the case with Equinox (tested on 3.6.2 and
     * 3.7.0), so we use this worker thread to avoid blocking the main thread.
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
                FrameworkEvent frameworkEvent = framework.waitForStop( timeout );
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
