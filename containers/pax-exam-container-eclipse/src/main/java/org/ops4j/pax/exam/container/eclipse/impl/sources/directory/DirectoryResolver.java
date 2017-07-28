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
import java.io.IOException;

import org.ops4j.pax.exam.container.eclipse.EclipseInstallation;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Eclipse-Installation based bundle source
 * 
 * @author Christoph Läubrich
 *
 */
public final class DirectoryResolver extends BundleAndFeatureSource implements EclipseInstallation {

    private static final String FEATURES_FOLDER = "features";

    private static final String PLUGINS_FOLDER = "plugins";

    private static final Logger LOG = LoggerFactory.getLogger(DirectoryResolver.class);

    private final DirectoryEclipseBundleSource bundleSource;

    private final DirectoryEclipseFeatureSource featureSource;

    public DirectoryResolver(File folder) throws IOException {
        if (FEATURES_FOLDER.equalsIgnoreCase(folder.getName())
            || PLUGINS_FOLDER.equalsIgnoreCase(folder.getName())) {
            folder = folder.getParentFile();
        }
        File pluginsFolder = new File(folder, PLUGINS_FOLDER);
        File featuresFolder = new File(folder, FEATURES_FOLDER);
        if (pluginsFolder.exists() || featuresFolder.exists()) {
            bundleSource = new DirectoryEclipseBundleSource(pluginsFolder);
            featureSource = new DirectoryEclipseFeatureSource(featuresFolder);
        }
        else {
            bundleSource = new DirectoryEclipseBundleSource();
            featureSource = new DirectoryEclipseFeatureSource();
            if (folder.exists()) {
                for (File file : folder.listFiles()) {
                    try {
                        if (featureSource.readFeature(file)) {
                            continue;
                        }
                        else if (bundleSource.readPlugin(file)) {
                            continue;
                        }
                        LOG.info("Skip {}, it is not a bundle/feature...", file.getAbsolutePath());
                    }
                    catch (Exception e) {
                        LOG.warn("Can't read bundle/feature from location {} ({})...",
                            file.getAbsolutePath(), e.toString());
                    }
                }
            }
        }

    }

    @Override
    protected EclipseBundleSource getBundleSource() {
        return bundleSource;
    }

    @Override
    protected EclipseFeatureSource getFeatureSource() {
        return featureSource;
    }

}
