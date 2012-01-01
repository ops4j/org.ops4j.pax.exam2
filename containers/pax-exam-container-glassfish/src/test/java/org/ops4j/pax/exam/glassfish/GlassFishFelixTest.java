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
import java.util.HashMap;
import java.util.Map;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.junit.Test;
import org.ops4j.pax.exam.util.ServiceLookup;
import org.ops4j.pax.swissbox.framework.FrameworkFactoryFinder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class GlassFishFelixTest
{

    @Test
    public void launchGlassFish() throws BundleException, InterruptedException, InvalidSyntaxException, GlassFishException {
        FrameworkFactory frameworkFactory = FrameworkFactoryFinder.loadSingleFrameworkFactory();
        
        String GLASSFISH_HOME = "/home/hwellmann/gf/glassfish-3.1.1-orig/glassfish/";
        System.setProperty("com.sun.aas.installRoot", GLASSFISH_HOME);
        System.setProperty("com.sun.aas.instanceRoot", GLASSFISH_HOME + "domains/domain1");
        //System.setProperty("org.glassfish.additionalOSGiBundlesToStart", "org.apache.felix.shell,org.apache.felix.gogo.runtime,org.apache.felix.gogo.shell,org.apache.felix.gogo.command,org.apache.felix.fileinstall");
        //System.setProperty("osgi.shell.telnet.port", "6666");
        System.setProperty("gosh.args", "--nointeractive");
        System.setProperty("GlassFish_Platform", "Felix");

        
        
        Map<String, String> frameworkProps = new HashMap<String, String>();
        frameworkProps.put("org.osgi.framework.storage", "/tmp/gf");
        frameworkProps.put("org.osgi.framework.bundle.parent", "framework");
        frameworkProps.put("org.osgi.framework.startlevel.beginning", "5");
        frameworkProps.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "org.glassfish.embeddable;version=3.1,org.glassfish.embeddable.spi;version=3.1");
        frameworkProps.put(Constants.FRAMEWORK_BOOTDELEGATION, "sun.*");
        frameworkProps.put("felix.log.level", "3");
        frameworkProps.put("felix.bootdelegation.implicit", "false");
        
        Framework framework = frameworkFactory.newFramework( frameworkProps );
        framework.init();
        
        BundleContext bc = framework.getBundleContext();
        Bundle gfBundle = bc.installBundle( "file:" + GLASSFISH_HOME + "modules/glassfish.jar" );
        framework.start();
        Thread.sleep(1000);
        System.out.println("vendor = " + System.getProperty( Constants.FRAMEWORK_VENDOR ));
        gfBundle.start();
        GlassFish gf = ServiceLookup.getService( bc, GlassFish.class );
        startShellBundles(bc);

        Deployer deployer = gf.getDeployer();
        for (String appName : deployer.getDeployedApplications()) {
            System.out.println("undeploying " + appName);
            deployer.undeploy( appName );
        }
        deployer.deploy( new File("/home/hwellmann/tmp/library.war"));
        Thread.sleep( 10000000 );
        gf.stop();    
    }

    private void startShellBundles( BundleContext bc ) throws BundleException
    {
        for (Bundle bundle : bc.getBundles()) {
            String name = bundle.getSymbolicName();
            if (name.startsWith( "org.apache.felix.gogo" ) || name.startsWith( "org.apache.felix.shell.remote")) {
                bundle.start();
            }
        }
    }
}
