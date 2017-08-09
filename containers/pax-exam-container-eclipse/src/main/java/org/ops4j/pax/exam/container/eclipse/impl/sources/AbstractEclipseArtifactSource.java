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
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * Abstract class for implementations based on an {@link ArtifactInfoMap}
 * 
 * @author Christoph Läubrich
 *
 * @param <ArtifactInfoContext>
 */
public abstract class AbstractEclipseArtifactSource<ArtifactInfoType extends ArtifactInfo<ArtifactInfoContext>, ArtifactInfoContext, ArtifactOption extends EclipseVersionedArtifact>
    implements EclipseArtifactSource {

    private ArtifactInfoMap<ArtifactInfoContext> artifactsMap;

    /**
     * 
     * @return all artifacts this source contains
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public final List<ArtifactOption> getIncludedArtifacts() throws IOException {
        List<ArtifactOption> list = new ArrayList<>();
        for (ArtifactInfo<ArtifactInfoContext> artifactInfo : getArtifactsMap().getArtifacts()) {
            try {
                list.add(getArtifact((ArtifactInfoType) artifactInfo));
            }
            catch (ArtifactNotFoundException e) {
                // just in case ... ignore it
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    protected ArtifactInfoType get(String id, Version version) {
        return (ArtifactInfoType) getArtifactsMap().get(id, version);
    }

    @SuppressWarnings("unchecked")
    protected ArtifactInfoType get(String id, VersionRange versionRange) {
        return (ArtifactInfoType) getArtifactsMap().get(id, versionRange);
    }

    protected abstract ArtifactOption getArtifact(ArtifactInfoType info) throws IOException;

    private ArtifactInfoMap<ArtifactInfoContext> getArtifactsMap() {
        if (artifactsMap == null) {
            artifactsMap = new ArtifactInfoMap<>();
        }
        return artifactsMap;
    }

    protected final boolean contains(EclipseVersionedArtifact artifact) {
        return getArtifactsMap().get(artifact) != null;
    }

    protected final boolean add(ArtifactInfoType artifactInfo) {
        ArtifactInfoMap<ArtifactInfoContext> map = getArtifactsMap();
        if (map.get(artifactInfo) == null) {
            map.add(artifactInfo);
            return true;
        }
        return false;
    }

}
