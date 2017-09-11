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
import java.util.List;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;

/**
 *
 * defines several options to provision bundles from
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseProvision {

    public enum IncludeMode {
        /**
         * Allow to add software with missing Units
         */
        SLICER,
        /**
         * include (and require) all dependent Units
         */
        PLANNER;
    }

    public enum SingletonConflictResolution {
        /**
         * Fails the provisioning process
         */
        FAIL,
        /**
         * on conflict, use the highest version
         */
        USE_HIGHEST_VERSION,
        /**
         * keep the current one regardless of version
         */
        KEEP_CURRENT,
        /**
         * replace the current one with the conflicting one regardless of version
         */
        REPLACE_CURRENT;
    }

    /**
     * Provisions a bundle
     * 
     * @param bundle
     *            the bundle to provision
     * @return a list containing the bundle added or an empty list if it was ignored
     * @throws IOException
     */
    List<EclipseBundleOption> bundle(EclipseBundle bundle) throws IOException;

    /**
     * Provision a features
     * 
     * @param feature
     *            the feature to provision
     * @return a list of bundles that where provisioned as a result of this feature
     * @throws IOException
     */
    List<EclipseBundleOption> feature(EclipseFeature feature) throws IOException;

    /**
     * Provision the given units with the given mode
     * 
     * @param mode
     *            the mode to use
     * @param units
     *            the units to provision
     * @return a list of bundles that where provisioned as a result of this
     * @throws IOException
     */
    List<EclipseBundleOption> units(IncludeMode mode, EclipseInstallableUnit... units)
        throws IOException;

    List<EclipseBundleOption> units(IncludeMode mode, EclipseUnitSource unitSource,
        EclipseInstallableUnit... units) throws IOException;

    EclipseProvision singletonConflictResolution(
        SingletonConflictResolution singletonConflictResolution);

    Option asOption();

}
