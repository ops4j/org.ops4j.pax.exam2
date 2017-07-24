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
import java.util.Collection;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This source in fact depends on another {@link EclipseBundleSource} but only supply the bundles
 * that are described by the given feature set
 * 
 * @author Christoph Läubrich
 *
 */
public class FeatureEclipseBundleSource
    extends AbstractEclipseFeatureSource<EclipseBundleOption, EclipseFeatureOption> {

    private static final Logger LOG = LoggerFactory.getLogger(FeatureEclipseBundleSource.class);

    private final EclipseBundleSource bundleSource;

    public FeatureEclipseBundleSource(EclipseBundleSource bundleSource,
        Collection<EclipseFeatureOption> includedFeatures)
        throws ArtifactNotFoundException, IOException {
        this.bundleSource = bundleSource;
        for (EclipseFeatureOption feature : includedFeatures) {
            addFeature(feature);
        }
    }

    private void addFeature(EclipseFeatureOption feature)
        throws ArtifactNotFoundException, IOException {
        if (getFeatures().get(feature) != null) {
            // we already added this feature so break out now...
            return;
        }
        getFeatures().add(new ArtifactInfo<EclipseFeatureOption>(feature, feature));
        for (EclipseVersionedArtifact bundle : feature.getBundles()) {
            if (getBundles().get(bundle) != null) {
                // we already have included this bundle
                continue;
            }
            EclipseBundleOption resolvedBundle = bundleSource.bundle(bundle.getId());
            getBundles().add(new ArtifactInfo<EclipseBundleOption>(resolvedBundle, resolvedBundle));
        }
        List<EclipseFeature> included = feature.getIncluded();
        for (EclipseFeature includedFeature : included) {
            try {
                if (bundleSource instanceof EclipseFeatureSource) {
                    addFeature(
                        ((EclipseFeatureSource) bundleSource).feature(includedFeature.getId()));
                }
                else if (includedFeature.isOptional()) {
                    LOG.info(
                        "ignore optional feature {}:{} because it can't be resolved without an EclipseFeatureSource",
                        includedFeature.getId(), includedFeature.getVersion());
                }
                else {
                    throw new ArtifactNotFoundException(includedFeature);
                }
            }
            catch (ArtifactNotFoundException e) {
                if (!includedFeature.isOptional()) {
                    throw e;
                }
                else {
                    LOG.info("ignore optional feature {}:{} because it can't be resolved: {}",
                        new Object[] { includedFeature.getId(), includedFeature.getVersion(),
                            e.toString() });
                }
            }
        }
    }

    @Override
    protected EclipseFeatureOption getFeature(ArtifactInfo<EclipseFeatureOption> featureInfo) {
        return featureInfo.getContext();
    }

    @Override
    protected EclipseBundleOption getBundle(ArtifactInfo<EclipseBundleOption> bundleInfo) {
        return bundleInfo.getContext();
    }

}
