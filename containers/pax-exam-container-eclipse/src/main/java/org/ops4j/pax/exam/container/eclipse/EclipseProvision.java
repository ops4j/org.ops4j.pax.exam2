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
import java.io.InputStream;

import org.ops4j.pax.exam.options.CompositeOption;

/**
 *
 * defines several options to provision bundles from
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseProvision extends CompositeOption {

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
     * Provisions bundles from a bundle file as defined by the
     * https://wiki.eclipse.org/Configurator#SimpleConfigurator
     * 
     * @param bundleFile
     *            the bundle file to read, normally located at
     *            configuration\org.eclipse.equinox.simpleconfigurator\bundles.info
     * @return
     * @throws IOException
     *             if reading failed
     */
    EclipseProvision simpleconfigurator(InputStream bundleFile) throws IOException;

    EclipseProvision product(InputStream productDefinition) throws IOException;

    EclipseProvision feature(EclipseFeature feature) throws IOException;

    EclipseProvision units(IncludeMode mode, EclipseInstallableUnit... units) throws IOException;

    EclipseProvision singletonConflictResolution(
        SingletonConflictResolution singletonConflictResolution);

}
