/*
 * Copyright 2009 Alin Dreghiciu.
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
package org.ops4j.pax.exam.options;

/**
 * Provision control options.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 27, 2009
 */
public interface ProvisionControl<T extends ProvisionControl>
{

    /**
     * If the provisioned bundle(s) should be updated (re-downloaded).
     * By default bundles should be updated.
     *
     * @return true if the bundle(s) should be updated, false otherwise.
     */
    boolean shouldUpdate();

    /**
     * If the provisioned bundle(s) should be started.
     * By default bundles should be started.
     *
     * @return true if the bundle(s) should be started, false otherwise.
     */
    boolean shouldStart();

    /**
     * The start level for the provisioned bundle(s).
     *
     * @return start level.
     *         If the returned value is null, default behavior will be used
     */
    Integer getStartLevel();

    /**
     * Setter.
     *
     * @param shouldUpdate true if the provisioned bundle(s) should be updated, false otherwise
     *
     * @return itself, for fluent api usage
     */
    T update( Boolean shouldUpdate );

    /**
     * Setter. Specifyies that the provisioned bundle(s) should be updated.
     *
     * @return itself, for fluent api usage
     */
    T update();

    /**
     * Setter. Specifyies that the provisioned bundle(s) should not be updated.
     *
     * @return itself, for fluent api usage
     */
    T noUpdate();

    /**
     * Setter.
     *
     * @param shouldStart true if the provisioned bundle(s) should be started, false otherwise
     *
     * @return itself, for fluent api usage
     */
    T start( Boolean shouldStart );

    /**
     * Setter. Specifyies that the provisioned bundle(s) should be started.
     *
     * @return itself, for fluent api usage
     */
    T start();

    /**
     * Setter. Specifyies that the provisioned bundle(s) should not be started.
     *
     * @return itself, for fluent api usage
     */
    T noStart();

    /**
     * Setter.
     *
     * @param startLevel start level of the provisioned bundle(s)
     *
     * @return itself, for fluent api usage
     */
    T startLevel( Integer startLevel );
}
