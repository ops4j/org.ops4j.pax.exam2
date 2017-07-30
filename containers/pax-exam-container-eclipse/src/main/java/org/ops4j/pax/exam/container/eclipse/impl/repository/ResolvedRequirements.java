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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitProviding;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitRequirement;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfoMap;

/**
 * Class that keeps track of resolved and failed requirements in a resolving process. This is needed
 * for performance, so one item is not resolved several times as well as detect (and break) cycles
 * 
 * @author Christoph Läubrich
 *
 */
public final class ResolvedRequirements {

    private final Map<String, ArtifactInfoMap<UnitProviding>> resolvedMap = new HashMap<>();

    private final Set<String> failed = new HashSet<>();
    private final Set<String> units = new HashSet<>();

    public boolean isFailed(UnitRequirement requires) {
        return failed.contains(getID(requires));
    }

    public void addFailed(UnitRequirement requirement) {
        failed.add(getID(requirement));
    }

    private String getID(UnitRequirement requires) {
        return requires.getNamespace() + ":" + requires.getName() + ":"
            + requires.getVersionRange();
    }

    public boolean isResolved(UnitRequirement requires) {
        ArtifactInfoMap<?> resolved = resolvedMap.get(requires.getNamespace());
        return resolved != null
            && resolved.get(requires.getName(), requires.getVersionRange()) != null;
    }

    public void addResolved(Collection<? extends UnitProviding> providedRequirements) {
        for (UnitProviding provided : providedRequirements) {
            ArtifactInfoMap<UnitProviding> resolved = resolvedMap.get(provided.getNamespace());
            if (resolved == null) {
                resolved = new ArtifactInfoMap<>();
                resolvedMap.put(provided.getNamespace(), resolved);
            }
            if (resolved.get(provided.getName(), provided.getVersion()) == null) {
                resolved.add(new ArtifactInfo<UnitProviding>(provided.getName(),
                    provided.getVersion(), provided));
            }
        }
    }

    public void addUnit(EclipseInstallableUnit unit) {
        units.add(getUnitId(unit));
    }

    public boolean containsUnit(EclipseInstallableUnit unit) {
        return units.contains(getUnitId(unit));
    }

    private String getUnitId(EclipseInstallableUnit unit) {
        EclipseUnitSource source = unit.getSource();
        return unit.getId() + ":" + unit.getVersion() + ":" + source.hashCode();
    }

}
