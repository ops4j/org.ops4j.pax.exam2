/*
 * Copyright 2009,2010 Toni Menzel.
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
package org.ops4j.pax.exam;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Toni Menzel
 * @since Jan 22, 2010
 */
public interface TestTarget {

   

    /**
     *
     * @param address the target to be called.
     * @param args optional arguments that the target might accept. Superfluous arguments are being dropped.
     * @throws ClassNotFoundException Problems
     * @throws InvocationTargetException Problems
     * @throws InstantiationException Problems
     * @throws IllegalAccessException Problems
     */
    void call( TestAddress address, Object... args ) throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException;

    /**
     * @param stream stream the content
     *
     * @return Bundle ID
     */
    long install( InputStream stream );

    /**
     * Do the cleanup operation as good as possible.
     * Like rewinding all installed probes.
     */
    void cleanup();

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
