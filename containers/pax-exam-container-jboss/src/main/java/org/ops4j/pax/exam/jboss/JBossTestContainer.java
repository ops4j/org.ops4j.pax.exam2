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
package org.ops4j.pax.exam.jboss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jboss.as.controller.client.helpers.standalone.DeploymentPlan;
import org.jboss.as.controller.client.helpers.standalone.InitialDeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentActionResult;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.as.controller.client.helpers.standalone.ServerUpdateActionResult.Result;
import org.jboss.as.embedded.EmbeddedServerFactory;
import org.jboss.as.embedded.ServerStartException;
import org.jboss.as.embedded.StandaloneServer;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.ProbeInvokerFactory;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDirectory;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.options.UrlDeploymentOption;
import org.ops4j.pax.exam.zip.ZipInstaller;
import org.ops4j.spi.ServiceProviderFinder;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class JBossTestContainer implements TestContainer
{
    // TODO make this configurable
    public static final String JBOSS_DISTRIBUTION_URL =
        "mvn:org.jboss.as/jboss-as-dist/7.1.1.Final/zip";

    private static final Logger LOG = LoggerFactory.getLogger( JBossTestContainer.class );

    final private Stack<String> deployed = new Stack<String>();

    final private ExamSystem system;

    final private TestDirectory testDirectory;

    private String jBossHome;

    private StandaloneServer server;

    private ServerDeploymentManager deploymentManager;

    private String httpPort;

    private String mgmtPort;

    public JBossTestContainer( ExamSystem system, FrameworkFactory frameworkFactory )
    {
        this.system = system;
        this.testDirectory = TestDirectory.getInstance();
    }

    public synchronized void call( TestAddress address )
    {
        TestInstantiationInstruction instruction = testDirectory.lookup( address );
        ProbeInvokerFactory probeInvokerFactory =
            ServiceProviderFinder.loadUniqueServiceProvider( ProbeInvokerFactory.class );
        ProbeInvoker invoker =
            probeInvokerFactory.createProbeInvoker( null, instruction.toString() );
        invoker.call( address.arguments() );
    }

    public synchronized long install( String location, InputStream stream )
    {
        deployModule( "Pax-Exam-Probe", stream);
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
            if( option.getName() == null )
            {
                option.name( "app" + numModules );
            }
            deployModule( option );
        }
    }

    private void deployModule( UrlDeploymentOption option )
    {
        try
        {
            URL applUrl = new URL( option.getURL() );
            deployModule(option.getName(), applUrl.openStream());
        }
        catch ( MalformedURLException exc )
        {
            throw new TestContainerException( "Problem deploying " + option, exc );
        }
        catch ( IOException exc )
        {
            throw new TestContainerException( "Problem deploying " + option, exc );
        }
    }
    
    private void deployModule( String applicationName, InputStream stream)
    {
        
        try
        {
            String warName = applicationName + ".war";
            InitialDeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
            DeploymentPlan plan = builder.add( warName, stream ).deploy( warName ).build();
            ServerDeploymentPlanResult result = deploymentManager.execute( plan ).get();
            UUID actionId = plan.getDeploymentActions().get( 0 ).getId();
            ServerDeploymentActionResult actionResult = result.getDeploymentActionResult( actionId );

            if( actionResult.getResult() != Result.EXECUTED )
            {
                throw new TestContainerException( "problem deploying " + applicationName );
            }
            deployed.push( warName );
        }
        catch ( ExecutionException exc )
        {
            throw new TestContainerException( "Problem deploying " + applicationName, exc );
        }
        catch ( InterruptedException exc )
        {
            throw new TestContainerException( "Problem deploying " + applicationName, exc );
        }
    }

    public void cleanup()
    {
        undeployModules();
        server.stop();
    }

    private void undeployModules()
    {
        while( !deployed.isEmpty() )
        {
            String applicationName = deployed.pop();
            undeployModule( applicationName );
        }
    }

    private void undeployModule( String applName )
    {
        InitialDeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
        DeploymentPlan plan = builder.undeploy( applName ).andRemoveUndeployed().build();
        ServerDeploymentPlanResult result;
        try
        {
            result = deploymentManager.execute( plan ).get();
        }
        catch ( InterruptedException exc )
        {
            throw new TestContainerException( "problem undeploying " + applName, exc );
        }
        catch ( ExecutionException exc )
        {
            throw new TestContainerException( "problem undeploying " + applName, exc );
        }
        UUID actionId = plan.getDeploymentActions().get( 0 ).getId();
        ServerDeploymentActionResult actionResult = result.getDeploymentActionResult( actionId );

        if( actionResult.getResult() != Result.EXECUTED )
        {
            throw new TestContainerException( "problem undeploying " + applName );
        }
    }

    public TestContainer start() throws TestContainerException
    {
        installContainer();
        File tempDir = system.getTempFolder();
        File dataDir = new File( tempDir, "data" );
        dataDir.mkdir();
        File configDir = new File( "src/test/resources/jboss-config" );
        File configFile = new File(configDir, "standalone.xml");
        if (!configFile.exists())
        {
            throw new TestContainerException( configFile + " does not exist" );
        }
        parseServerConfiguration( configFile );
        System.setProperty( "jboss.server.config.dir", configDir.getAbsolutePath() );
        System.setProperty( "jboss.server.data.dir", dataDir.getAbsolutePath() );
        server =
            EmbeddedServerFactory.create( new File( jBossHome ), 
                System.getProperties(),
                System.getenv(),
                // packages to be loaded from system class loader
                "org.jboss.logmanager", 
                "org.jboss.logging", 
                "org.jboss.threads",
                "org.slf4j",
                "org.slf4j.cal10n", 
                "ch.qos.cal10n" );
        try
        {
            server.start();
            deploymentManager =
                ServerDeploymentManager.Factory.create( InetAddress.getByName( "localhost" ), Integer.parseInt( mgmtPort ) );
            testDirectory.setAccessPoint( new URI( "http://localhost:" + httpPort + "/Pax-Exam-Probe/" ) );
            deployModules();
        }
        catch ( ServerStartException exc )
        {
            throw new TestContainerException( "Problem starting test container.", exc );
        }
        catch ( URISyntaxException exc )
        {
            throw new TestContainerException( "Problem starting test container.", exc );
        }
        catch ( UnknownHostException exc )
        {
            throw new TestContainerException( "Problem starting test container.", exc );
        }
        return this;
    }

    public void installContainer()
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        System.setProperty( "java.util.logging.manager", "org.jboss.logmanager.LogManager" );
        ConfigurationManager cm = new ConfigurationManager();
        jBossHome = cm.getProperty( "pax.exam.jboss.home" );
        if( jBossHome == null )
        {
            throw new TestContainerException(
                "System property pax.exam.jboss.home must be set to JBoss AS install root" );
        }
        File installDir = new File(jBossHome);
        if( installDir.exists() )
        {
            File moduleLoader = new File( installDir, "jboss-modules.jar" );
            if( moduleLoader.exists() )
            {
                LOG.info( "using JBoss AS installation in {}", jBossHome );
            }
            else
            {
                String msg =
                    String.format( "%s exists, but %s does not. " +
                            "This does not look like a valid JBoss AS installation.",
                        jBossHome, moduleLoader );
                throw new TestContainerException( msg );
            }
        }
        else
        {
            LOG.info( "installing JBoss AS in {}", jBossHome );
            try
            {
                URL url = new URL( JBOSS_DISTRIBUTION_URL );
                File installParent = installDir.getParentFile();
                File tempInstall = new File( installParent, UUID.randomUUID().toString() );
                ZipInstaller installer = new ZipInstaller( url, tempInstall.getAbsolutePath() );
                installer.downloadAndInstall();
                File unpackedRoot = tempInstall.listFiles()[0];
                unpackedRoot.renameTo( installDir );
            }
            catch ( MalformedURLException exc )
            {
                throw new TestContainerException( exc );
            }
            catch ( IOException exc )
            {
                throw new TestContainerException( "error during JBoss AS installation", exc );                
            }
        }
    }
    
    private void parseServerConfiguration(File serverConfig) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(serverConfig);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xPath = xpf.newXPath();
            String HTTP_PORT_XPATH = "/server/socket-binding-group/socket-binding[@name='http']/@port";
            String MGMT_PORT_XPATH = "/server/socket-binding-group/socket-binding[@name='management-native']/@port";
            httpPort = substituteProperties(xPath.evaluate(HTTP_PORT_XPATH, doc));
            mgmtPort = substituteProperties(xPath.evaluate(MGMT_PORT_XPATH, doc));
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
  
    
    
    public String substituteProperties(String value)
    {
        String result = value;
        if (value.startsWith("${") && value.endsWith( "}" ))
        {
            String propWithDefault = value.substring( 2, value.length()-1 );
            int colon = propWithDefault.indexOf( ':' );
            String defaultValue = "";
            String propertyKey = propWithDefault;
            if (colon >= 0)
            {
                propertyKey = propWithDefault.substring( 0, colon );
                defaultValue = propWithDefault.substring( colon+1 );
            }
            result = System.getProperty( propertyKey, defaultValue );
        }
        return result;
    }
    

    public TestContainer stop()
    {
        cleanup();
        system.clear();
        return this;
    }

    @Override
    public String toString()
    {
        return "JBossContainer";
    }
}
