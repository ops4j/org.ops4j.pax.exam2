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
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

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
                serviceTracker = safeRegisterServiceTracker(context, ConfigurationAdmin.class, listener);
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

    /**
     * Safely creates a {@link ServiceTracker} across wide OSGi versions. It is
     * used because the ServiceTracker's constructors are changed and they cannot
     * be invoked directly.
     * <p>
     * The method uses reflection to find the appropriate constructor and invokes
     * it accordingly.
     *
     * TODO the method should be part of the pax swissbox project
     *
     * @param context The {@link BundleContext} against which the tracking
     *        is done.
     * @param clazz The class of the services to be tracked.
     * @param customizer The customizer object to call when services are added,
     *        modified, or removed in this {@code ServiceTracker}. If
     *        customizer is {@code null}, then this
     *        {@link ServiceTracker} will be used as the
     *        {@link ServiceTrackerCustomizer} and this
     *        {@link ServiceTracker} will call the
     *        {@link ServiceTrackerCustomizer} methods on itself.
     * @return created ServiceTracker
     */
    @SuppressWarnings("unchecked")
    private <S, T> ServiceTracker<S, T> safeRegisterServiceTracker(BundleContext context, Class<S> clazz,
            final ServiceTrackerCustomizer<S, T> customizer) {
        Constructor<?>[] constructors = ServiceTracker.class.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] parameters = constructor.getParameterTypes();
            if (parameters.length == 3
                    && parameters[0].equals(BundleContext.class)
                    && parameters[1].equals(String.class)
                    && parameters[2].equals(ServiceTrackerCustomizer.class)) {
                try {
                    return (ServiceTracker<S, T>) constructor.newInstance(context, clazz.getName(), customizer);
                } catch (Exception e) {
                    // do not catch 
                }
            }
        }

        return new ServiceTracker<S, T>(context, clazz, customizer);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        serviceTracker.close();
    }

}
