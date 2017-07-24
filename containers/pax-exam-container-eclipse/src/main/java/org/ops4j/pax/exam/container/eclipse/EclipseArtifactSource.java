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

import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * 
 * Represents a source for bundle files that can resolve bundles
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseArtifactSource {

    public static final VersionRange HIGHEST_VERSION = new VersionRange(VersionRange.LEFT_CLOSED,
        Version.emptyVersion, null, VersionRange.RIGHT_CLOSED);

    public static interface EclipseBundleSource extends EclipseArtifactSource {

        /**
         * Resolves a bundle by the given name with the highest version
         * 
         * @param bundleSymbolicName
         *            the bundle symbolic name
         * @return the resolved bundle
         * @throws IOException
         *             on I/O error
         * @throws ArtifactNotFoundException
         *             if no bundle with the name could be found
         */
        EclipseBundleOption bundle(String bundleSymbolicName)
            throws IOException, ArtifactNotFoundException;

        /**
         * Resolves a bundle by the given name with the highest version matching the given version
         * range
         * 
         * @param bundleSymbolicName
         *            the bundle symbolic name
         * @param bundleVersionRange
         *            the version range to match
         * @return the resolved bundle
         * @throws IOException
         *             on I/O error
         * @throws ArtifactNotFoundException
         *             if no bundle with the name could be found
         */
        EclipseBundleOption bundle(String bundleSymbolicName, VersionRange bundleVersionRange)
            throws IOException, ArtifactNotFoundException;
    }

    public static interface EclipseFeatureSource extends EclipseBundleSource {

        /**
         * Resolves a feature by the given name with the highest version
         * 
         * @param featureId
         *            the ID of the feature
         * @return the resolved feature
         * @throws IOException
         *             on I/O error
         * @throws ArtifactNotFoundException
         *             if no feature with the name could be found
         */
        EclipseFeatureOption feature(String featureId)
            throws IOException, ArtifactNotFoundException;

        /**
         * Resolves a feature by the given name with the highest version matching the given version
         * range
         * 
         * @param featureId
         *            the feature ID
         * @param featureVersionRange
         *            the version range to match
         * @return the resolved feature
         * @throws IOException
         *             on I/O error
         * @throws ArtifactNotFoundException
         *             if no feature with the name could be found
         */
        EclipseFeatureOption feature(String featureId, VersionRange featureVersionRange)
            throws IOException, ArtifactNotFoundException;
    }

    /**
     * a source that can supply raw eclipse projects also
     */
    public static interface EclipseProjectSource extends EclipseFeatureSource {

        EclipseProject project(String projectName) throws ArtifactNotFoundException;
    }

    /**
     * 
     * a source that can supply IUs (installable units)
     */
    public static interface EclipseUnitSource extends EclipseFeatureSource {

        /**
         * 
         * @param id
         *            the id of the IU to return
         * @return the IU for the given id with the hihest version
         * @throws ArtifactNotFoundException
         *             if this unit was not found in this source
         */
        EclipseInstallableUnit unit(String id) throws IOException, ArtifactNotFoundException;

        /**
         * 
         * @param id
         *            the id of the IU to return
         * @param versionRange
         *            the versionrange
         * @throws ArtifactNotFoundException
         *             if this unit was not found in this source
         */
        EclipseInstallableUnit unit(String id, VersionRange versionRange)
            throws IOException, ArtifactNotFoundException;

        /**
         * 
         * @return all units available from this source
         * @throws IOException
         */
        Collection<EclipseInstallableUnit> getAllUnits() throws IOException;
    }

}
