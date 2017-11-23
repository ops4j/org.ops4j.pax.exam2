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
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment.ModifiableEclipseEnvironment;
import org.osgi.framework.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation that used a map as backing source and inits with systemproperties
 * 
 * @author Christoph Läubrich
 *
 */
public class DefaultEclipseEnvironment implements ModifiableEclipseEnvironment {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEclipseEnvironment.class);
    private final Map<String, String> properties;

    public DefaultEclipseEnvironment() {
        this(initWithSystemProperties(), true);
    }

    public DefaultEclipseEnvironment(Map<String, String> initialProperties) {
        this(new HashMap<>(initialProperties), true);
    }

    private DefaultEclipseEnvironment(Map<String, String> properties, boolean readOnly) {
        if (!properties.containsKey(EclipseStarter.PROP_OS)) {
            properties.put(EclipseStarter.PROP_OS, getOS());
        }
        if (!properties.containsKey(EclipseStarter.PROP_ARCH)) {
            properties.put(EclipseStarter.PROP_ARCH, getArch());
        }
        if (!properties.containsKey(EclipseStarter.PROP_WS)) {
            properties.put(EclipseStarter.PROP_WS, getWS(properties.get(EclipseStarter.PROP_OS)));
        }
        if (!properties.containsKey(EclipseStarter.PROP_NL)) {
            properties.put(EclipseStarter.PROP_NL, getNL());
        }
        this.properties = readOnly ? Collections.unmodifiableMap(properties) : properties;
    }

    private static String getNL() {
        return Locale.getDefault().getLanguage();
    }

    private static String getArch() {
        String osArch = System.getProperties().getProperty("os.arch", "unknown").toLowerCase();
        if (osArch.contains("amd64")) {
            return "x86_64";
        }
        else if (osArch.contains("ia64")) {
            return "ia64";
        }
        else if (osArch.contains("x86")) {
            return "x86";
        }
        // TODO maybe we should use CommonsLang SystemUtils??
        // TODO other values are x86, ia64, ia64_32, ppc, PA_RISC, sparc
        LOG.warn("not recognized os.arch: {}!", osArch);
        return osArch;
    }

    private static String getOS() {
        String osName = System.getProperties().getProperty("os.name", "unknown").toLowerCase();
        if (osName.contains("windows")) {
            return "win32";
        }
        else if (osName.contains("linux")) {
            return "linux";
        }
        // TODO other values are macosx, aix, solaris, hpux, qnx
        LOG.warn("not recognized os.name: {}!", osName);
        return osName;
    }

    private static String getWS(String os) {
        if (os.equals("win32")) {
            return "win32";
        }
        else if (os.equals("linux")) {
            return "gtk";
        }
        // TODO values are windowing system motif, photon, carbon
        return "unknown";
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
        return new DefaultEclipseEnvironment(new HashMap<>(properties), false);
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
