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
package org.ops4j.pax.exam.container.eclipse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleSource.BundleNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleSource.EclipseProjectSource;
import org.ops4j.pax.exam.container.eclipse.impl.BundleInfo;
import org.ops4j.pax.exam.container.eclipse.impl.DirectoryEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.EclipseApplicationImpl;
import org.ops4j.pax.exam.container.eclipse.impl.TargetEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.WorkspaceEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProductParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProductParser.PluginConfiguration;
import org.ops4j.pax.exam.options.ProvisionControl;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static Options to configure the EclipseContainer
 * 
 * @author Christoph Läubrich
 *
 */
public class EclipseOptions {

    private static final Logger PROVISION_LOGGER = LoggerFactory.getLogger(EclipseProvision.class);

    private static final class EclipseLauncherImpl implements EclipseLauncher {

        private final boolean forked;

        private EclipseLauncherImpl(boolean forked) {
            this.forked = forked;
        }

        @Override
        public boolean isForked() {
            return forked;
        }

        @Override
        public EclipseApplication ignoreApp() {
            return new EclipseApplicationImpl(this, true,
                CoreOptions.frameworkProperty("eclipse.ignoreApp").value(true));
        }

        @Override
        public EclipseApplication application(String applicationID) {
            return new EclipseApplicationImpl(this, false,
                CoreOptions.frameworkProperty("eclipse.application").value(applicationID));
        }

        @Override
        public EclipseProduct product(InputStream productFile) {
            throw new UnsupportedOperationException("not implmented yet, sorry :-(");
        }

        @Override
        public EclipseProduct product(final String productID) {
            return new EclipseProduct() {

                @Override
                public EclipseApplication application(String applicationID) {
                    return new EclipseApplicationImpl(EclipseLauncherImpl.this, false,
                        CoreOptions.frameworkProperty("eclipse.application").value(applicationID),
                        CoreOptions.frameworkProperty("eclipse.product").value(productID));
                }
            };
        }

    }

    public static EclipseLauncher launcher(final boolean forked) {
        return new EclipseLauncherImpl(forked);
    }

    /**
     * Uses a Installation-Folder to provision bundles from
     * 
     * @param folder
     * @return
     * @throws IOException
     */
    public static EclipseBundleSource fromInstallation(final File baseFolder) throws IOException {
        return new DirectoryEclipseBundleSource(baseFolder);
    }

    /**
     * Use an Eclipse-WOrkspace to provision bundles from
     * 
     * @param workspaceFolder
     * @return
     * @throws IOException
     */
    public static EclipseProjectSource fromWorkspace(final File workspaceFolder)
        throws IOException {
        return new WorkspaceEclipseBundleSource(workspaceFolder);
        // throw new UnsupportedOperationException("not yet implemented, sorry");
    }

    public static EclipseBundleSource fromTarget(InputStream targetDefinition) throws IOException {
        return new TargetEclipseBundleSource(targetDefinition);
    }

    public static EclipseBundleSource combine(final EclipseBundleSource... sources) {
        return new EclipseBundleSource() {

            @Override
            public EclipseBundleOption bundle(String bundleName, Version bundleVersion)
                throws IOException, FileNotFoundException {
                BundleNotFoundException fnfe = new BundleNotFoundException(
                    "bundle " + bundleName + ":" + bundleVersion + " not found in any sources");
                for (EclipseBundleSource fallback : sources) {
                    try {
                        return fallback.bundle(bundleName, bundleVersion);
                    }
                    catch (BundleNotFoundException ef) {
                        fnfe.addSuppressed(ef);
                    }
                }
                throw fnfe;
            }

            @Override
            public EclipseFeatureOption feature(String featureName, Version featureVersion)
                throws IOException {
                BundleNotFoundException fnfe = new BundleNotFoundException(
                    "feature " + featureName + ":" + featureVersion + " not found in any sources");
                for (EclipseBundleSource fallback : sources) {
                    try {
                        return fallback.feature(featureName, featureVersion);
                    }
                    catch (BundleNotFoundException ef) {
                        fnfe.addSuppressed(ef);
                    }
                }
                throw fnfe;
            }

        };

    }

