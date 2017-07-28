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

import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;

/**
 * An {@link EclipseFeatureSource} that uses the context as its option
 * 
 * @author Christoph Läubrich
 *
 */
public class ContextEclipseFeatureSource
    extends AbstractEclipseFeatureSource<EclipseFeatureOption> {

    @Override
    protected EclipseFeatureOption getArtifact(ArtifactInfo<EclipseFeatureOption> info)
        throws IOException {
        return info.getContext();
    }

    public boolean addFeature(EclipseFeatureOption featureOption) {
        return add(new ArtifactInfo<EclipseFeatureOption>(featureOption, featureOption));
    }

    public boolean containsFeature(EclipseVersionedArtifact feature) {
        return contains(feature);
    }
}
