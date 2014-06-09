/*
 * Copyright 2011 Harald Wellmann.
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
package org.ops4j.pax.exam;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.ops4j.util.property.PropertiesPropertyResolver;

/**
 * Reads configuration settings from a properties file {@code exam.properties} at the root of the
 * classpath. System properties take precedence over properties in the configuration file.
 * <p>
 * Also allows setting system properties from a properties URL.
 * 
 * @author Harald Wellmann
 * @since Dec 2011
 */
public class ConfigurationManager {

    private PropertiesPropertyResolver resolver;

    /**
     * Creates a configuration manager.
     */
    public ConfigurationManager() {
        Properties props = new Properties();
        URL url = null;
        String configurationLocation = System.getProperty(Constants.EXAM_CONFIGURATION_KEY);
        try {
            if (configurationLocation == null) {
                url = getClass().getResource(Constants.EXAM_PROPERTIES_PATH);
            }
            else {
                url = new URL(configurationLocation);
            }
            InputStream is = (url == null) ? null : url.openStream();
            if (is != null) {
                props.load(is);
                is.close();
                resolver = new PropertiesPropertyResolver(props);
            }
            resolver = new PropertiesPropertyResolver(System.getProperties(), resolver);
        }
        catch (MalformedURLException exc) {
            throw new TestContainerException(exc);
        }
        catch (IOException exc) {
            throw new TestContainerException(exc);
        }
    }

    /**
     * Returns the configuration property for the given key.
     * 
     * @param key
     *            configuration key
     * @return configuration value, or null
     */
    public String getProperty(String key) {
        return resolver.get(key);
    }

    /**
     * Returns the configuration property for the given key, or the given default value.
     * 
     * @param key
     *            configuration key
     * @param defaultValue
     *            default value for key           
     * @return configuration value, or the default value if the key is not defined
     */
    public String getProperty(String key, String defaultValue) {
        String value = resolver.get(key);
        return (value == null) ? defaultValue : value;
    }

    /**
     * Loads system properties from the given configuration key.
     * <p>
     * If this configuration key has no value, then this method has no effect.
     * <p>
     * If the value starts with {@code env:}, this prefix is stripped and the remainder is taken to
     * be an environment variable. The value is then replaced by the value of the environment
     * variable.
     * <p>
     * This value is now interpreted as a classpath resource and converted to a URL. If the value
     * has no matching classpath resource, the value itself is interpreted as a URL.
     * <p>
     * Finally, properties are loaded from this URL and merged into the current system properties.
     * 
     * @param configurationKey
     *            configuration key, the value defining a property source
     */
    public void loadSystemProperties(String configurationKey) {
        String propertyRef = getProperty(configurationKey);
        if (propertyRef == null) {
            return;
        }

        if (propertyRef.startsWith("env:")) {
            propertyRef = propertyRef.substring(4);
            propertyRef = System.getenv(propertyRef);
        }

        if (!propertyRef.startsWith("/")) {
            propertyRef = "/" + propertyRef;
        }
        try {
            URL url = getClass().getResource(propertyRef);
            if (url == null) {
                url = new URL(propertyRef);
            }
            Properties props = System.getProperties();
            props.load(url.openStream());
            System.setProperties(props);
        }
        catch (IOException exc) {
            throw new TestContainerException(exc);
        }
    }
}
