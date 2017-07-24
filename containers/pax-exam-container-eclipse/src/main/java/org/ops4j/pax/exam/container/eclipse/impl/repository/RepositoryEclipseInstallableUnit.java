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
package org.ops4j.pax.exam.container.eclipse.impl.repository;

import java.util.List;

import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.osgi.framework.Version;

/**
 * 
 * @author Christoph Läubrich
 *
 */
public class RepositoryEclipseInstallableUnit implements EclipseInstallableUnit {

    private final EclipseUnitSource repository;
    private final ArtifactInfo<Unit> unit;

    public RepositoryEclipseInstallableUnit(ArtifactInfo<Unit> artifact,
        EclipseUnitSource repository) {
        this.unit = artifact;
        this.repository = repository;
    }

    @Override
    public String getId() {
        return unit.getId();
    }

    @Override
    public Version getVersion() {
        return unit.getVersion();
    }

    @Override
    public List<UnitRequirement> getRequirements() {
        return unit.getContext().getRequires();
    }

    @Override
    public EclipseUnitSource getSource() {
        return repository;
    }

    @Override
    public String toString() {
        return unit.getContext() + ":" + repository;
    }

    @Override
    public List<EclipseClassifiedVersionedArtifact> getArtifacts() {
        return unit.getContext().getArtifacts();
    }

    @Override
    public List<UnitProviding> getProvided() {
        return unit.getContext().getProvides();
    }

}
