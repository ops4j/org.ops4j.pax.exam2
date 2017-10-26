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
package org.ops4j.pax.exam.container.eclipse.impl.sources.directory;

import java.io.File;

import org.ops4j.pax.exam.container.eclipse.EclipseDirectoryLayout;

/**
 * A directory layout that has a flat representation for plugins and features
 * 
 * @author Christoph Läubrich
 *
 */
public class FlatFolderEclipseDirectoryLayout implements EclipseDirectoryLayout {

    private final EclipseDirectoryLayout layout;

    public FlatFolderEclipseDirectoryLayout(EclipseDirectoryLayout layout) {
        this.layout = layout;
    }

    @Override
    public File getBaseFolder() {
        return layout.getBaseFolder();
    }

    @Override
    public File getPluginFolder() {
        return layout.getBaseFolder();
    }

    @Override
    public File getConfigurationFolder() {
        return layout.getConfigurationFolder();
    }

    @Override
    public File getFeaturesFolder() {
        return layout.getFeaturesFolder();
    }

    @Override
    public File getP2Folder() {
        return layout.getP2Folder();
    }

}
