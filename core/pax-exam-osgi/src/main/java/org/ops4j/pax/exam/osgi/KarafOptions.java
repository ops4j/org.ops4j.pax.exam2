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
import org.ops4j.pax.exam.osgi.internal.karaf.KarafFeatureProvisionOption;

/**
 * This class gives access to several Karaf provisioning options
 */
public class KarafOptions {

    public KarafOptions() {
        //we are just static
    }

    /**
     * Creates a {@link KarafFeatureOption} for the given repositoryUrl
     * 
     * @param repositoryUrl
     * @return
     */
    public static KarafFeatureOption features(String repositoryUrl) {
        return new KarafFeatureProvisionOption(repositoryUrl);
    }

    /**
     * Creates a {@link KarafFeatureOption} for the given repositoryUrl, init it
     * with the feature names and convert it to an option
     * 
     * @param repositoryUrl
     * @param features
     * @return
     */
    public static Option features(String repositoryUrl, String... features) {
        return features(repositoryUrl).add(features).toOption();
    }

    /**
     * Creates a {@link KarafFeatureOption} for the given repositoryUrl, init it
     * with the feature names and {@link WorkingDirectoryOption} and convert it
     * to an option
     * 
     * @param repositoryUrl
     * @param features
     * @return
     */
    public static Option features(String repositoryUrl, WorkingDirectoryOption workingDirectoryOption, String... features) {
        return features(repositoryUrl).add(features).workingDir(workingDirectoryOption).toOption();
    }
}
