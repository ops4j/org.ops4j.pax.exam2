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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption.EclipseFeatureBundle;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.CacheableSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.directory.DirectoryResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This source in fact depends on another {@link EclipseBundleSource} but only supply the bundles
 * that are described by the given feature set
 * 
 * @author Christoph Läubrich
 *
 */
public class FeatureResolver extends BundleAndFeatureSource implements CacheableSource {

    private static final Logger LOG = LoggerFactory.getLogger(FeatureResolver.class);

    private final AbstractEclipseBundleSource<?> bundles;
    private final AbstractEclipseFeatureSource<?> features;

    private FeatureResolver(DirectoryResolver directoryResolver) {
        bundles = directoryResolver.getBundleSource();
        features = directoryResolver.getFeatureSource();
    }

    public FeatureResolver(EclipseBundleSource bundleSource, EclipseFeatureSource featureSource,
        Collection<EclipseFeatureOption> includedFeatures, EclipseEnvironment environment)
        throws ArtifactNotFoundException, IOException {
        ContextEclipseBundleSource bundles = new ContextEclipseBundleSource();
        ContextEclipseFeatureSource features = new ContextEclipseFeatureSource();
        for (EclipseFeatureOption feature : includedFeatures) {
            addFeature(feature, bundleSource, featureSource, bundles, features, environment);
        }
        this.bundles = bundles;
        this.features = features;
    }

    private static void addFeature(EclipseFeatureOption feature, EclipseBundleSource bundleSource,
        EclipseFeatureSource featureSource, ContextEclipseBundleSource bundles,
        ContextEclipseFeatureSource features, EclipseEnvironment environment)
        throws ArtifactNotFoundException, IOException {
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
                LOG.debug("Skip bundle {}:{} it does not match environment...", bundle.getId(),
                    bundle.getId());
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
                    featureSource, bundles, features, environment);
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
    public AbstractEclipseBundleSource<?> getBundleSource() {
        return bundles;
    }

    @Override
    public AbstractEclipseFeatureSource<?> getFeatureSource() {
        return features;
    }

    public List<EclipseBundleOption> getResolvedBundles() throws IOException {
        return bundles.getIncludedArtifacts();
    }

    public List<EclipseFeatureOption> getResolvedFeatures() throws IOException {
        return features.getIncludedArtifacts();
    }

    @Override
    public void writeToFolder(Properties metadata, File cacheFolder) throws IOException {
        DirectoryResolver.storeToFolder(cacheFolder, getResolvedBundles(), getResolvedFeatures());
    }

    public static FeatureResolver restoreFromCache(Properties metadata, File cacheFolder)
        throws IOException {
        return new FeatureResolver(new DirectoryResolver(cacheFolder));
    }

}
