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

import java.io.IOException;
import java.util.Collection;

import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.osgi.framework.Version;

/**
 * represents an "Installable Unit" that can be resolved to bundles and features (and other
 * artifacts)
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseInstallableUnit extends EclipseVersionedArtifact {

    /**
     * 
     * @return the repository this unit comes from
     */
    EclipseUnitSource getSource();

    Collection<UnitRequirement> getRequirements();

    ResolvedArtifacts resolveArtifacts(EclipseEnvironment environment)
        throws ArtifactNotFoundException, IOException;

    Collection<UnitProviding> getProvided();

    public static interface UnitProviding {

        String getName();

        String getNamespace();

        Version getVersion();
    }

    public static interface UnitRequirement {

        boolean matches(UnitProviding providing);

        boolean matches(EclipseEnvironment environment);

        String getID();

        boolean isOptional();

        boolean isGreedy();

    }

    public static interface ResolvedArtifacts extends EclipseBundleSource, EclipseFeatureSource {

        public Collection<EclipseBundleOption> getBundles() throws IOException;

        public Collection<EclipseFeatureOption> getFeatures() throws IOException;
    }

}
