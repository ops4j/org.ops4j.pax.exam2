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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.swissbox.framework.FrameworkFactoryFinder;
import org.ops4j.pax.swissbox.framework.ServiceLookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import com.google.common.io.Files;

public class GlassFishLaunchTest
{

    private FrameworkFactory frameworkFactory;
    private String glassFishHome;
    private String instanceRoot;
    
    @Before
    public void setUp() throws IOException
    {
        GlassFishTestContainer gftc = new GlassFishTestContainer( null, null );
        gftc.installContainer();
    }

    @Test
    public void launchGlassFish() throws Exception
    {
        System.setProperty( "java.util.logging.config.file", "src/test/resources/glassfish-config/logging.properties" );
        ConfigurationManager cm = new ConfigurationManager();
        glassFishHome = cm.getProperty( GlassFishTestContainer.GLASSFISH_HOME_KEY );

        setSystemProperties();
        Map<String, String> frameworkProps = createFrameworkProperties();
        
        FileUtils.copyFile( new File("src/test/resources/glassfish-config/domain.xml"), 
            new File(instanceRoot, "config/domain.xml"), null );       

        frameworkFactory = FrameworkFactoryFinder.loadSingleFrameworkFactory();
        Framework framework = frameworkFactory.newFramework( frameworkProps );
        framework.init();

        BundleContext bc = framework.getBundleContext();
        String gfBundleUrl = "file:" + glassFishHome + "/glassfish/modules/glassfish.jar";
        Bundle gfBundle = bc.installBundle( gfBundleUrl );
        framework.start();
        gfBundle.start();

        if( isFelix() )
        {
            startShellBundles( bc );
        }
        GlassFish gf = ServiceLookup.getService( bc, GlassFish.class );

        Deployer deployer = gf.getDeployer();
        for( String appName : deployer.getDeployedApplications() )
        {
            System.out.println( "undeploying " + appName );
            deployer.undeploy( appName );
        }

        URI sampleWarUri = new URI( "mvn:org.apache.wicket/wicket-examples/1.5.3/war" );
        String sampleAppName = "wicket-examples";

        System.out.println( "deploying " + sampleAppName );
        //deployer.deploy( sampleWarUri, "--name", sampleAppName, "--contextroot", sampleAppName );
        deployer.deploy( sampleWarUri.toURL().openStream(), "--name", sampleAppName, "--contextroot", sampleAppName );
        delay();

        System.out.println( "undeploying " + sampleAppName );
        deployer.undeploy( sampleAppName );
        delay();

        System.out.println( "stopping GlassFish" );
        gf.stop();
        delay();

        System.out.println( "stopping framework" );
        framework.stop();
    }

    private void delay() throws InterruptedException
    {
        Thread.sleep( 2500 );
    }

    private void setSystemProperties()
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        System.setProperty( "com.sun.aas.installRoot", glassFishHome + "/glassfish" );
        instanceRoot = glassFishHome + "/glassfish/domains/domain1";
        System.setProperty( "com.sun.aas.instanceRoot", instanceRoot );
        System.setProperty( "osgi.console", "6666" );
    }

    private Map<String, String> createFrameworkProperties()
    {
        Map<String, String> frameworkProps = new HashMap<String, String>();
        frameworkProps.put( "org.osgi.framework.storage", Files.createTempDir().getAbsolutePath() );
        frameworkProps.put( "org.osgi.framework.bundle.parent", "framework" );
        frameworkProps.put( "org.osgi.framework.startlevel.beginning", "5" );
        frameworkProps.put( Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA,
            "org.glassfish.embeddable;version=3.1,org.glassfish.embeddable.spi;version=3.1" );
        frameworkProps.put( "osgi.compatibility.bootdelegation", "false" );

        frameworkProps.put( "osgi.resolver.preferSystemPackages", "false" );
        return frameworkProps;
    }

    private void startShellBundles( BundleContext bc ) throws BundleException
    {
        for( Bundle bundle : bc.getBundles() )
        {
            String name = bundle.getSymbolicName();
            if( name.startsWith( "org.apache.felix.gogo" )
                    || name.startsWith( "org.apache.felix.shell.remote" ) )
            {
                bundle.start();
            }
        }
    }

    private boolean isFelix()
    {
        return frameworkFactory.getClass().getName().contains( "felix" );
    }
}
