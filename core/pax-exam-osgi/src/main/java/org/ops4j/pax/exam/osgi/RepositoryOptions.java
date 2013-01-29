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

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.osgi.internal.obr.OBRRepositoryProvisionOption;

/**
 * provides access to Repository related Options to configure test cases
 */
public class RepositoryOptions {

    /**
     * @return an option tha represents the bundles needed to resolve OBR
     *         resources. This is needed if you don't want to provide the
     *         service API and an implementation on your own, like
     *         {@link CoreOptions#junitBundles()}
     */
    public static Option obrBundles() {
        MavenArtifactProvisionOption serviceBundle = CoreOptions.mavenBundle("org.apache.felix", "org.osgi.service.obr", "1.0.2");
        serviceBundle.start(true).startLevel(1);
        MavenArtifactProvisionOption implBundle = CoreOptions.mavenBundle("org.apache.felix", "org.apache.felix.bundlerepository", "1.6.6");
        implBundle.start(true).startLevel(1);
        return CoreOptions.composite(serviceBundle, implBundle);
    }

    /**
     * Creates an {@link OBRRepositoryOption} initilized with the given
     * repository URLs
     * 
     * @param reproURLs
     * @return
     */
    public static OBRRepositoryOption obr(String... reproURLs) {
        return new OBRRepositoryProvisionOption().repository(reproURLs);
    }
}
