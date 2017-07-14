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

import org.ops4j.pax.exam.Option;

/**
 *
 * defines several options to provision bundles from
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseProvision {

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
    Option simpleconfigurator(InputStream bundleFile) throws IOException;
}
