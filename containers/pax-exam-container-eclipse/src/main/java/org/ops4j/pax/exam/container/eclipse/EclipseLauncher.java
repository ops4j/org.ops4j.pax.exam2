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
 * The main entry point for an EclipsePlatform is the launcher
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseLauncher {

    EclipseProvision getProvision();

    /**
     * 
     * @return true if this is a forked process launcher
     */
    boolean isForked();

    /**
     * Creates an application where the main launching thread will not start the default application
     * and will simply proceed, this can be used if you want to run a default OSGi Application
     * inside the Eclipse Platform
     * 
     * @return
     */
    EclipseApplicationOption ignoreApp();

    /**
     * 
     * @param applicationID
     *            the identifier of the application to run
     * @return
     */
    EclipseApplicationOption application(String applicationID);

    /**
     * 
     * @param productID
     *            the identifier of the product being @param productID
     * @return
     */
    EclipseProduct product(String productID);

}
