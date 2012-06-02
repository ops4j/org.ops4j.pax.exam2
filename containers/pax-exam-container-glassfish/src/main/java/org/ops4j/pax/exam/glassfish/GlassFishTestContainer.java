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
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;
import org.ops4j.pax.exam.options.FrameworkStartLevelOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.UrlDeploymentOption;
import org.ops4j.pax.exam.options.ValueOption;
import org.ops4j.pax.exam.util.PathUtils;
import org.ops4j.pax.exam.zip.ZipInstaller;
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
 * A {@link TestContainer} for the GlassFish 3.1 Java EE 6 application server.
 * <p>
 * This container support both OSGi and Java EE modes. You can provision OSGi bundles and deploy WAR
 * configuration modules via Pax Exam options.
 * <p>
 * The test probe is an OSGi bundle in OSGi mode, built by TinyBundles from the root directory of
 * the current test class. In Java EE mode, the probe is a WAR built on the fly from the classpath
 * contents with some default exclusions.
 * <p>
 * GlassFish logging is redirected from java.util.logging to SLF4J. The necessary artifacts are
 * provisioned by this container automatically.
 * <p>
 * The implementation is based on the Native Test Container. This container is launched in the
 * following steps:
 * <ul>
 * <li>Download and install GlassFish, if not present in the directory indicated by
 * pax.exam.glassfish.home.</li>
 * <li>Launch OSGi framework.</li>
 * <li>Install and start GlassFish bootstrap bundle.</li>
 * <li>Install and start user bundles and deploy user modules.</li>
 * </ul>
 * <p>
 * TODO Support Felix. At the moment, this container has only been tested on Equinox.
 * 
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class GlassFishTestContainer implements TestContainer
{
    // TODO make this configurable
    public static final String GLASSFISH_DISTRIBUTION_URL =
        "mvn:org.glassfish.main.distributions/glassfish/3.1.2/zip";

    /** Configuration property key for GlassFish installation directory. */
    public static final String GLASSFISH_HOME_KEY = "pax.exam.glassfish.home";

    /**
     * Configuration property key for GlassFish installation configuration file directory. The files
     * contained in this directory will be copied to the config directory of the GlassFish instance.
     */
    public static final String GLASSFISH_CONFIG_DIR_KEY = "pax.exam.glassfish.config.dir";

    private static final Logger LOG = LoggerFactory.getLogger( GlassFishTestContainer.class );

    /**
     * Probe service property key. In OSGi mode, each test method is wrapped in a probe invoker
     * service with a given signature property.
     */
    private static final String PROBE_SIGNATURE_KEY = "Probe-Signature";

    /**
     * Name of the probe web application (in Java EE mode).
     */
    private static final String PROBE_APPLICATION_NAME = "Pax-Exam-Probe";

    /**
     * XPath to read the HTTP port from the domain.xml configuration file.
     */
    private static final String HTTP_PORT_XPATH =
        "/domain/configs/config/network-config/network-listeners/network-listener[@name='http-listener-1']/@port";

    /**
     * Stack of installed bundles. On shutdown, the bundles are uninstalled in reverse order.
     */
    private Stack<Long> installed = new Stack<Long>();

    /**
     * Stack of deployed modules. On shutdown, the modules are undeployed in reverse order.
     */
    private Stack<String> deployed = new Stack<String>();

    /**
     * OSGi framework factory located via Java SE ServiceLoader.
     */
    private FrameworkFactory frameworkFactory;

    /**
     * Pax Exam system with configuration options.
     */
    private ExamSystem system;

    /**
     * OSGi framework.
     */
    private Framework framework;

    /**
     * OSGi Start level service.
     */
    private StartLevel sl;

    /**
     * GlassFish OSGi service.
     */
    private GlassFish glassFish;

    /**
     * GlassFish installation directory.
     */
    private String glassFishHome;

    /**
     * Bundle context of OSGi framework.
     */
    private BundleContext bc;

    /**
     * Are we running in Java EE mode (set in Exam configuration).
     */
    private boolean isJavaEE;

    /**
     * GlassFish runtime, obtained from GlassFish OSGi service.
     */
    private GlassFishRuntime glassFishRuntime;

    /**
     * Test directory which tracks all tests in the current suite. We need to register the context
     * URL of the probe web app as access point.
     */
    private TestDirectory testDirectory;

    /**
     * Copy of domain.xml in the GlassFish installation area.
     */
    private File configTarget;

    private ConfigurationManager cm;

    private String configDirName;

    /**
     * Creates a GlassFish container, running on top of an OSGi framework.
     * 
     * @param system Pax Exam system configuration
     * @param frameworkFactory OSGi framework factory.
     */
    public GlassFishTestContainer( ExamSystem system, FrameworkFactory frameworkFactory )
    {
        this.frameworkFactory = frameworkFactory;
        this.system = system;
        this.testDirectory = TestDirectory.getInstance();

    }

    /**
     * Calls a test with the given address. In OSGi mode, this works just as in the Native
     * Container. In Java EE mode, we lookup the test from the test directory and invoke it via
     * probe invoker obtained from the Java SE service loader. (This invoker uses a servlet bridge,)
     */
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

    /**
     * Installs a probe in the test container. In OSGi mode, this is a bundle which we can directly
     * install from the given stream.
     * <p>
     * In Java EE mode, the probe is a WAR, enriched by the Pax Exam servlet bridge which allows us
     * to invoke tests running within the container via an HTTP client.
     * 
     * @param location bundle location, not used for WAR probes
     * @param stream input stream containing probe
     * @return bundle ID, or -1 for WAR
     */
    public synchronized long install( String location, InputStream stream )
    {
        if( isJavaEE )
        {
            deployWarProbe( stream );
            return -1;
        }
        else
        {
            return installOsgiProbe( location, stream );
        }
    }

    private long installOsgiProbe( String location, InputStream stream )
    {
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

    private void deployWarProbe( InputStream stream )
    {
        try
        {
            LOG.info( "deploying probe" );
            Deployer deployer = glassFish.getDeployer();

            /*
             * FIXME The following should work, but does not. For some reason, we cannot directly
             * deploy from a stream. As a workaround, we copy the stream to a temp file and deploy
             * the file.
             * 
             * deployer.deploy( stream, "--name", "Pax-Exam-Probe", "--contextroot",
             * "Pax-Exam-Probe" );
             */

            File tempFile = File.createTempFile( "pax-exam", ".war" );
            tempFile.deleteOnExit();
            StreamUtils.copyStream( stream, new FileOutputStream( tempFile ), true );
            deployer.deploy( tempFile, "--name", PROBE_APPLICATION_NAME, "--contextroot",
                PROBE_APPLICATION_NAME );
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
    }

    public synchronized long install( InputStream stream )
    {
        return install( "local", stream );
    }

    /**
     * Deploys all Java EE modules defined in Pax Exam options. For options without an explicit
     * application name, names app1, app2 etc. are generated on the fly. The context root defaults
     * to the application name if not set in the option.
     */
    public void deployModules()
    {
        UrlDeploymentOption[] deploymentOptions = system.getOptions( UrlDeploymentOption.class );
        int numModules = 0;
        for( UrlDeploymentOption option : deploymentOptions )
        {
            numModules++;
            if( option.getName() == null )
            {
                option.name( "app" + numModules );
            }
            deployModule( option );
        }
    }

    /**
     * Deploys the module specified by the given option.
     * 
     * @param option deployment option
     */
    private void deployModule( UrlDeploymentOption option )
    {
        try
        {
            String url = option.getURL();
            LOG.info( "deploying module {}", url );
            URI uri = new URL( url ).toURI();
            String applicationName = option.getName();
            String contextRoot = option.getContextRoot();
            if( contextRoot == null )
            {
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

    /**
     * Undeploys all modules and shuts down the GlassFish runtime, then uninstalls all OSGi bundles.
     */
    public synchronized void cleanup()
    {
        undeployModules();
        try
        {
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

    /**
     * Undeploys all deployed modules in reverse order.
     */
    private void undeployModules()
    {
        try
        {
            Deployer deployer = glassFish.getDeployer();
            while( !deployed.isEmpty() )
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

    /**
     * Sets the start level for the given bundle.
     * 
     * @param bundleId
     * @param startLevel
     * @throws TestContainerException
     */
    public void setBundleStartLevel( long bundleId, int startLevel ) throws TestContainerException
    {
        BundleContext context = framework.getBundleContext();
        StartLevel sl = getService( context, StartLevel.class );
        sl.setBundleStartLevel( context.getBundle( bundleId ), startLevel );
    }

    /**
     * Starts the GlassFish container, first downloading and installing GlassFish, if required.
     */
    public TestContainer start() throws TestContainerException
    {
        try
        {
            installContainer();

            // start OSGi framework
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

            /*
             * Start early bundles, i.e. the ones required for bootstrapping GlassFish with SLF4J
             * bridge and logback.
             */
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

            // get GlassFish service and runtime
            glassFish = getService( bc, GlassFish.class );
            glassFishRuntime = glassFish.getService( GlassFishRuntime.class );

            // set access point in test directory
            String portNumber = getPortNumber( configTarget );
            testDirectory.setAccessPoint( new URI( "http://localhost:" + portNumber
                    + "/Pax-Exam-Probe/" ) );

            // install user bundles and modules
            installAndStartBundles( bc );
            deployModules();
        }
        catch ( Exception e )
        {
            throw new TestContainerException( "Problem starting test container.", e );
        }
        return this;
    }

    /**
     * Checks if the given installation directory looks like a GlassFish installation. If the
     * directory is empty, GlassFish is downloaded and installed.
     * <p>
     * The GlassFish distribution archive has the following structure (excerpt only):
     * 
     * <pre>
     * glassfish3
     *     bin
     *     glassfish
     *         domains
     *             domain1
     *                 config
     *         modules        
     *     javadb
     *     mq
     *     pkg
     * </pre>
     * 
     * The top-level directory is always called glassfish3 and does not reflect the actual version.
     * We get rid of this directory by unpacking the archive to the <em>parent</em> directory of
     * {@code pax.exam.glassfish.home} and then renaming {@code glassfish3} to the value of
     * {@code pax.exam.glassfish.home}.
     * <p>
     * Finally, we copy all files contained in {@code pax.exam.glassfish.config.dir} (defaulting
     * to {@code src/test/resources/glassfish-config}) to
     * {@code pax.exam.glassfish.home/glassfish/domains/domain1/config/}.
     * 
     * @throws IOException
     */
    public void installContainer() throws IOException
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        cm = new ConfigurationManager();
        String systemType = cm.getProperty( Constants.EXAM_SYSTEM_KEY );
        isJavaEE = Constants.EXAM_SYSTEM_JAVAEE.equals( systemType );
        glassFishHome = cm.getProperty( GLASSFISH_HOME_KEY );

        // try the property we had in 3.0.0.M1
        if( glassFishHome == null )
        {
            glassFishHome = cm.getProperty( "pax.exam.server.home" );
        }

        if( glassFishHome == null )
        {
            throw new TestContainerException(
                "System property " + GLASSFISH_HOME_KEY + " must be set to GlassFish install root" );
        }
        File gfHome = new File( glassFishHome );
        configDirName = cm.getProperty( GLASSFISH_CONFIG_DIR_KEY, "src/test/resources/glassfish-config" );
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
            URL url = new URL( GLASSFISH_DISTRIBUTION_URL );
            File gfParent = gfHome.getParentFile();
            File tempInstall = new File( gfParent, UUID.randomUUID().toString() );
            ZipInstaller installer = new ZipInstaller( url, tempInstall.getAbsolutePath() );
            installer.downloadAndInstall();
            new File( tempInstall, "glassfish3" ).renameTo( gfHome );

            installConfiguration();
        }
    }

    /**
     * Copies all files in a user-defined configuration directory to the GlassFish instance
     * configuration directory.
     */
    private void installConfiguration()
    {
        File configSource = new File( configDirName );

        File configTargetDir = new File( glassFishHome, "glassfish/domains/domain1/config" );
        for( File configFile : configSource.listFiles() )
        {
            if( !configFile.isDirectory() )
            {
                File targetFile = new File( configTargetDir, configFile.getName() );
                try
                {
                    FileUtils.copyFile( configFile, targetFile, null );
                }
                catch ( IOException exc )
                {
                    throw new TestContainerException( "error copying config file " + configFile,
                        exc );
                }
            }
        }
    }

    /**
     * Reads the first port number from the domain.xml configuration.
     * 
     * @param domainConfig
     * @return
     */
    private String getPortNumber( File domainConfig )
    {
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse( domainConfig );
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xPath = xpf.newXPath();
            String port = xPath.evaluate( HTTP_PORT_XPATH, doc );
            return port;
        }
        catch ( ParserConfigurationException exc )
        {
            throw new IllegalArgumentException( exc );
        }
        catch ( SAXException exc )
        {
            throw new IllegalArgumentException( exc );
        }
        catch ( IOException exc )
        {
            throw new IllegalArgumentException( exc );
        }
        catch ( XPathExpressionException exc )
        {
            throw new IllegalArgumentException( exc );
        }
    }

    /**
     * Builds options for provisioning the GlassFish bootstrap bundle, Pax Exam bundles and logging
     * support.
     * 
     * @return option array
     */
    private Option[] buildContainerOptions()
    {
        return new Option[]{
            // Container dependencies need to be delegated to the system class loader,
            // or else we will run into class loader conflicts.
            systemPackages(
                "org.ops4j.pax.exam;version=" + skipSnapshotFlag( Info.getPaxExamVersion() ),
                "org.ops4j.pax.exam.options;version=" + skipSnapshotFlag( Info.getPaxExamVersion() ),
                "org.ops4j.pax.exam.util;version=" + skipSnapshotFlag( Info.getPaxExamVersion() ),
                "org.glassfish.embeddable;version=3.1",
                "org.glassfish.embeddable.spi;version=3.1" ),

            // enable Pax URL protocol handlers
            systemProperty( "java.protocol.handler.pkgs" ).value( "org.ops4j.pax.url" ),

            // define installation area
            systemProperty( "com.sun.aas.installRoot" ).value( glassFishHome + "/glassfish" ),
            systemProperty( "com.sun.aas.instanceRoot" ).value(
                glassFishHome + "/glassfish/domains/domain1" ),

            // override logging configuration
            systemProperty( "java.util.logging.config.file" ).value(
                configDirName + "/logging.properties" ),

            // set logback configuration
            systemProperty( "logback.configurationFile" ).value(
                "file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml" ),

            // set property for OSGi framework - is this still needed?
            systemProperty( "GlassFish_Platform" ).value( "Equinox" ),

            // for well-behaved class loading
            frameworkProperty( "org.osgi.framework.bundle.parent" ).value( "framework" ),
            frameworkProperty( "osgi.resolver.preferSystemPackages" ).value( "false" ),
            frameworkProperty( "osgi.compatibility.bootdelegation" ).value( "false" ),

            // set framework start leve
            frameworkStartLevel( START_LEVEL_TEST_BUNDLE ),

            // Pax Exam bundles and dependencies, including SLF4J + Logback instead of Pax Logging.
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

    /**
     * Stops the test container gracefully, undeploying all modules and uninstalling all bundles.
     */
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

    /**
     * Stops the framework, throwing an exception if no stop event was received after the configured
     * timeout period.
     * 
     * @throws BundleException
     * @throws InterruptedException
     */
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

    /**
     * Installs and starts bundles defined in Pax Exam provisioning options. This does <em>not</em>
     * include "early" bundles required for GlassFish and the Pax Exam itself.
     * 
     * @param context
     * @param bundle
     * @throws BundleException
     */
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
