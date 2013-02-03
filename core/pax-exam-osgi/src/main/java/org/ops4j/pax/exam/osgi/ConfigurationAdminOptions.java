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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
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

    /**
     * read all configuration files (.cfg) from a folder and transform them into
     * configuration options similar to apache felix fileinstall
     * 
     * @param folder
     * @return an option containing all the read configurations
     */
    public static Option configurationFolder(File folder) {
        return configurationFolder(folder, ".cfg");
    }

    /**
     * read all configuration files from a folder and transform them into
     * configuration options
     * 
     * @param folder
     * @param extension
     *            the file extension to scan for (eg .cfg)
     * @return an option containing all the read configurations
     */
    public static Option configurationFolder(File folder, String extension) {
        if (!folder.exists()) {
            throw new TestContainerException("folder " + folder + " does not exits");
        }
        List<Option> options = new ArrayList<Option>();
        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            String name = file.getName();
            if (!name.endsWith(extension)) {
                continue;
            } else {
                name = name.substring(0, name.length() - extension.length());
            }
            String[] split = name.split("-");
            ConfigurationProvisionOption cfg = new ConfigurationProvisionOption(split[0], new HashMap<String, Object>());
            cfg.factory(split.length > 1);
            Properties properties = new Properties();
            try {
                FileInputStream stream = new FileInputStream(file);
                try {
                    properties.load(stream);
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                throw new TestContainerException("can't read configuration file " + file, e);
            }
            Set<String> names = properties.stringPropertyNames();
            for (String key : names) {
                cfg.put(key, properties.getProperty(key));
            }
            options.add(cfg.asOption());
        }
        return CoreOptions.composite(options.toArray(new Option[0]));
    }

}
