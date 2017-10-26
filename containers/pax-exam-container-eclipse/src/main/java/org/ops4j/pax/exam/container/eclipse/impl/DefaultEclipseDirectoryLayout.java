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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ops4j.pax.exam.container.eclipse.EclipseDirectoryLayout;

/**
 * Represents the eclipse/equinox directory layout, even though equinox can run with other layouts
 * some eclipse features depend on it and will fail with other layouts
 * 
 * @author Christoph Läubrich
 *
 */
public class DefaultEclipseDirectoryLayout implements EclipseDirectoryLayout {

    private final File baseFolder;
    private final File pluginFolder;
    private final File configurationFolder;
    private final File featuresFolder;
    private final File p2Folder;
    private final File configIniFile;

    public DefaultEclipseDirectoryLayout(File baseFolder) {
        this.baseFolder = baseFolder;
        configurationFolder = new File(baseFolder, "configuration");
        configIniFile = new File(configurationFolder, "config.ini");
        featuresFolder = new File(baseFolder, "features");
        p2Folder = new File(baseFolder, "p2");
        pluginFolder = new File(baseFolder, "plugins");
    }

    public void create() throws IOException {
        FileUtils.forceMkdir(baseFolder);
        FileUtils.forceMkdir(configurationFolder);
        FileUtils.forceMkdir(featuresFolder);
        FileUtils.forceMkdir(p2Folder);
        FileUtils.forceMkdir(pluginFolder);
        configIniFile.createNewFile();
    }

    @Override
    public File getBaseFolder() {
        return baseFolder;
    }

    @Override
    public File getPluginFolder() {
        return pluginFolder;
    }

    @Override
    public File getConfigurationFolder() {
        return configurationFolder;
    }

    @Override
    public File getFeaturesFolder() {
        return featuresFolder;
    }

    @Override
    public File getP2Folder() {
        return p2Folder;
    }

}
