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
import org.osgi.framework.VersionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Eclipse-Installation based bundle source
 * 
 * @author Christoph Läubrich
 *
 */
public final class DirectoryEclipseBundleSource implements EclipseBundleSource {

    private static final String FEATURES_FOLDER = "features";

    private static final String PLUGINS_FOLDER = "plugins";

    private static final Logger LOG = LoggerFactory.getLogger(DirectoryEclipseBundleSource.class);

    private final BundleInfoMap<File> bundleMap = new BundleInfoMap<>();

    private final BundleInfoMap<FeatureFile> featureMap = new BundleInfoMap<>();

    private DirectoryEclipseBundleSource(File folder, boolean readPlugins, boolean readFeatures)
        throws IOException {
        String info;
        if (readFeatures && readPlugins) {
            info = "bundle/feature";
        }
        else if (readFeatures) {
            info = "feature";
        }
        else {
            info = "bundle";
        }
        File[] files = folder.listFiles();
        for (File file : files) {
            try {
                if (readFeatures) {
                    if (readFeature(file)) {
                        continue;
                    }
                }
                if (readPlugins) {
                    if (readPlugin(file)) {
                        continue;
                    }
                }
                LOG.info("Skip {}, it is not a ", info, file.getAbsolutePath());
            }
            catch (Exception e) {
                LOG.warn("Can't read {} from folder {} ({})...",
                    new Object[] { info, file.getName(), e.toString() });
            }
        }
    }

