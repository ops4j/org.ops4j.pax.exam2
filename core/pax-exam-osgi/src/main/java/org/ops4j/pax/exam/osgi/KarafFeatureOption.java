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
import org.ops4j.pax.exam.options.extra.WorkingDirectoryOption;

/**
 * Represents a set of karaf features to resolve
 */
public interface KarafFeatureOption {

    /**
     * Add one or more features to this option
     * 
     * @param features
     */
    KarafFeatureOption add(String... features);

    /**
     * Set the default start level if none is given in the feature descriptor (defaults to 60)
     * 
     * @param level
     * @return <code>this</code> for chaining
     */
    KarafFeatureOption defaultStartLevel(int level);

    /**
     * Set the Working directory, this is required if you want to use the configFile deploy feature
     * 
     * @param directoryOption
     * @return <code>this</code> for chaining
     */
    KarafFeatureOption workingDir(WorkingDirectoryOption directoryOption);

    /**
     * Creates an Option from the current settings that could be used in Configure methods
     * 
     * @return this configuration as an Option
     */
    Option toOption();
}
