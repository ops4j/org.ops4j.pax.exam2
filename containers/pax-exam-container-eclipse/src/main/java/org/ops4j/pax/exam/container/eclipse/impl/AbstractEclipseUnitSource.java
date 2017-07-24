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
import java.util.Collection;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
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
public abstract class AbstractEclipseUnitSource<BundleInfoContext, FeatureInfoContext, UnitInfoContext>
    extends AbstractEclipseFeatureSource<BundleInfoContext, FeatureInfoContext>
    implements EclipseUnitSource, EclipseFeatureSource, EclipseBundleSource {

    private ArtifactInfoMap<UnitInfoContext> units;

    @Override
    public final EclipseInstallableUnit unit(String id)
        throws IOException, ArtifactNotFoundException {
        return unit(id, HIGHEST_VERSION);
    }

    @Override
    public EclipseInstallableUnit unit(String id, VersionRange versionRange)
        throws IOException, ArtifactNotFoundException {
        ArtifactInfo<UnitInfoContext> info = getUnits().get(id, versionRange);
        if (info == null) {
            throw new ArtifactNotFoundException("unit", id, versionRange);
        }
        return getUnit(info);
    }

    /**
     * 
     * @return all bundles that are resolved by the feature set of this source
     * @throws IOException
     */
    public final List<EclipseInstallableUnit> getIncludedUnits() throws IOException {
        ArrayList<EclipseInstallableUnit> list = new ArrayList<>();
        for (ArtifactInfo<UnitInfoContext> featureInfo : getUnits().getArtifacts()) {
            try {
                list.add(getUnit(featureInfo));
            }
            catch (ArtifactNotFoundException e) {
                // just in case ... ignore it
            }
        }
        return list;
    }

    protected abstract EclipseInstallableUnit getUnit(ArtifactInfo<UnitInfoContext> unitInfo)
        throws IOException;

    protected ArtifactInfoMap<UnitInfoContext> getUnits() {
        if (units == null) {
            units = new ArtifactInfoMap<>();
        }
        return units;
    }

    @Override
    public Collection<EclipseInstallableUnit> getAllUnits() throws IOException {
        return getIncludedUnits();
    }

}
