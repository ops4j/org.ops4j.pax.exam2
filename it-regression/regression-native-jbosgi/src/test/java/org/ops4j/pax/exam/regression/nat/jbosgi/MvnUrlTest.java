/*
 * Copyright (C) 2011 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.regression.nat.jbosgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;

import org.jboss.osgi.framework.internal.FrameworkFactoryImpl;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * Check if we can install bundles from mvn: URLs into JBoss OSGi.
 * This currently does not work - JBoss OSGi does not seem to
 * recognize any custom protocol handlers, but this is a prerequisite
 * for using the Native Test Container.
 * 
 * @author Harald Wellmann
 *
 */
public class MvnUrlTest {

    private HashMap<String, String> props;

    @Before
    public void setUp() {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        props = new HashMap<String, String>();
    }
    
    @Test
    public void jbosgiMvnUrl() throws BundleException, IOException {
        FrameworkFactory frameworkFactory = new FrameworkFactoryImpl();
        Framework framework = frameworkFactory.newFramework( props );
        provisionMvnUrl(framework);
    }

    private void provisionMvnUrl(Framework framework) throws BundleException, IOException {
        framework.start();
        BundleContext bc = framework.getBundleContext();
        assertNotNull(bc);
        
        String url = "mvn:org.ops4j.base/ops4j-base-lang/1.2.3";
        
        // throws java.lang.IllegalArgumentException: Null path
        Bundle bundle = bc.installBundle(url);
        
        assertNotNull(bundle);
        assertEquals("org.ops4j.base.lang", bundle.getSymbolicName());

        bundle.start();
        bundle.uninstall();
        framework.stop();
    }
}
