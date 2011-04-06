/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ops4j.pax.exam.container.externalframework;

import static org.ops4j.pax.exam.container.externalframework.options.ExternalFrameworkOptions.*;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.waitForFrameworkStartupFor;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.options.TestContainerStartTimeoutOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.container.externalframework.options.KarafFrameworkConfigurationOption;

/**
 * Before running this test, you must delete all directory like target/test-karaf-home-*
 * @author Stephane Chomat
 *
 */

@RunWith(JUnit4TestRunner.class)
public class MainStart2Test  {
	
	 @Inject
	 protected BundleContext bundleContext;
	 
	private static String mvnUrl = "mvn:org.osgi/org.osgi.compendium/4.2.0";
	
	private static File home;
	private static String fileMVNbundle;


	@Configuration
    public static Option[] configuration() throws Exception{
		File basedir = new File(MainStart2Test.class.getClassLoader().getResource("foo").getPath()).getParentFile();
        home = new File(basedir, "test-karaf-home");
        fileMVNbundle = new File(home, "bundles/pax-url-mvn.jar").toURI().toURL().toExternalForm();
		
		return options(
        		new KarafFrameworkConfigurationOption("start2").
        			home(home).
        			systemBundleId(7).
                    defaultConf(),
                systemProperty(JAVA_RUNNER).value(JAVA_RUNNER_DEFAULT),
                systemProperty("karaf.auto.start.2").value( "\""+fileMVNbundle+"|unused\""),
        		systemProperty("karaf.auto.start.5").value( "\""+mvnUrl+"|unused\""),
        		systemProperty("fileMVNbundle").value( fileMVNbundle),
        		systemProperty("mvnUrl").value( fileMVNbundle),
        		
        		// wait for ever
                new TestContainerStartTimeoutOption(Long.MAX_VALUE),
                //vmOption( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5008" ),
            		 
             waitForFrameworkStartupFor(60000)
        );
    }
	
	
	/** 
	 * Start two bundles at level one and two.
	 * At level one start mvn handler service and at level 2 use mvn handler service.
	 * @throws Exception cannot start karaf.
	 */
	@Test
    public void testAutoStart() throws Exception {
		Thread.sleep(1000);
		Bundle[] bundles = bundleContext.getBundles();
		Assert.assertTrue(bundles.length>=3);
        for (Bundle b : bundles) {
			System.out.println(b);
			System.out.println(" - "+b.getLocation());
		}
        fileMVNbundle = System.getProperty("fileMVNbundle");
		Assert.assertEquals(fileMVNbundle, bundles[6].getLocation());
		Assert.assertEquals(mvnUrl, bundles[7].getLocation());
		Assert.assertEquals(Bundle.ACTIVE, bundles[6].getState());
		Assert.assertEquals(Bundle.ACTIVE, bundles[7].getState());
	}
	
}
