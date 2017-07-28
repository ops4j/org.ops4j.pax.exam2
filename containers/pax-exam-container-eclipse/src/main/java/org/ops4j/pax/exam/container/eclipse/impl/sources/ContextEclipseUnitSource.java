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

import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;

/**
 * An {@link EclipseUnitSource} that uses the context as its unit
 * 
 * @author Christoph Läubrich
 *
 */
public class ContextEclipseUnitSource extends AbstractEclipseUnitSource<EclipseInstallableUnit> {

    public boolean addUnit(EclipseInstallableUnit unit) {
        return add(new ArtifactInfo<EclipseInstallableUnit>(unit, unit));
    }

    @Override
    protected EclipseInstallableUnit getArtifact(ArtifactInfo<EclipseInstallableUnit> info)
        throws IOException {
        return info.getContext();
    }

    public boolean containsUnit(EclipseVersionedArtifact unit) {
        return contains(unit);
    }

}
