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
package org.ops4j.pax.exam.osgi;

import java.util.HashMap;

import org.ops4j.pax.exam.osgi.internal.configuration.ConfigurationProvisionOption;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * This class allows to construct conifuration options that interact with the
 * {@link ConfigurationAdmin} service of the OSGi plattform to support creation
 * and/or modification of configuration data used in test scenarios
 */
public class ConfigurationAdminOptions {

    private ConfigurationAdminOptions() {
        //we are just static
    }

    /**
     * Creates a basic, empty configuration for the given PID
     * 
     * @param pid
     *            the pid for this configuration
     * @return empty configuration
     */
    public static ConfigurationOption newConfiguration(String pid) {
        return new ConfigurationProvisionOption(pid, new HashMap<String, Object>());
    }

    /**
     * Creates an overriding, empty configuration for the given PID
     * 
     * @param pid
     *            the pid for this configuration
     * @return empty configuration
     */
    public static ConfigurationOption overrideConfiguration(String pid) {
        return new ConfigurationProvisionOption(pid, new HashMap<String, Object>()).override(true).create(false);
    }

    /**
     * Creates a factory, empty configuration for the given PID
     * 
     * @param pid
     *            the pid for this configuration
     * @return an empty factory configuration
     */
    public static ConfigurationOption factoryConfiguration(String pid) {
        return new ConfigurationProvisionOption(pid, new HashMap<String, Object>()).factory(true);
    }

}
