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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Eclipse-Installation based bundle source
 * 
 * @author Christoph Läubrich
 *
 */
public final class DirectoryEclipseBundleSource implements EclipseBundleSource {

    private static final Logger LOG = LoggerFactory.getLogger(DirectoryEclipseBundleSource.class);

    private final BundleInfoMap<File> bundleMap = new BundleInfoMap<>();

    private final BundleInfoMap<FeatureFile> featureMap = new BundleInfoMap<>();

    public DirectoryEclipseBundleSource(File baseFolder) throws IOException {
        File pluginsFolder = new File(baseFolder, "plugins");
        File featuresFolder = new File(baseFolder, "features");
        if (!pluginsFolder.exists() && !featuresFolder.exists()) {
            pluginsFolder = baseFolder;
            featuresFolder = baseFolder;
        }
        // TODO: we should optimize the case where pluginfolder == featuresfolder and then exclude
        // all files we have added as bundles from the set of files that are considered features, or
        // we can even merge the searchfeature/searchbundle in some way?
        if (pluginsFolder.exists()) {
            readPlugins(pluginsFolder);
        }
        if (featuresFolder.exists()) {
            readFeatures(featuresFolder);
        }
    }

    private void readFeatures(File featuresFolder) {
        for (File file : featuresFolder.listFiles()) {
            if (file.isDirectory()) {
                File featureFile = new File(file, BundleInfo.FEATURE_XML_LOCATION);
                if (featureFile.exists()) {
                    try {
                        FeatureParser featureParser = new FeatureParser(
                            new FileInputStream(featureFile));
                        BundleInfo<FeatureFile> explodedFeature = new BundleInfo<DirectoryEclipseBundleSource.FeatureFile>(
                            featureParser.getId(), featureParser.getVersion(),
                            new FeatureFile(file, featureParser));
                        featureMap.add(explodedFeature);
                        LOG.info("Add exploded feature {}...", explodedFeature);
                    }
                    catch (IOException e) {
                        LOG.warn("Can't read exploded feature from folder {} ({})...",
                            file.getName(), e.toString());
                    }
                }
            }
            else if (file.getName().toLowerCase().endsWith(".jar")) {
                try {
                    try (JarFile jarFile = new JarFile(file)) {
                        ZipEntry entry = jarFile.getEntry(BundleInfo.FEATURE_XML_LOCATION);
                        if (entry != null) {
                            FeatureParser featureParser = new FeatureParser(
                                jarFile.getInputStream(entry));
                            BundleInfo<FeatureFile> feature = new BundleInfo<DirectoryEclipseBundleSource.FeatureFile>(
                                featureParser.getId(), featureParser.getVersion(),
                                new FeatureFile(file, featureParser));
                            featureMap.add(feature);
                            LOG.info("Add feature {}...", feature);
                        }
                        else {
                            LOG.info("Skip file {} it is not a feature... ", file);
                        }
                    }
                }
                catch (IOException | IllegalArgumentException e) {
                    LOG.warn("can't read jar file {} ({})", file, e.toString());
                }
            }
        }

    }

    private void readPlugins(File pluginsFolder) {
        for (File file : pluginsFolder.listFiles()) {
            if (file.isDirectory() && BundleInfo.isBundle(file)) {
                try {
                    BundleInfo<File> explodedBundle = BundleInfo.readExplodedBundle(file, file);
                    LOG.info("Add exploded bundle {}...", explodedBundle);
                    bundleMap.add(explodedBundle);
                }
                catch (IOException e) {
                    LOG.warn("Can't read exploded bundle from folder {} ({})...", file.getName(),
                        e.toString());
                }
            }
            else if (file.getName().toLowerCase().endsWith(".jar")) {
                try {
                    try (JarFile jarFile = new JarFile(file)) {
                        Manifest mf = jarFile.getManifest();
                        if (BundleInfo.isBundle(mf)) {
                            BundleInfo<File> bundleInfo = new BundleInfo<File>(mf, file);
                            LOG.info("Add bundle {}...", bundleInfo);
                            bundleMap.add(bundleInfo);
                        }
                        else {
                            LOG.info("Skip file {} it is not a bundle... ", file);
                        }
                    }
                }
                catch (IOException | IllegalArgumentException e) {
                    LOG.warn("can't read jar file {} ({})", file, e.toString());
                }
            }
        }
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, Version bundleVersion)
        throws BundleNotFoundException {
        final BundleInfo<File> bundleInfo = bundleMap.get(bundleName, bundleVersion);
        if (bundleInfo == null) {
            throw new BundleNotFoundException(
                "Can't resolve bundle '" + bundleName + ":" + bundleVersion + "'");
        }
        final File file = bundleInfo.getContext();
        if (!file.exists()) {
            throw new BundleNotFoundException(
                "Can't resolve bundle '" + file.getAbsolutePath() + "' does not exists");
        }
        return new AbstractEclipseBundleOption<File>(bundleInfo) {

            @Override
            protected Option toOption(BundleInfo<File> bundleInfo) {
                return CoreOptions.bundle(bundleInfo.getContext().getAbsolutePath());
            }
        };
    }

    @Override
    public EclipseFeatureOption feature(final String featureName, Version featureVersion)
        throws IOException, BundleNotFoundException {
        final BundleInfo<FeatureFile> bundleInfo = featureMap.get(featureName, featureVersion);
        if (bundleInfo == null) {
            throw new BundleNotFoundException(
                "Can't resolve feature '" + featureName + ":" + featureVersion + "'");
        }
        final File file = bundleInfo.getContext().file;
        if (!file.exists()) {
            throw new BundleNotFoundException(
                "Can't resolve feature '" + file.getAbsolutePath() + "' does not exists");
        }
        return new AbstractEclipseFeatureOption<FeatureFile>(bundleInfo) {

            @Override
            protected List<? extends EclipseFeature> getIncluded(
                BundleInfo<FeatureFile> bundleInfo) {
                return bundleInfo.getContext().feature.getIncluded();
            }

            @Override
            protected List<? extends EclipseBundle> getBundles(BundleInfo<FeatureFile> bundleInfo) {
                return bundleInfo.getContext().feature.getPlugins();
            }

            @Override
            protected boolean isOptional(BundleInfo<FeatureFile> bundleInfo) {
                return false;
            }

            @Override
            protected Option toOption(BundleInfo<FeatureFile> bundleInfo) {
                return CoreOptions.bundle(bundleInfo.getContext().file.getAbsolutePath());
            }

        };
    }

    private static final class FeatureFile {

        private final File file;
        private final FeatureParser feature;

        public FeatureFile(File file, FeatureParser feature) {
            this.file = file;
            this.feature = feature;
        }

        @Override
        public String toString() {
            return file.getAbsolutePath();
        }
    }

}
