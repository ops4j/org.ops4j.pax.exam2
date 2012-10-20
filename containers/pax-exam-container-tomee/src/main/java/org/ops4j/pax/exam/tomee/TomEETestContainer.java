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
package org.ops4j.pax.exam.tomee;

import static org.ops4j.pax.exam.spi.container.ContainerConstants.EXAM_APPLICATION_NAME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.Stack;

import javax.naming.NamingException;

import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.config.sys.Tomee;
import org.apache.tomee.embedded.Configuration;
import org.ops4j.io.StreamUtils;
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
import org.ops4j.spi.ServiceProviderFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class TomEETestContainer implements TestContainer
{
    /**
     * Configuration property key for TomEE configuration file directory. The files contained in
     * this directory will be used to configure the TomEE instance.
     */
    public static final String TOMEE_CONFIG_DIR_KEY = "pax.exam.tomee.config.dir";

    public static final String TOMEE_HTTP_PORT_KEY = "pax.exam.tomee.http.port";

    public static final String TOMEE_STOP_PORT_KEY = "pax.exam.tomee.stop.port";

    private static final Logger LOG = LoggerFactory.getLogger( TomEETestContainer.class );

    private Stack<String> deployed = new Stack<String>();

    private ExamSystem system;

    private TestDirectory testDirectory;

    private WrappedTomEEContainer tomee;

    private File webappDir;

    public TomEETestContainer( ExamSystem system )
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
        deployModule( EXAM_APPLICATION_NAME, stream );
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
            deployModule( option.getName(), applUrl.openStream() );
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

    private void deployModule( String applicationName, InputStream stream )
    {
        try
        {
            File warFile = new File( webappDir, applicationName + ".war" );
            StreamUtils.copyStream( stream, new FileOutputStream( warFile ), true );
            tomee.deploy( applicationName, warFile );
        }
        catch ( IOException exc )
        {
            throw new TestContainerException( "Problem deploying " + applicationName, exc );
        }
        catch ( NamingException exc )
        {
            throw new TestContainerException( "Problem deploying " + applicationName, exc );
        }
        catch ( OpenEJBException exc )
        {
            throw new TestContainerException( "Problem deploying " + applicationName, exc );
        }
    }

    public void cleanup()
    {
        undeployModules();
        LOG.info( "stopping TomEE" );
        try
        {
            tomee.stop();
        }
        catch ( Exception exc )
        {
            throw new TestContainerException( exc );
        }
    }

    private void undeployModules()
    {
        while( !deployed.isEmpty() )
        {
            String applicationName = deployed.pop();
            try
            {
                tomee.undeploy( applicationName );
            }
            catch ( UndeployException exc )
            {
                throw new TestContainerException( exc );
            }
            catch ( NoSuchApplicationException exc )
            {
                throw new TestContainerException( exc );
            }
        }
    }

    public TestContainer start()
    {
        LOG.info( "starting TomEE" );
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );

        File tempDir = system.getTempFolder();
        webappDir = new File( tempDir, "webapps" );
        webappDir.mkdirs();

        ConfigurationManager cm = new ConfigurationManager();
        String configDirName =
            cm.getProperty( TOMEE_CONFIG_DIR_KEY, "src/test/resources/tomee-config" );
        String httpPortString = cm.getProperty( TOMEE_HTTP_PORT_KEY, "9080" );
        String stopPortString = cm.getProperty( TOMEE_STOP_PORT_KEY, "9005" );
        int httpPort = Integer.parseInt( httpPortString );
        int stopPort = Integer.parseInt( stopPortString );

        File tomeeXml = new File( configDirName, "tomee.xml" );
        File serverXml = new File( configDirName, "server.xml" );

        Configuration tomeeConfig = new Configuration();
        tomeeConfig.setHttpPort( httpPort );
        tomeeConfig.setStopPort( stopPort );
        Properties props = new Properties();

        if( tomeeXml.exists() )
        {
            props.setProperty( "openejb.configuration", tomeeXml.getAbsolutePath() );
            props.setProperty( "openejb.configuration.class", Tomee.class.getName() );
        }
        tomeeConfig.setProperties( props );
        
        if( serverXml.exists() )
        {
            tomeeConfig.setServerXml( serverXml.getAbsolutePath() );
        }

        tomee = new WrappedTomEEContainer();
        tomee.setup( tomeeConfig );
        try
        {
            tomee.start();
            testDirectory.setAccessPoint( new URI( "http://localhost:" + httpPort
                    + "/Pax-Exam-Probe/" ) );
        }
        catch ( Exception exc )
        {
            throw new TestContainerException( exc );
        }
        return this;
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
        return "TomEE";
    }
}
