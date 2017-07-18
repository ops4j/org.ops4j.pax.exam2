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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.osgi.framework.Version;

/**
 * 
 * Represents a source for bundle files that can resolve bundles
 * 
 * @author Christoph Läubrich
 *
 */
public interface EclipseBundleSource {

    /**
     * Resolves a bundle by the given name and version
     * 
     * @param bundleName
     * @param bundleVersion
     * @return
     * @throws IOException
     */
    EclipseBundleOption bundle(String bundleName, Version bundleVersion)
        throws IOException, BundleNotFoundException;

    EclipseFeatureOption feature(String featureName, Version featureVersion)
        throws IOException, BundleNotFoundException;

    /**
     * thrown when a bundle can't be resolved
     */
    public static final class BundleNotFoundException extends FileNotFoundException {

        private static final long serialVersionUID = -2162456041243828303L;

        public BundleNotFoundException(String s) {
            super(s);
        }

    }

    public interface EclipseProjectSource extends EclipseBundleSource {

        EclipseProject project(String projectName) throws BundleNotFoundException;
    }

}
