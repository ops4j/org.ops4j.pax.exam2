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
package org.ops4j.pax.exam.osgi.internal.configuration;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks the {@link ConfigurationAdmin} and uses {@link ConfigurationListener}
 * to be notified when configuration changes
 */
public class ConfigurationOptionConfigurationListener implements ConfigurationListener, ServiceTrackerCustomizer {

    private static final Logger       LOG = LoggerFactory.getLogger(ConfigurationOptionConfigurationListener.class);

    private final Map<String, Object> properties;
    private final BundleContext       context;
    private final String              pid;
    private final boolean             create;
    private final boolean             override;
    private final boolean             factory;

    private Configuration             factoryConfiguration;

    /**
     * @param properties
     * @param pid
     * @param factory
     * @param override
     * @param create
     */
    public ConfigurationOptionConfigurationListener(String pid, Map<String, Object> properties, BundleContext context, boolean create, boolean override,
            boolean factory) {
        this.properties = properties;
        this.context = context;
        this.pid = pid;
        this.create = create;
        this.override = override;
        this.factory = factory;
    }

    @Override
    public void configurationEvent(ConfigurationEvent event) {
        if (!factory && override && event.getPid().equals(pid)) {
            ServiceReference reference = event.getReference();
            ConfigurationAdmin service = (ConfigurationAdmin) context.getService(reference);
            if (service != null) {
                try {
                    checkIfConfigurationNeeded(service);
                } finally {
                    context.ungetService(reference);
                }
            }
        }
    }

    /**
     * Check if an override of config values is needed for the given
     * {@link ConfigurationAdmin} service, since events can occur asycrounous we
     * synchronized here to handle each event one by one since this method might
     * trigger other async events also...
     * 
     * @param service
     *            the {@link ConfigurationAdmin} to check
     */
    private synchronized void checkIfConfigurationNeeded(ConfigurationAdmin service) {
        try {
            if (factory) {
                if (factoryConfiguration == null) {
                    factoryConfiguration = service.createFactoryConfiguration(pid, null);
                    factoryConfiguration.update(new Hashtable<String, Object>(properties));
                    LOG.info("Created new factory configuration for factory-pid {}, generated pid is {} with properties: {}", new Object[] { pid,
                            factoryConfiguration.getPid(), properties });
                }
            } else {
                Configuration configuration = service.getConfiguration(pid, null);
                @SuppressWarnings("unchecked")
                Dictionary<String, Object> dictionary = configuration.getProperties();
                if (dictionary != null) {
                    boolean update = false;
                    Set<Entry<String, Object>> entrySet = properties.entrySet();
                    for (Entry<String, Object> entry : entrySet) {
                        Object object = dictionary.get(entry.getKey());
                        if (object == null) {
                            if (entry.getValue() == null) {
                                //Not changed...
                                continue;
                            }
                        } else {
                            if (object.equals(entry.getValue())) {
                                //Not changed...
                                continue;
                            }
                        }
                        //A change is detected...
                        update = true;
                        dictionary.put(entry.getKey(), entry.getValue());
                    }
                    if (update) {
                        LOG.info("Update existing configuration for pid {} with properties: {}", pid, dictToString(dictionary));
                        configuration.update(dictionary);
                    }
                } else {
                    if (create) {
                        configuration.update(new Hashtable<String, Object>(properties));
                        LOG.info("Created new configuration for pid {} with properties: {}", pid, properties);
                    }
                }
            }
        } catch (IOException e) {
            LOG.warn("can't modify configuration for PID {}", pid, e);
        }
    }

    private static Object dictToString(Dictionary<String, Object> dictionary) {
        StringBuilder sb = new StringBuilder();
        Enumeration<String> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            if (sb.length() == 0) {
                sb.append("[");
            } else {
                sb.append(", ");
            }
            String key = keys.nextElement();
            sb.append(key);
            sb.append(": ");
            sb.append(dictionary.get(key));
        }
        return sb.append("]");
    }

    @Override
    public ConfigurationAdmin addingService(ServiceReference reference) {
        ConfigurationAdmin service = (ConfigurationAdmin) context.getService(reference);
        if (service != null) {
            checkIfConfigurationNeeded(service);
        }
        return service;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
        // don't care
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        if (factoryConfiguration != null) {
            //We delete it here, just in case the ConfigAdmin is restarted so we are not ending up with two factory configs
            try {
                factoryConfiguration.delete();
            } catch (IOException e) {
                LOG.debug("Deleting factoryConfiguration failed", e);
            } catch (IllegalStateException e) {
                //Ignore... it was already deleted!
            }
        }
        context.ungetService(reference);
    }

}
