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

import java.util.Collections;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitProviding;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitRequirement;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.osgi.framework.Version;

/**
 * Container class that holds unit information of a repro and its dependecies
 * 
 * @author Christoph Läubrich
 *
 */
public final class Unit extends VersionSerializable implements EclipseVersionedArtifact {

    private static final long serialVersionUID = 4993725222726971756L;
    private final String id;
    private final List<UnitProviding> provides;
    private final List<UnitRequirement> requires;
    private final List<EclipseClassifiedVersionedArtifact> artifacts;

    public Unit(String id, Version version, List<Provides> provides, List<Requires> requires,
        List<Artifact> artifacts) {
        super(version);
        this.id = id;
        this.provides = Collections.unmodifiableList(provides);
        this.requires = Collections.unmodifiableList(requires);
        this.artifacts = Collections.unmodifiableList(artifacts);
    }

    @Override
    public String toString() {
        return "Unit:" + id + ":" + getVersion();
    }

    @Override
    public String getId() {
        return id;
    }

    public List<EclipseClassifiedVersionedArtifact> getArtifacts() {
        return artifacts;
    }

    public List<UnitProviding> getProvides() {
        return provides;
    }

    public List<UnitRequirement> getRequires() {
        return requires;
    }

}
