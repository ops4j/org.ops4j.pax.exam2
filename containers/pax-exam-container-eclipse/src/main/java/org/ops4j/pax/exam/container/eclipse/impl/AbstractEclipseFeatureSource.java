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

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.osgi.framework.VersionRange;

/**
 * Abstract class for implementations based on an {@link ArtifactInfoMap} that want to provide
 * {@link EclipseFeatureSource}
 * 
 * @author Christoph Läubrich
 *
 * @param <BundleInfoContext>
 * @param <FeatureInfoContext>
 */
public abstract class AbstractEclipseFeatureSource<BundleInfoContext, FeatureInfoContext>
    extends AbstractEclipseBundleSource<BundleInfoContext> implements EclipseFeatureSource {

    private ArtifactInfoMap<FeatureInfoContext> features;

    @Override
    public final EclipseFeatureOption feature(String featureId)
        throws IOException, ArtifactNotFoundException {
        return feature(featureId, HIGHEST_VERSION);
    }

    @Override
    public final EclipseFeatureOption feature(String featureId, VersionRange featureVersionRange)
        throws IOException, ArtifactNotFoundException {
        ArtifactInfo<FeatureInfoContext> info = getFeatures().get(featureId, featureVersionRange);
        if (info == null) {
            throw new ArtifactNotFoundException("feature", featureId, featureVersionRange);
        }
        return getFeature(info);
    }

    /**
     * 
     * @return all bundles that are resolved by the feature set of this source
     * @throws IOException
     */
    public final List<EclipseFeatureOption> getIncludedFeatures() throws IOException {
        ArrayList<EclipseFeatureOption> list = new ArrayList<>();
        for (ArtifactInfo<FeatureInfoContext> featureInfo : getFeatures().getArtifacts()) {
            try {
                list.add(getFeature(featureInfo));
            }
            catch (ArtifactNotFoundException e) {
                // just in case ... ignore it
            }
        }
        return list;
    }

    protected abstract EclipseFeatureOption getFeature(ArtifactInfo<FeatureInfoContext> featureInfo)
        throws IOException;

    protected ArtifactInfoMap<FeatureInfoContext> getFeatures() {
        if (features == null) {
            features = new ArtifactInfoMap<>();
        }
        return features;
    }

}
