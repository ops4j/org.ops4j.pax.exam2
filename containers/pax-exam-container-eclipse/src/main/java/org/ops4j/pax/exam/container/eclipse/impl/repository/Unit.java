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
import java.util.Map;

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
public final class Unit implements EclipseVersionedArtifact {

    private final String id;
    private final Version version;
    private final Map<String, String> properties;
    private final List<UnitProviding> provides;
    private final List<UnitRequirement> requires;
    private final List<EclipseClassifiedVersionedArtifact> artifacts;

    public Unit(String id, Version version, Map<String, String> properties, List<Provides> provides,
        List<Requires> requires, List<Artifact> artifacts) {
        this.id = id;
        this.version = version;
        this.properties = Collections.unmodifiableMap(properties);
        this.provides = Collections.unmodifiableList(provides);
        this.requires = Collections.unmodifiableList(requires);
        this.artifacts = Collections.unmodifiableList(artifacts);
    }

    @Override
    public String toString() {
        return "Unit:" + id + ":" + version;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    public List<EclipseClassifiedVersionedArtifact> getArtifacts() {
        return artifacts;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public List<UnitProviding> getProvides() {
        return provides;
    }

    public List<UnitRequirement> getRequires() {
        return requires;
    }

}
