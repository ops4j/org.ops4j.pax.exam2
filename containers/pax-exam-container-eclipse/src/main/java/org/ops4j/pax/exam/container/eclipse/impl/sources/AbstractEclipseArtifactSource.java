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
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfoMap;

/**
 * Abstract class for implementations based on an {@link ArtifactInfoMap}
 * 
 * @author Christoph Läubrich
 *
 * @param <ArtifactInfoContext>
 */
public abstract class AbstractEclipseArtifactSource<ArtifactInfoContext, ArtifactOption extends EclipseVersionedArtifact>
    implements EclipseArtifactSource {

    private ArtifactInfoMap<ArtifactInfoContext> artifactsMap;

    /**
     * 
     * @return all artifacts this source contains
     * @throws IOException
     */
    public final List<ArtifactOption> getIncludedArtifacts() throws IOException {
        List<ArtifactOption> list = new ArrayList<>();
        for (ArtifactInfo<ArtifactInfoContext> artifactInfo : getArtifactsMap().getArtifacts()) {
            try {
                list.add(getArtifact(artifactInfo));
            }
            catch (ArtifactNotFoundException e) {
                // just in case ... ignore it
            }
        }
        return list;
    }

    protected abstract ArtifactOption getArtifact(ArtifactInfo<ArtifactInfoContext> info)
        throws IOException;

    protected ArtifactInfoMap<ArtifactInfoContext> getArtifactsMap() {
        if (artifactsMap == null) {
            artifactsMap = new ArtifactInfoMap<>();
        }
        return artifactsMap;
    }

    protected final boolean contains(EclipseVersionedArtifact artifact) {
        return getArtifactsMap().get(artifact) != null;
    }

    protected final boolean add(ArtifactInfo<ArtifactInfoContext> artifactInfo) {
        ArtifactInfoMap<ArtifactInfoContext> map = getArtifactsMap();
        if (map.get(artifactInfo) == null) {
            map.add(artifactInfo);
            return true;
        }
        return false;
    }

}
