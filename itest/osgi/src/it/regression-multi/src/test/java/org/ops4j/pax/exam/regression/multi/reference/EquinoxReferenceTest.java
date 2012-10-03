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
package org.ops4j.pax.exam.regression.multi.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.isEquinox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.ops4j.pax.exam.util.PathUtils;
import org.ops4j.pax.swissbox.framework.FrameworkFactoryFinder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class EquinoxReferenceTest {

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    @Test
    public void installAndStartReferenceBundle() throws BundleException, IOException {
        assumeTrue( isEquinox() );
        assumeTrue(System.getProperty("java.protocol.handler.pkgs") == null);

        Map<String, String> props = new HashMap<String, String>();
        props.put("osgi.clean", "true");
        props.put("osgi.dev", "target/classes");

        FrameworkFactory frameworkFactory = FrameworkFactoryFinder.loadSingleFrameworkFactory();
        Framework framework = frameworkFactory.newFramework( props );
        framework.start();
        BundleContext bc = framework.getBundleContext();
        assertNotNull(bc);

        String reference = "reference:file:" + PathUtils.getBaseDir() +
                "/target/regression-pde-bundle";
        Bundle bundle = bc.installBundle(reference);
        assertNotNull(bundle);
        assertEquals("org.ops4j.pax.exam.regression.pde", bundle.getSymbolicName());

        bundle.start();
        ServiceReference serviceRef = bc.getServiceReference("org.ops4j.pax.exam.regression.pde.HelloService");
        Object service = bc.getService(serviceRef);
        assertNotNull(service);
        assertEquals("org.ops4j.pax.exam.regression.pde.impl.EnglishHelloService", service.getClass().getName());

        bundle.uninstall();
        framework.stop();
    }
}
