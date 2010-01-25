/*
 * Copyright 2008 Alin Dreghiciu.
 *
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
package org.ops4j.pax.exam.spi.container;

import java.io.InputStream;
import java.util.List;

/**
 * Management of an OSGi framework that can be used as a integration test container.
 * Each container is also a test target.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 09, 2008
 */
public interface TestContainer extends TestTarget
{

    /**
     * Sets the start level for a bundle.
     *
     * @param bundleId   bundle id
     * @param startLevel start level
     *
     * @throws TestContainerException if startlevel cannot be set
     */
    void setBundleStartLevel( long bundleId, int startLevel )
        throws TestContainerException;

    /**
     * Starts the test container.
     *
     * @throws TimeoutException - if timeout occured and the test container cannot be started
     */
    TestContainer start()
        throws TimeoutException;

    /**
     * Stops the test container.
     *
     * @throws TimeoutException - if timeout occured and the test container cannot be stopped
     */
    TestContainer stop()
        throws TimeoutException;

    /**
     * Waits for a bundle to be in a certain state and returns.
     *
     * @param bundleId        bundle id
     * @param state           expected state
     * @param timeoutInMillis max time to wait for state
     *
     * @throws TimeoutException - if timeout occured and expected state has not being reached
     */
    void waitForState( long bundleId, int state, long timeoutInMillis )
        throws TimeoutException;
}
