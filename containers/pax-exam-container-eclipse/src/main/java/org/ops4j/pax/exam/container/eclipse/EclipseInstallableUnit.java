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
package org.ops4j.pax.exam.container.eclipse;

import java.util.List;

import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * represents an "Installable Unit" that can be resolved to bundles and features (and other
 * artifacts)
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseInstallableUnit extends EclipseVersionedArtifact {

    public static final String NAMESPACE_IU = "org.eclipse.equinox.p2.iu";
    public static final String NAMESPACE_BUNDLE = "osgi.bundle";
    public static final String NAMESPACE_PACKAGE = "java.package";

    /**
     * 
     * @return the repository this unit comes from
     */
    EclipseUnitSource getSource();

    List<UnitRequirement> getRequirements();

    List<EclipseClassifiedVersionedArtifact> getArtifacts();

    List<UnitProviding> getProvided();

    public static interface UnitProviding {

        String getName();

        String getNamespace();

        Version getVersion();
    }

    public static interface UnitRequirement {

        String getName();

        String getNamespace();

        VersionRange getVersionRange();

    }

}