    public static EclipseProvision provision(final EclipseBundleSource bundleSource,
        String... ignoreItems) {
        final Set<String> ignored = new HashSet<>();
        if (ignoreItems != null) {
            ignored.addAll(Arrays.asList(ignoreItems));
        }
        // We provide this by default
        ignored.add("org.eclipse.osgi");
        // We do the job of the configurator
        ignored.add("org.eclipse.equinox.simpleconfigurator");
        return new EclipseProvision() {

            List<Option> bundles = new ArrayList<>();

            Set<String> featuresAdded = new HashSet<>();

            @Override
            public EclipseProvision simpleconfigurator(InputStream bundleFile) throws IOException {
                // We parse the file and add bundle(...) Options, later we might just provision the
                // simple configurator, but then we must find a way to fetch/install it ...
                Map<String, Option> local = new HashMap<>();
                try (
                    BufferedReader reader = new BufferedReader(new InputStreamReader(bundleFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("#") || line.trim().isEmpty()) {
                            continue;
                        }
                        String[] bundleInfo = line.split(",");
                        String bsn = bundleInfo[0];
                        Version version = version(bundleInfo[1]);
                        if (isIgnored(bsn, version)) {
                            continue;
                        }
                        EclipseBundleOption bundle = bundleSource.bundle(bsn, version);
                        if (bundle instanceof ProvisionControl<?>) {
                            ProvisionControl<?> control = (ProvisionControl<?>) bundle;
                            String sl = bundleInfo[3];
                            String start = bundleInfo[4];
                            control.startLevel(Integer.parseInt(sl));
                            control.start(Boolean.valueOf(start));
                        }
                        local.put(getKey(bundle), bundle);
                    }
                }
                addAll(local);
                return this;
            }

            @Override
            public EclipseProvision product(InputStream productDefinition) throws IOException {
                Map<String, Option> local = new HashMap<>();
                ProductParser parser = new ProductParser(productDefinition);
                for (BundleInfo<PluginConfiguration> bundleInfo : parser.getPlugins()) {
                    if (isIgnored(bundleInfo.getSymbolicName(), bundleInfo.getVersion())) {
                        continue;
                    }
                    EclipseBundleOption bundle = bundleSource.bundle(bundleInfo.getSymbolicName(),
                        bundleInfo.getVersion());
                    if (bundle instanceof ProvisionControl<?>) {
                        ProvisionControl<?> control = (ProvisionControl<?>) bundle;
                        PluginConfiguration context = bundleInfo.getContext();
                        if (context == null) {
                            control.start(false);
                        }
                        else {
                            control.start(context.autoStart);
                            if (context.startLevel > 0) {
                                control.startLevel(context.startLevel);
                            }
                        }
                    }
                    local.put(getKey(bundle), bundle);
                }
                addAll(local);
                return this;
            }

            @Override
            public EclipseProvision feature(String name, Version version) throws IOException {
                Map<String, Option> localOptions = new HashMap<>();
                Set<String> localFeatures = new HashSet<>(featuresAdded);
                Set<String> localBundles = new HashSet<>();
                addFeature(bundleSource.feature(name, version), localOptions, localFeatures,
                    localBundles);
                addAll(localOptions);
                featuresAdded.addAll(localFeatures);
                return this;
            }

            private void addFeature(EclipseFeatureOption feature, Map<String, Option> localOptions,
                Set<String> localFeatures, Set<String> localBundles)
                throws BundleNotFoundException, IOException {
                String featureId = getKey(feature);
                if (localFeatures.contains(featureId)) {
                    // this feature was already added so return to prevent loops
                    return;
                }
                localFeatures.add(featureId);
                for (EclipseBundle eclipseBundle : feature.getBundles()) {
                    String bundleKey = getKey(eclipseBundle);
                    if (isIgnored(eclipseBundle.getSymbolicName(), eclipseBundle.getVersion())
                        || localBundles.contains(bundleKey)) {
                        // the bundle is either ignored or installed by another feature already...
                        continue;
                    }
                    // resolve bundl and add it to the local store
                    EclipseBundleOption bundle = bundleSource
                        .bundle(eclipseBundle.getSymbolicName(), eclipseBundle.getVersion());
                    localBundles.add(bundleKey);
                    localOptions.put(bundleKey, bundle);
                }
                List<EclipseFeature> included = feature.getIncluded();
                for (EclipseFeature includedFeature : included) {
                    try {
                        EclipseFeatureOption includedFeatureOption = bundleSource.feature(
                            includedFeature.getSymbolicName(), includedFeature.getVersion());
                        addFeature(includedFeatureOption, localOptions, localFeatures,
                            localBundles);
                    }
                    catch (BundleNotFoundException e) {
                        if (!includedFeature.isOptional()) {
                            throw e;
                        }
                        else {
                            PROVISION_LOGGER.info(
                                "ignore optional feature {} because it can't be resolved: {}",
                                getKey(includedFeature), e.toString());
                        }
                    }
                }
            }

            @Override
            public Option[] getOptions() {
                return bundles.toArray(new Option[0]);
            }

            private String getKey(EclipseBundle bundle) {
                return bundle.getSymbolicName() + ":" + bundle.getVersion();
            }

            private void addAll(Map<String, Option> local) {
                for (Entry<String, Option> entry : local.entrySet()) {
                    ignored.add(entry.getKey());
                    bundles.add(entry.getValue());
                }
            }

            private Version version(String version) {
                return (version == null || version.isEmpty()) ? Version.emptyVersion
                    : Version.parseVersion(version);
            }

            private boolean isIgnored(String bsn, Version version) {
                if (ignored.contains(bsn)) {
                    return true;
                }
                StringBuilder sb = new StringBuilder(bsn);
                sb.append(':');
                sb.append(version.getMajor());
                if (ignored.contains(sb.toString())) {
                    return true;
                }
                sb.append('.');
                sb.append(version.getMinor());
                if (ignored.contains(sb.toString())) {
                    return true;
                }
                sb.append('.');
                sb.append(version.getMicro());
                if (ignored.contains(sb.toString())) {
                    return true;
                }
                else {
                    sb.append('.');
                    sb.append(version.getQualifier());
                    return ignored.contains(sb.toString());
                }
            }

        };
    }

}
