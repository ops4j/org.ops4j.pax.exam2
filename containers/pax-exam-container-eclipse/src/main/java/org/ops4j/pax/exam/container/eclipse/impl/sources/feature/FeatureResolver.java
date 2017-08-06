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
package org.ops4j.pax.exam.container.eclipse.impl.sources.feature;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption.EclipseFeatureBundle;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseFeatureSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This source in fact depends on another {@link EclipseBundleSource} but only supply the bundles
 * that are described by the given feature set
 * 
 * @author Christoph Läubrich
 *
 */
public class FeatureResolver extends BundleAndFeatureSource {

    private static final Logger LOG = LoggerFactory.getLogger(FeatureResolver.class);

    private final ContextEclipseBundleSource bundles = new ContextEclipseBundleSource();
    private final ContextEclipseFeatureSource features = new ContextEclipseFeatureSource();

    private final EclipseEnvironment environment;

    public FeatureResolver(EclipseBundleSource bundleSource, EclipseFeatureSource featureSource,
        Collection<EclipseFeatureOption> includedFeatures, EclipseEnvironment environment)
        throws ArtifactNotFoundException, IOException {
        this.environment = environment;
        for (EclipseFeatureOption feature : includedFeatures) {
            addFeature(feature, bundleSource, featureSource);
        }
    }

    private void addFeature(EclipseFeatureOption feature, EclipseBundleSource bundleSource,
        EclipseFeatureSource featureSource) throws ArtifactNotFoundException, IOException {
        if (features.addFeature(feature)) {
            LOG.info("Resolve feature {}:{}...", feature.getId(), feature.getVersion());
        }
        else {
            // we already added this feature so break out now...
            return;

        }
        for (EclipseFeatureBundle bundle : feature.getBundles()) {
            if (bundles.containsBundle(bundle)) {
                // we already have included this bundle
                continue;
            }
            if (!bundle.matches(environment)) {
                // not valid for our env...
                continue;
            }
            EclipseBundleOption resolvedBundle = bundleSource.bundle(bundle.getId());
            if (bundles.addBundle(resolvedBundle)) {
                LOG.debug("Add bundle {}:{}...", bundle.getId(), bundle.getVersion());
            }
        }
        List<EclipseFeature> included = feature.getIncluded();
        for (EclipseFeature includedFeature : included) {
            try {
                addFeature(featureSource.feature(includedFeature.getId()), bundleSource,
                    featureSource);
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
    public ContextEclipseBundleSource getBundleSource() {
        return bundles;
    }

    @Override
    public ContextEclipseFeatureSource getFeatureSource() {
        return features;
    }

}
