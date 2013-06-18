/*
 * Copyright 2013 Harald Wellmann
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
package org.ops4j.pax.exam.karaf.options;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.RawUrlReference;
import org.ops4j.pax.exam.options.UrlReference;

/**
 * Option for installing one or more features from a Karaf features descriptor. 
 * <p>
 * Tests should use static methods from {@link KarafDistributionOption} instead of this class.
 * 
 * @author Harald Wellmann
 *
 */
public class KarafFeaturesOption implements Option, UrlReference  {

    private String[] features;
    private UrlReference urlReference;

    /**
     * Constructor.
     * 
     * @param repositoryUrl
     *            url of features respository (cannot be null or empty)
     * @param features
     *            features to be installed
     * 
     */
    public KarafFeaturesOption(final String repositoryUrl, final String... features) {
        this.urlReference = new RawUrlReference(repositoryUrl);
        this.features = features;
    }

    /**
     * Constructor.
     * 
     * @param repositoryUrl
     *            url of features respository
     * @param features
     *            features to be installed
     */
    public KarafFeaturesOption(final UrlReference repositoryUrl,
        final String... features) {
        this.urlReference = repositoryUrl;
        this.features = features;
    }
    
    public String[] getFeatures() {
        return features;
    }

    @Override
    public String getURL() {
        return urlReference.getURL();
    }
}
