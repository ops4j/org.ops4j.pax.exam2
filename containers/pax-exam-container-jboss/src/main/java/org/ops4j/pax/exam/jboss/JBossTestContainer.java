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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Stack;
import java.util.concurrent.ExecutionException;

import org.jboss.as.embedded.EmbeddedServerFactory;
import org.jboss.as.embedded.ServerStartException;
import org.jboss.as.embedded.StandaloneServer;
import org.ops4j.io.FileUtils;
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
import org.ops4j.pax.exam.options.ValueOption;
import org.ops4j.spi.ServiceProviderFinder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class JBossTestContainer implements TestContainer
{
    // TODO make this configurable
    // The only reason for not using full profile is reduced download time ;-)
    public static final String GLASSFISH_WEB_DISTRIBUTION_URL =
        "mvn:org.glassfish.main.distributions/web/3.1.2/zip";

    private static final Logger LOG = LoggerFactory.getLogger( JBossTestContainer.class );

    private Stack<File> deployed = new Stack<File>();

    private ExamSystem system;

    private String jBossHome;

    private TestDirectory testDirectory;

    private StandaloneServer server;

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
        try
        {
            File tempFile = new File( "/tmp/Pax-Exam-Probe.war" );
            tempFile.deleteOnExit();
            StreamUtils.copyStream( stream, new FileOutputStream( tempFile ), true );
            server.deploy( tempFile );
            deployed.push( tempFile );
        }
        catch ( IOException exc )
        {

        }
        catch ( ExecutionException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( InterruptedException e )
        {
            // TODO Auto-generated catch block
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
            InputStream stream = new URL( option.getURL() ).openStream();
            File tempFile = new File( "/tmp/" + option.getName() + ".war" );
            StreamUtils.copyStream( stream, new FileOutputStream( tempFile ), true );
            server.deploy( tempFile );
        }
        catch ( ExecutionException exc )
        {
            throw new TestContainerException( "Problem deploying " + option, exc );
        }
        catch ( InterruptedException exc )
        {
            throw new TestContainerException( "Problem deploying " + option, exc );
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

    public synchronized void cleanup()
    {
        undeployModules();
    }

    private void undeployModules()
    {
    }

    public TestContainer start() throws TestContainerException
    {
        installContainer();
        File tempDir = system.getTempFolder();
        File dataDir = new File(tempDir, "data");
        dataDir.mkdir();
        File configDir = new File("src/test/resources/jboss-config");
        System.setProperty( "jboss.server.config.dir", configDir.getAbsolutePath() );
        System.setProperty( "jboss.server.data.dir", dataDir.getAbsolutePath() );
        server =
            EmbeddedServerFactory.create( new File( jBossHome ), System.getProperties(),
                System.getenv(), "org.jboss.logmanager", "org.jboss.logging", "org.slf4j",
                "org.slf4j.cal10n", "ch.qos.cal10n" );
        try
        {
            server.start();
            testDirectory.setAccessPoint( new URI( "http://localhost:9080/Pax-Exam-Probe/" ) );
            deployModules();
        }
        catch ( ServerStartException e )
        {
            throw new TestContainerException( "Problem starting test container.", e );
        }
        catch ( URISyntaxException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this;
    }

    public void installContainer()
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        System.setProperty( "java.util.logging.manager", "org.jboss.logmanager.LogManager" );
        ConfigurationManager cm = new ConfigurationManager();
        jBossHome = cm.getProperty( "pax.exam.server.home" );
        if( jBossHome == null )
        {
            throw new TestContainerException(
                "System property pax.exam.server.home must be set to JBoss AS install root" );
        }
    }

    public TestContainer stop()
    {
        cleanup();
        system.clear();
        return this;
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

    @Override
    public String toString()
    {
        return "JBossContainer";
    }
}
