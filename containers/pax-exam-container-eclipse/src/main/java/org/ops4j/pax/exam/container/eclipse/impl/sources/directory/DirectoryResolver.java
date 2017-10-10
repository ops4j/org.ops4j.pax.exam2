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
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallation;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.CacheableSource;
import org.ops4j.pax.exam.options.StreamReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Eclipse-Installation based bundle source
 * 
 * @author Christoph Läubrich
 *
 */
public final class DirectoryResolver extends BundleAndFeatureSource
    implements EclipseInstallation, CacheableSource {

    private static final String DIRECTORY_KEY = DirectoryResolver.class.getName() + ".directory";

    private static final String FEATURES_FOLDER = "features";

    private static final String PLUGINS_FOLDER = "plugins";

    private static final Logger LOG = LoggerFactory.getLogger(DirectoryResolver.class);

    private final DirectoryEclipseBundleSource bundleSource;

    private final DirectoryEclipseFeatureSource featureSource;

    private final File location;

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
        this.location = folder;
    }

    @Override
    public File getDirectory() {
        return location;
    }

    @Override
    public DirectoryEclipseBundleSource getBundleSource() {
        return bundleSource;
    }

    @Override
    public DirectoryEclipseFeatureSource getFeatureSource() {
        return featureSource;
    }

    @Override
    public void writeToFolder(Properties metadata, File cacheFolder) throws IOException {
        metadata.setProperty(DIRECTORY_KEY, getDirectory().getAbsolutePath());
    }

    public static DirectoryResolver restoreFromCache(Properties metadata, File cacheFolder)
        throws IOException {
        String property = metadata.getProperty(DIRECTORY_KEY);
        if (property == null) {
            throw new IllegalStateException("property " + DIRECTORY_KEY + " is missing!");
        }
        return new DirectoryResolver(new File(property));
    }

    /**
     * Stores the given collection of bundles and features to the given folder in a way so it can be
     * read back by the directory resolver. All bundles and features must be able to be transformed
     * to an input stream via the {@link StreamReference} interface or an exception is raised!
     * 
     * @param folder
     *            the folder to store the artifacts to
     * @param bundles
     *            the bundles to store
     * @param features
     *            the features to store
     * @throws IOException
     *             if an I/O error occurs
     */
    public static void storeToFolder(File folder, Collection<? extends EclipseBundle> bundles,
        Collection<? extends EclipseFeature> features) throws IOException {
        File pluginsFolder = new File(folder, PLUGINS_FOLDER);
        File featuresFolder = new File(folder, FEATURES_FOLDER);
        FileUtils.forceMkdir(pluginsFolder);
        FileUtils.forceMkdir(featuresFolder);
        for (EclipseFeature feature : features) {
            if (feature instanceof StreamReference) {
                try (InputStream stream = ((StreamReference) feature).createStream()) {
                    FileUtils.copyInputStreamToFile(stream,
                        new File(featuresFolder, getFileName(feature)));
                }
            }
            else {
                throw new IllegalArgumentException("feature " + createWrongTypeMsg(feature));
            }
        }

        for (EclipseBundle bundle : bundles) {
            if (bundle instanceof StreamReference) {
                try (InputStream stream = ((StreamReference) bundle).createStream()) {
                    FileUtils.copyInputStreamToFile(stream,
                        new File(pluginsFolder, getFileName(bundle)));
                }
            }
            else {
                throw new IllegalArgumentException("bundle " + createWrongTypeMsg(bundle));
            }
        }
    }

    private static String createWrongTypeMsg(EclipseVersionedArtifact artifact) {
        return artifact.getId() + ":" + artifact.getVersion() + " of type "
            + artifact.getClass().getName() + " does not implement required interface "
            + StreamReference.class.getName();
    }

    private static String getFileName(EclipseVersionedArtifact artifact) {
        return artifact.getId() + "_" + artifact.getVersion() + ".jar";
    }

}
