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
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.junit.Test;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.util.ServiceLookup;
import org.ops4j.pax.swissbox.framework.FrameworkFactoryFinder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class GlassFishEquinoxTest
{

    @Test
    public void launchGlassFish() throws BundleException, InterruptedException, InvalidSyntaxException, GlassFishException, IOException, NamingException {
        FrameworkFactory frameworkFactory = FrameworkFactoryFinder.loadSingleFrameworkFactory();
        
        ConfigurationManager cm = new ConfigurationManager();
        String glassFishHome = cm.getProperty( "pax.exam.server.home" );
        System.setProperty("com.sun.aas.installRoot", glassFishHome);
        System.setProperty("com.sun.aas.instanceRoot", glassFishHome + "/domains/domain1");
        System.setProperty("osgi.console", "6666");
        System.setProperty("GlassFish_Platform", "Equinox");

        
        Map<String, String> frameworkProps = new HashMap<String, String>();
        frameworkProps.put("org.osgi.framework.storage", "/tmp/gf");
        frameworkProps.put("org.osgi.framework.bundle.parent", "framework");
        frameworkProps.put("org.osgi.framework.startlevel.beginning", "5");
        frameworkProps.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "org.glassfish.embeddable;version=3.1,org.glassfish.embeddable.spi;version=3.1");
        frameworkProps.put( "osgi.compatibility.bootdelegation", "false");
        
        frameworkProps.put( "osgi.resolver.preferSystemPackages", "false");

        
        
        Framework framework = frameworkFactory.newFramework( frameworkProps );
        framework.init();
        
        BundleContext bc = framework.getBundleContext();
        Bundle gfBundle = bc.installBundle(  "file:"+ glassFishHome + "/modules/glassfish.jar" );
        framework.start();
        gfBundle.start();
        GlassFish gf = ServiceLookup.getService( bc, GlassFish.class );

        Deployer deployer = gf.getDeployer();
        for (String appName : deployer.getDeployedApplications()) {
            System.out.println("undeploying " + appName);
            deployer.undeploy( appName );
        }
        InitialContext ic = new InitialContext();
        System.out.println(ic.lookup( "jdbc/library" ));
        System.out.println(ic.lookup( "jdbc/jeeunit" ));
        deployer.deploy( new File("/home/hwellmann/tmp/library.war"));
        Thread.sleep( 10000000 );
        gf.stop();    
    }
}
