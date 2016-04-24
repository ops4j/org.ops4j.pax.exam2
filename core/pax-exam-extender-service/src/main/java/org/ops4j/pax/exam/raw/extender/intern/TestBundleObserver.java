/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.raw.extender.intern;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.ProbeInvokerFactory;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.ops4j.pax.swissbox.extender.ManifestEntry;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Toni Menzel
 * @since Jan 7, 2010
 */
public class TestBundleObserver implements BundleObserver<ManifestEntry> {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TestBundleObserver.class);
    /**
     * Holder for regression runner registrations per bundle.
     */
    private final Map<Bundle, ServiceRegistration<?>> registrations;

    /**
     * Constructor.
     */
    TestBundleObserver() {
        registrations = new HashMap<Bundle, ServiceRegistration<?>>();
    }

    /**
     * Registers specified regression case as a service.
     */
    @Override
    public void addingEntries(Bundle bundle, List<ManifestEntry> manifestEntries) {
        for (ManifestEntry manifestEntry : manifestEntries) {

            if (Constants.PROBE_EXECUTABLE.equals(manifestEntry.getKey())) {
                BundleContext bc = bundle.getBundleContext();
                ServiceRegistration<ProbeInvoker> registration = bc.registerService(ProbeInvoker.class, createInvoker(bc), new Hashtable<String, Object>());
                registrations.put(bundle, registration);
                break;
            }
        }
    }

    private ProbeInvoker createInvoker(BundleContext ctx) {
        String invokerType = System.getProperty("pax.exam.invoker");
        if (invokerType == null) {
            throw new TestContainerException("System property pax.exam.invoker must be defined");
        }
        else {
            Map<String, String> props = new HashMap<String, String>();
            props.put("driver", invokerType);
            ProbeInvokerFactory factory = ServiceLookup.getService(ctx, ProbeInvokerFactory.class,
                props);
            return factory.createProbeInvoker(ctx, null);
        }
    }


    /**
     * Unregisters prior registered regression for the service.
     */
    @Override
    public void removingEntries(final Bundle bundle, final List<ManifestEntry> manifestEntries) {
        ServiceRegistration<?> registration = registrations.remove(bundle);
        if (registration != null) {
            // Do not unregister as below, because the services are automatically unregistered as
            // soon as the bundle
            // for which the services are registered gets stopped
            // registration.serviceRegistration.unregister();
            LOG.debug("Unregistered ProbeInvoker for bundle {}", bundle);
        }
    }
}
