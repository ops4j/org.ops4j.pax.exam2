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
package org.ops4j.pax.exam.container.eclipse.internal;

import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.osgi.internal.location.EquinoxLocations;

/**
 * Represents the eclipse/equinox directory layout, even though equinox can run with other layouts
 * some eclipse features depend on it and will fail with other layouts
 * 
 * @author Christoph Läubrich
 *
 */
public class EclipseDirectoryLayout {

    private final File baseFolder;
    private final File eclipseFolder;
    private final File pluginFolder;
    private final File configurationFolder;
    private final File featuresFolder;
    private final File p2Folder;
    private final File configIniFile;

    public EclipseDirectoryLayout(File baseFolder) {
        this.baseFolder = baseFolder;
        eclipseFolder = new File(baseFolder, "eclipse");
        configurationFolder = new File(eclipseFolder, "configuration");
        configIniFile = new File(configurationFolder, "config.ini");
        featuresFolder = new File(eclipseFolder, "features");
        p2Folder = new File(eclipseFolder, "p2");
        pluginFolder = new File(eclipseFolder, "plugins");
    }

    public void create() throws IOException {
        FileUtils.forceMkdir(baseFolder);
        FileUtils.forceMkdir(eclipseFolder);
        FileUtils.forceMkdir(configurationFolder);
        FileUtils.forceMkdir(featuresFolder);
        FileUtils.forceMkdir(p2Folder);
        FileUtils.forceMkdir(pluginFolder);
        configIniFile.createNewFile();
    }

    public File getBaseFolder() {
        return baseFolder;
    }

    public File getEclipseFolder() {
        return eclipseFolder;
    }

    public File getPluginFolder() {
        return pluginFolder;
    }

    public File getConfigurationFolder() {
        return configurationFolder;
    }

    public File getFeaturesFolder() {
        return featuresFolder;
    }

    public File getP2Folder() {
        return p2Folder;
    }

    /**
     * Set the different locations into the properties required by equinox
     * 
     * @param properties
     */
    public Map<String, String> setProperties(Map<String, String> properties) {
        // # configure the different locations
        // These are all given as URIs
        properties.put(EquinoxLocations.PROP_INSTALL_AREA, getBaseFolder().toURI().toASCIIString());
        properties.put(EquinoxLocations.PROP_CONFIG_AREA,
            getConfigurationFolder().toURI().toASCIIString());
        properties.put(EquinoxLocations.PROP_INSTANCE_AREA,
            getBaseFolder().toURI().toASCIIString());
        properties.put(EquinoxLocations.PROP_HOME_LOCATION_AREA,
            getPluginFolder().toURI().toASCIIString());
        // These are all given as file names
        properties.put("osgi.syspath", getPluginFolder().getAbsolutePath());
        properties.put("osgi.logfile", new File(getBaseFolder(), "logfile.log").getAbsolutePath());
        properties.put("osgi.tracefile",
            new File(getBaseFolder(), "tracefile.log").getAbsolutePath());
        properties.put(EquinoxLocations.PROP_USER_DIR, getEclipseFolder().getAbsolutePath());
        properties.put(FRAMEWORK_STORAGE, getConfigurationFolder().toURI().toASCIIString());
        return properties;
    }

}
