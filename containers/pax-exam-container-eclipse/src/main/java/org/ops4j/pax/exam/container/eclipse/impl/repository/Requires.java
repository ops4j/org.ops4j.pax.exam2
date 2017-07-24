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

import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.osgi.framework.VersionRange;

/**
 * Container class that holds requirement information of a unit
 * 
 * @author Christoph Läubrich
 *
 */
public final class Requires implements EclipseInstallableUnit.UnitRequirement {

    private final String namespace;
    private final String name;
    private final VersionRange versionRange;

    public Requires(String namespace, String name, VersionRange versionRange) {
        this.namespace = namespace;
        this.name = name;
        this.versionRange = versionRange;
    }

    @Override
    public String toString() {
        return "Requires:" + namespace + ":" + name + ":" + versionRange;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public VersionRange getVersionRange() {
        return versionRange;
    }

}
