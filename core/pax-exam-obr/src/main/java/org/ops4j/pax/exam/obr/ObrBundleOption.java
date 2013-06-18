/*
 * Copyright 2013 Christoph Läubrich
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
package org.ops4j.pax.exam.obr;

import org.ops4j.pax.exam.Option;

/**
 * allows to specify a version to use when install the bundle via OBR
 */
public interface ObrBundleOption {

    /**
     * restrict this bundle to the given version
     * 
     * @param version
     * @return the {@link ObrRepositoryOption} for further adding of bundles
     */
    ObrRepositoryOption version(String version);

    /**
     * Add a new Bundle with the given symbolic name
     * 
     * @param symbolicName
     * @return the newly created Bundle
     */
    ObrBundleOption bundle(String symbolicName);

    /**
     * end the setup phase and convert this bundle and the underlying {@link ObrRepositoryOption} to
     * an {@link Option}
     * 
     * @return
     */
    Option toOption();

}
