/*
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
package org.ops4j.pax.exam.container.eclipse.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment.ModifiableEclipseEnvironment;
import org.osgi.framework.Filter;

/**
 * Implementation that used a map as backing source and inits with systemproperties
 * 
 * @author Christoph Läubrich
 *
 */
public class DefaultEclipseEnvironment implements ModifiableEclipseEnvironment {

    private final Map<String, String> properties;

    public DefaultEclipseEnvironment() {
        this(initWithSystemProperties(), true);
    }

    public DefaultEclipseEnvironment(Map<String, String> initialProperties) {
        this(new HashMap<>(initialProperties), true);
    }

    public DefaultEclipseEnvironment(Map<String, String> initialProperties, boolean readOnly) {
        properties = readOnly ? Collections.unmodifiableMap(initialProperties)
            : new HashMap<>(initialProperties);
    }

    @Override
    public void set(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public boolean matches(String value, String... keys) {
        for (String key : keys) {
            if (value.equals(properties.get(key))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(Filter filter) {
        if (filter == null) {
            return true;
        }
        return filter.matches(properties);
    }

    @Override
    public ModifiableEclipseEnvironment copy() {
        return new DefaultEclipseEnvironment(properties, false);
    }

    private static Map<String, String> initWithSystemProperties() {
        Map<String, String> map = new HashMap<>();
        Properties properties = System.getProperties();
        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }
        return map;
    }

}