    private boolean readPlugin(File file) throws IOException {
        if (file.isDirectory() && BundleInfo.isBundle(file)) {
            BundleInfo<File> explodedBundle = BundleInfo.readExplodedBundle(file, file);
            LOG.info("Add exploded bundle {}...", explodedBundle);
            bundleMap.add(explodedBundle);
            return true;
        }
        else if (file.getName().toLowerCase().endsWith(".jar")) {
            try (JarFile jarFile = new JarFile(file)) {
                Manifest mf = jarFile.getManifest();
                if (BundleInfo.isBundle(mf)) {
                    BundleInfo<File> bundleInfo = new BundleInfo<File>(mf, file);
                    LOG.info("Add bundle {}...", bundleInfo);
                    bundleMap.add(bundleInfo);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean readFeature(File file) throws IOException {
        if (file.isDirectory()) {
            File featureFile = new File(file, BundleInfo.FEATURE_XML_LOCATION);
            if (featureFile.exists()) {
                FeatureParser featureParser = new FeatureParser(new FileInputStream(featureFile));
                BundleInfo<FeatureFile> explodedFeature = new BundleInfo<DirectoryEclipseBundleSource.FeatureFile>(
                    featureParser.getId(), featureParser.getVersion(),
                    new FeatureFile(file, featureParser));
                featureMap.add(explodedFeature);
                LOG.info("Add exploded feature {}...", explodedFeature);
                return true;
            }
        }
        else if (file.getName().toLowerCase().endsWith(".jar")) {
            try (JarFile jarFile = new JarFile(file)) {
                ZipEntry entry = jarFile.getEntry(BundleInfo.FEATURE_XML_LOCATION);
                if (entry != null) {
                    FeatureParser featureParser = new FeatureParser(jarFile.getInputStream(entry));
                    BundleInfo<FeatureFile> feature = new BundleInfo<DirectoryEclipseBundleSource.FeatureFile>(
                        featureParser.getId(), featureParser.getVersion(),
                        new FeatureFile(file, featureParser));
                    featureMap.add(feature);
                    LOG.info("Add feature {}...", feature);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, Version bundleVersion)
        throws BundleNotFoundException {
        final BundleInfo<File> bundleInfo = bundleMap.get(bundleName, bundleVersion, false);
        if (bundleInfo == null) {
            throw new BundleNotFoundException(
                "Can't resolve bundle '" + bundleName + ":" + bundleVersion + "'");
        }
        return new DirectoryEclipseBundleOption(bundleInfo);
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, VersionRange bundleVersionRange)
        throws IOException, BundleNotFoundException {
        final BundleInfo<File> bundleInfo = bundleMap.get(bundleName, bundleVersionRange);
        if (bundleInfo == null) {
            throw new BundleNotFoundException(
                "Can't resolve bundle '" + bundleName + ":" + bundleVersionRange + "'");
        }
        return new DirectoryEclipseBundleOption(bundleInfo);
    }

    @Override
    public EclipseFeatureOption feature(final String featureName, Version featureVersion)
        throws IOException, BundleNotFoundException {
        final BundleInfo<FeatureFile> bundleInfo = featureMap.get(featureName, featureVersion,
            false);
        if (bundleInfo == null) {
            throw new BundleNotFoundException(
                "Can't resolve feature '" + featureName + ":" + featureVersion + "'");
        }
        return new DirectoryEclipseFeatureOption(bundleInfo);
    }

    @Override
    public EclipseFeatureOption feature(String featureName, VersionRange featureVersionRange)
        throws IOException, BundleNotFoundException {
        final BundleInfo<FeatureFile> bundleInfo = featureMap.get(featureName, featureVersionRange);
        if (bundleInfo == null) {
            throw new BundleNotFoundException(
                "Can't resolve feature '" + featureName + ":" + featureVersionRange + "'");
        }
        return new DirectoryEclipseFeatureOption(bundleInfo);
    }

    private final class DirectoryEclipseFeatureOption
        extends AbstractEclipseFeatureOption<FeatureFile> {

        private DirectoryEclipseFeatureOption(BundleInfo<FeatureFile> bundleInfo)
            throws BundleNotFoundException {
            super(bundleInfo);
            File file = bundleInfo.getContext().file;
            if (!file.exists()) {
                throw new BundleNotFoundException(
                    "Can't resolve bundle '" + file.getAbsolutePath() + "' does not exists");
            }
        }

        @Override
        protected List<? extends EclipseFeature> getIncluded(BundleInfo<FeatureFile> bundleInfo) {
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
    }

    private final class DirectoryEclipseBundleOption extends AbstractEclipseBundleOption<File> {

        private DirectoryEclipseBundleOption(BundleInfo<File> bundleInfo)
            throws BundleNotFoundException {
            super(bundleInfo);
            final File file = bundleInfo.getContext();
            if (!file.exists()) {
                throw new BundleNotFoundException(
                    "Can't resolve bundle '" + file.getAbsolutePath() + "' does not exists");
            }
        }

        @Override
        protected Option toOption(BundleInfo<File> bundleInfo) {
            return CoreOptions.bundle(bundleInfo.getContext().getAbsolutePath());
        }
    }

    private static final class PluginFeatureEclipseBundleSource implements EclipseBundleSource {

        private DirectoryEclipseBundleSource pluginSource;
        private DirectoryEclipseBundleSource featureSource;

        public PluginFeatureEclipseBundleSource(DirectoryEclipseBundleSource pluginSource,
            DirectoryEclipseBundleSource featureSource) {
            this.pluginSource = pluginSource;
            this.featureSource = featureSource;
        }

        @Override
        public EclipseBundleOption bundle(String bundleName, Version bundleVersion)
            throws IOException, BundleNotFoundException {
            return pluginSource.bundle(bundleName, bundleVersion);
        }

        @Override
        public EclipseFeatureOption feature(String featureName, Version featureVersion)
            throws IOException, BundleNotFoundException {
            return featureSource.feature(featureName, featureVersion);
        }

        @Override
        public EclipseBundleOption bundle(String bundleName, VersionRange bundleVersionRange)
            throws IOException, BundleNotFoundException {
            return pluginSource.bundle(bundleName, bundleVersionRange);
        }

        @Override
        public EclipseFeatureOption feature(String featureName, VersionRange featureVersionRange)
            throws IOException, BundleNotFoundException {
            return featureSource.feature(featureName, featureVersionRange);
        }
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

    public static EclipseBundleSource create(File folder) throws IOException {
        if (FEATURES_FOLDER.equalsIgnoreCase(folder.getName())
            || PLUGINS_FOLDER.equalsIgnoreCase(folder.getName())) {
            folder = folder.getParentFile();
        }
        File pluginsFolder = new File(folder, PLUGINS_FOLDER);
        File featuresFolder = new File(folder, FEATURES_FOLDER);
        DirectoryEclipseBundleSource pluginSource = null;
        if (pluginsFolder.exists()) {
            pluginSource = new DirectoryEclipseBundleSource(pluginsFolder, true, false);
            if (!featuresFolder.exists()) {
                return pluginSource;
            }
        }
        if (featuresFolder.exists()) {
            DirectoryEclipseBundleSource featureSource = new DirectoryEclipseBundleSource(
                pluginsFolder, false, true);
            if (pluginSource == null) {
                return featureSource;
            }
            else {
                return new PluginFeatureEclipseBundleSource(pluginSource, featureSource);
            }
        }
        return new DirectoryEclipseBundleSource(folder, true, true);
    }

}
