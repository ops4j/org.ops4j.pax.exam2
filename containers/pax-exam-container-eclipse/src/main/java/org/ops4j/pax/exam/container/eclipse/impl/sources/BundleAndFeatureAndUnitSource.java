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
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * A compound source that is based on an {@link EclipseBundleSource}, an
 * {@link EclipseFeatureSource}
 * 
 * @author Christoph Läubrich
 *
 */
public abstract class BundleAndFeatureAndUnitSource extends BundleAndFeatureSource
    implements EclipseUnitSource {

    @Override
    public EclipseInstallableUnit unit(String id) throws IOException, ArtifactNotFoundException {
        return getUnitSource().unit(id);
    }

    @Override
    public EclipseInstallableUnit unit(String id, Version version)
        throws IOException, ArtifactNotFoundException {
        return getUnitSource().unit(id, version);
    }

    @Override
    public EclipseInstallableUnit unit(String id, VersionRange versionRange)
        throws IOException, ArtifactNotFoundException {
        return getUnitSource().unit(id, versionRange);
    }

    @Override
    public Collection<EclipseInstallableUnit> getAllUnits() throws IOException {
        return getUnitSource().getAllUnits();
    }

    protected abstract EclipseUnitSource getUnitSource();

}
