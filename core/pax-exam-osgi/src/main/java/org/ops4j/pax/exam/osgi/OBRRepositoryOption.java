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
package org.ops4j.pax.exam.osgi;

import org.ops4j.pax.exam.Option;

/**
 * represents repository configuration and bundles
 */
public interface OBRRepositoryOption {

    /**
     * Add a repository to resolve bundles against
     * 
     * @param urls
     * @return
     */
    public OBRRepositoryOption repository(String... urls);

    /**
     * end the setup phase and convert this bundle and the underlying
     * {@link OBRRepositoryOption} to an {@link Option}
     * 
     * @return
     */
    public Option toOption();

    /**
     * Add a new Bundle with the given symbolic name
     * 
     * @param symbolicName
     * @return the newly created Bundle
     */
    public OBRBundleOption bundle(String symbolicName);

    /**
     * Add a list of bundles, use {@link #bundle(String)} if you want to specify
     * a specific version
     * 
     * @param symbolicNames
     * @return
     */
    public OBRRepositoryOption bundles(String... symbolicNames);

}
