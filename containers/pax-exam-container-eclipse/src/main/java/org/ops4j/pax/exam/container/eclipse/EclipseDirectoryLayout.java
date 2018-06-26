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

import java.io.File;

/**
 * Represents the layout of a directory
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseDirectoryLayout {

    /**
     * 
     * @return the base folder
     */
    File getBaseFolder();

    /**
     * 
     * @return the folder where bundles are located
     */
    File getPluginFolder();

    /**
     * 
     * @return the configuration/framework area
     */
    File getConfigurationFolder();

    /**
     * 
     * @return the folder where features are located
     */
    File getFeaturesFolder();

    /**
     * 
     * @return p2 working directory
     */
    File getP2Folder();

}
