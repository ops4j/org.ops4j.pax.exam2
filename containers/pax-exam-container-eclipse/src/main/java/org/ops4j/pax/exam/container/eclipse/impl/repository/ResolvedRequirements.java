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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitProviding;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitRequirement;

/**
 * Class that keeps track of resolved and failed requirements in a resolving process. This is needed
 * for performance, so one item is not resolved several times as well as detect (and break) cycles
 * 
 * @author Christoph Läubrich
 *
 */
public final class ResolvedRequirements {

    private final Set<String> resolved = new HashSet<>();

    private final Set<String> failed = new HashSet<>();
    private final Set<String> units = new HashSet<>();
    private final List<UnitProviding> provided = new ArrayList<>();

    public boolean isFailed(UnitRequirement requires) {
        return failed.contains(requires.getID());
    }

    public void addFailed(UnitRequirement requirement) {
        failed.add(requirement.getID());
    }

    public boolean isResolved(UnitRequirement requires) {
        if (resolved.contains(requires.getID())) {
            return true;
        }
        for (UnitProviding p : provided) {
            if (requires.matches(p)) {
                resolved.add(requires.getID());
                return true;
            }
        }
        return false;
    }

    public void addUnit(EclipseInstallableUnit unit) {
        units.add(getUnitId(unit));
        provided.addAll(unit.getProvided());
    }

    public boolean containsUnit(EclipseInstallableUnit unit) {
        return units.contains(getUnitId(unit));
    }

    private String getUnitId(EclipseInstallableUnit unit) {
        EclipseUnitSource source = unit.getSource();
        return unit.getId() + ":" + unit.getVersion() + ":" + source.hashCode();
    }

}
