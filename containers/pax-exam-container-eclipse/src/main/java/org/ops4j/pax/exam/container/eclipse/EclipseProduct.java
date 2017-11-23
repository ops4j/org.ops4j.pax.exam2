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

/**
 * 
 * An Eclipse Product defines the branding
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseProduct {

    /**
     * 
     * @param applicationID
     *            the identifier of the application to run
     * @return
     */
    EclipseApplicationOption application(String applicationID);

    /**
     * 
     * @return the launcher for this product
     */
    EclipseLauncher getLauncher();

    /**
     * 
     * @return the productID for this product or <code>null</code> if none is defined
     */
    String productID();
}
