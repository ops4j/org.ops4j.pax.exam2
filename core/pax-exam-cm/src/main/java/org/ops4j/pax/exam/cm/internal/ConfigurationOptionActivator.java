/*
 * Copyright 2013 Christoph Läubrich
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
package org.ops4j.pax.exam.cm.internal;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The {@link BundleActivator} that is used internaly to interact with {@link ConfigurationAdmin}
 */
public class ConfigurationOptionActivator implements BundleActivator {

    private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> serviceTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        URL entry = context.getBundle().getEntry("/override.obj");
        InputStream stream = entry.openStream();
        try {
            ObjectInputStream oi = new ObjectInputStream(stream);
            try {
                String pid = (String) oi.readObject();
                boolean create = oi.readBoolean();
                boolean override = oi.readBoolean();
                boolean factory = oi.readBoolean();
                @SuppressWarnings("unchecked")
                Map<String, Object> overrides = (Map<String, Object>) oi.readObject();
                ConfigurationOptionConfigurationListener listener = new ConfigurationOptionConfigurationListener(
                    pid, overrides, context, create, override, factory);
                context.registerService(ConfigurationListener.class.getName(), listener, null);
                serviceTracker = new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(context, ConfigurationAdmin.class,
                    listener);
                serviceTracker.open();
            }
            finally {
                oi.close();
            }
        }
        finally {
            stream.close();
        }

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        serviceTracker.close();
    }

}
