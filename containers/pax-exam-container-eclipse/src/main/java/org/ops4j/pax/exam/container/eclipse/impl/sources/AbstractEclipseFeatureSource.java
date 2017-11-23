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
package org.ops4j.pax.exam.container.eclipse.impl.sources;

import java.io.IOException;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfoMap;
import org.osgi.framework.Version;
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
public abstract class AbstractEclipseFeatureSource<FeatureInfoContext> extends
    AbstractEclipseArtifactSource<ArtifactInfo<FeatureInfoContext>, FeatureInfoContext, EclipseFeatureOption>
    implements EclipseFeatureSource {

    @Override
    public final EclipseFeatureOption feature(String featureId)
        throws IOException, ArtifactNotFoundException {
        return feature(featureId, Version.emptyVersion);
    }

    @Override
    public final EclipseFeatureOption feature(String featureId, VersionRange featureVersionRange)
        throws IOException, ArtifactNotFoundException {
        ArtifactInfo<FeatureInfoContext> info = get(featureId, featureVersionRange);
        if (info == null) {
            throw new ArtifactNotFoundException("feature", featureId, featureVersionRange);
        }
        return getArtifact(info);
    }

    @Override
    public EclipseFeatureOption feature(String featureId, Version featureVersion)
        throws IOException, ArtifactNotFoundException {
        ArtifactInfo<FeatureInfoContext> info = get(featureId, featureVersion);
        if (info == null) {
            throw new ArtifactNotFoundException("feature", featureId, featureVersion);
        }
        return getArtifact(info);
    }

}
