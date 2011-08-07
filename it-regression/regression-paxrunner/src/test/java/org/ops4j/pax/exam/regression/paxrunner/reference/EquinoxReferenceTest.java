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
package org.ops4j.pax.exam.regression.paxrunner.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.launch.Equinox;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.ops4j.pax.exam.regression.paxrunner.util.PathUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;

public class EquinoxReferenceTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void exceptionWithoutCustomHandler() throws BundleException, IOException {
        assertNull(System.getProperty("java.protocol.handler.pkgs"));
        expectedException.expect(MalformedURLException.class);
        expectedException.expectMessage("unknown protocol");
        assertNull(System.getProperty("java.protocol.handler.pkgs"));
        String reference = "reference:file:" + PathUtils.getBaseDir() +
                "/../regression-pde-bundle";
        new URL(reference);
    }

    @Test
    public void installAndStartReferenceBundle() throws BundleException, IOException {
        assertNull(System.getProperty("java.protocol.handler.pkgs"));

        Map<String, String> props = new HashMap<String, String>();
        props.put("osgi.clean", "true");
        props.put("osgi.dev", "target/classes");

        Framework framework = new Equinox(props);
        framework.start();
        BundleContext bc = framework.getBundleContext();
        assertNotNull(bc);

        String reference = "reference:file:" + PathUtils.getBaseDir() +
                "/../regression-pde-bundle";
        Bundle bundle = bc.installBundle(reference);
        assertNotNull(bundle);
        assertEquals("org.ops4j.pax.exam.regression.pde", bundle.getSymbolicName());

        bundle.start();
        ServiceReference serviceRef = bc.getServiceReference("org.ops4j.pax.exam.regression.pde.HelloService");
        Object service = bc.getService(serviceRef);
        assertNotNull(service);
        assertEquals("org.ops4j.pax.exam.regression.pde.HelloServiceImpl", service.getClass().getName());

        bundle.uninstall();
        framework.stop();
    }

    @Test
    public void equinoxInternalReferenceHandler() throws BundleException, IOException {
        try {
            System.setProperty("java.protocol.handler.pkgs", "org.eclipse.osgi.framework.internal.protocol");
            String reference = "reference:file:" + PathUtils.getBaseDir() +
                    "/../regression-pde-bundle";
            URL url = new URL(reference);
            assertEquals("reference", url.getProtocol());
            InputStream is = url.openStream();
            assertNotNull(is);
            assertEquals("org.eclipse.osgi.framework.internal.core.ReferenceInputStream", is.getClass().getName());
        } finally {
            System.setProperty("java.protocol.handler.pkgs", "bogus");
        }
    }
}
