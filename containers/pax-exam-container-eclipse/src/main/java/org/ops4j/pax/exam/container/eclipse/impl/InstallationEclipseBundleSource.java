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
package org.ops4j.pax.exam.container.eclipse.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleSource;

/**
 * An Eclipse-Installation based bundle source
 * 
 * @author Christoph Läubrich
 *
 */
public final class InstallationEclipseBundleSource implements EclipseBundleSource {

    private final File baseFolder;

    public InstallationEclipseBundleSource(File baseFolder) {
        this.baseFolder = baseFolder;
    }

    private Option loadBundle(File file) throws BundleNotFoundException {
        if (!file.exists()) {
            throw new BundleNotFoundException(
                "Can't resolve bundle '" + file.getAbsolutePath() + "' does not exists");
        }
        return CoreOptions.bundle(file.getAbsolutePath());
    }

    @Override
    public Option resolve(String bundleName, String bundleVersion) throws BundleNotFoundException {
        // TODO we might want to scan the folder and read the META-INF
        File file = new File(baseFolder, "plugins/" + bundleName + "_" + bundleVersion + ".jar");
        return loadBundle(file);
    }

    @Override
    public Option resolve(String bundleName, String bundleVersion, String bundleFile)
        throws IOException, FileNotFoundException {
        try {
            return resolve(bundleName, bundleVersion);
        }
        catch (BundleNotFoundException e) {
            File file = new File(baseFolder, bundleFile);
            return loadBundle(file);
        }
    }
}
