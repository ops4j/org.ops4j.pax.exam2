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
import java.util.Collection;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfoMap;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * Abstract class for implementations based on an {@link ArtifactInfoMap} that want to provide
 * {@link EclipseUnitSource}
 * 
 * @author Christoph Läubrich
 *
 * @param <BundleInfoContext>
 * @param <FeatureInfoContext>
 * @param <UnitInfoContext>
 */
public abstract class AbstractEclipseUnitSource<UnitInfoContext>
    extends AbstractEclipseArtifactSource<UnitInfoContext, EclipseInstallableUnit>
    implements EclipseUnitSource {

    @Override
    public final EclipseInstallableUnit unit(String id)
        throws IOException, ArtifactNotFoundException {
        return unit(id, Version.emptyVersion);
    }

    @Override
    public EclipseInstallableUnit unit(String id, VersionRange versionRange)
        throws IOException, ArtifactNotFoundException {
        ArtifactInfo<UnitInfoContext> info = getArtifactsMap().get(id, versionRange);
        if (info == null) {
            throw new ArtifactNotFoundException("unit", id, versionRange);
        }
        return getArtifact(info);
    }

    @Override
    public EclipseInstallableUnit unit(String id, Version version)
        throws IOException, ArtifactNotFoundException {
        ArtifactInfo<UnitInfoContext> info = getArtifactsMap().get(id, version);
        if (info == null) {
            throw new ArtifactNotFoundException("unit", id, version);
        }
        return getArtifact(info);
    }

    @Override
    public Collection<EclipseInstallableUnit> getAllUnits() throws IOException {
        return getIncludedArtifacts();
    }

}
