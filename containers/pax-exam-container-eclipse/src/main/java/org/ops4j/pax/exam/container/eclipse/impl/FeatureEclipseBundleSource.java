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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This source in fact depends on another {@link EclipseBundleSource} but only supply the bundles
 * that are described by the given feature set
 * 
 * @author Christoph Läubrich
 *
 */
public class FeatureEclipseBundleSource implements EclipseBundleSource {

    private static final Logger LOG = LoggerFactory.getLogger(FeatureEclipseBundleSource.class);

    private BundleInfoMap<EclipseFeatureOption> features = new BundleInfoMap<>();
    private BundleInfoMap<EclipseBundleOption> bundles = new BundleInfoMap<>();

    private EclipseBundleSource bundleSource;

    public FeatureEclipseBundleSource(EclipseBundleSource bundleSource,
        EclipseFeatureOption[] includedFeatures) throws BundleNotFoundException, IOException {
        this.bundleSource = bundleSource;
        for (EclipseFeatureOption feature : includedFeatures) {
            addFeature(feature);
        }
    }

    private void addFeature(EclipseFeatureOption feature)
        throws BundleNotFoundException, IOException {
        if (features.get(feature, true) != null) {
            // we already added this feature so break out now...
            return;
        }
        features.add(new BundleInfo<EclipseFeatureOption>(feature, feature));
        for (EclipseBundle bundle : feature.getBundles()) {
            if (bundles.get(bundle, true) != null) {
                // we already have included this bundle
                continue;
            }
            EclipseBundleOption resolvedBundle = bundleSource.bundle(bundle.getSymbolicName(),
                bundle.getVersion());
            bundles.add(new BundleInfo<EclipseBundleOption>(resolvedBundle, resolvedBundle));
        }
        List<EclipseFeature> included = feature.getIncluded();
        for (EclipseFeature includedFeature : included) {
            try {
                addFeature(bundleSource.feature(includedFeature.getSymbolicName(),
                    includedFeature.getVersion()));
            }
            catch (BundleNotFoundException e) {
                if (!includedFeature.isOptional()) {
                    throw e;
                }
                else {
                    LOG.info("ignore optional feature {}:{} because it can't be resolved: {}",
                        new Object[] { includedFeature.getSymbolicName(),
                            includedFeature.getVersion(), e.toString() });
                }
            }
        }
    }

    /**
     * 
     * @return all bundles that are resolved by the feature set of this source
     */
    public List<EclipseBundleOption> getIncludedBundles() {
        ArrayList<EclipseBundleOption> list = new ArrayList<>();
        for (List<BundleInfo<EclipseBundleOption>> eclipseBundleOption : bundles.asMap().values()) {
            for (BundleInfo<EclipseBundleOption> bundleInfo : eclipseBundleOption) {
                list.add(bundleInfo.getContext());
            }
        }
        return list;
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, Version bundleVersion)
        throws IOException, BundleNotFoundException {
        BundleInfo<EclipseBundleOption> info = bundles.get(bundleName, bundleVersion, false);
        if (info == null) {
            throw new BundleNotFoundException(
                "can't find feature " + bundleName + ":" + bundleVersion);
        }
        return info.getContext();
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, VersionRange bundleVersionRange)
        throws IOException, BundleNotFoundException {
        BundleInfo<EclipseBundleOption> info = bundles.get(bundleName, bundleVersionRange);
        if (info == null) {
            throw new BundleNotFoundException(
                "can't find feature " + bundleName + ":" + bundleVersionRange);
        }
        return info.getContext();
    }

    @Override
    public EclipseFeatureOption feature(String featureName, Version featureVersion)
        throws IOException, BundleNotFoundException {
        BundleInfo<EclipseFeatureOption> info = features.get(featureName, featureVersion, false);
        if (info == null) {
            throw new BundleNotFoundException(
                "can't find feature " + featureName + ":" + featureVersion);
        }
        return info.getContext();
    }

    @Override
    public EclipseFeatureOption feature(String featureName, VersionRange featureVersionRange)
        throws IOException, BundleNotFoundException {
        BundleInfo<EclipseFeatureOption> info = features.get(featureName, featureVersionRange);
        if (info == null) {
            throw new BundleNotFoundException(
                "can't find feature " + featureName + ":" + featureVersionRange);
        }
        return info.getContext();
    }

}
