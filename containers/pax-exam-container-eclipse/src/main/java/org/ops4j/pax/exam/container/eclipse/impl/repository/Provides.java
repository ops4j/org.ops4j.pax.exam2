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

import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitProviding;
import org.osgi.framework.Version;

/**
 * Container class that holds providing information of a unit
 * 
 * @author Christoph Läubrich
 *
 */
public final class Provides implements UnitProviding {

    private final String namespace;
    private final String name;
    private final Version version;

    public Provides(String namespace, String name, Version version) {
        this.namespace = namespace;
        this.name = name;
        this.version = version;
    }

    @Override
    public String toString() {
        return "Provides:" + namespace + ":" + name + ":" + version;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getName() {
        return name;
    }

}
