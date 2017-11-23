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
package org.ops4j.pax.exam.container.eclipse.impl.sources.directory;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.impl.BundleArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseBundleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads Eclipse Bundles from a folder
 * 
 * @author Christoph Läubrich
 *
 */
public class DirectoryEclipseBundleSource extends AbstractEclipseBundleSource<File> {

    private static final Logger LOG = LoggerFactory.getLogger(DirectoryEclipseBundleSource.class);

    public DirectoryEclipseBundleSource() {
    }

    public DirectoryEclipseBundleSource(File directory) throws IOException {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                try {
                    if (!readPlugin(file)) {
                        LOG.info("Skip {}, it is not a bundle", file.getAbsolutePath());
                    }
                }
                catch (Exception e) {
                    LOG.warn("Can't read bundle from location {} ({})...", file.getAbsolutePath(),
                        e.toString());
                }
            }

        }
    }

    public boolean readPlugin(File file) throws IOException {
        if (file.isDirectory() && BundleArtifactInfo.isBundle(file)) {
            BundleArtifactInfo<File> explodedBundle = BundleArtifactInfo.readExplodedBundle(file,
                file);
            if (add(explodedBundle)) {
                LOG.debug("Add exploded bundle {}...", explodedBundle);
            }
            return true;
        }
        else if (file.getName().toLowerCase().endsWith(".jar")) {
            try (JarFile jarFile = new JarFile(file)) {
                Manifest mf = jarFile.getManifest();
                if (BundleArtifactInfo.isBundle(mf)) {
                    BundleArtifactInfo<File> bundleInfo = new BundleArtifactInfo<File>(mf, file);
                    if (add(bundleInfo)) {
                        LOG.debug("Add bundle {}...", bundleInfo);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected EclipseBundleOption getArtifact(BundleArtifactInfo<File> bundleInfo)
        throws IOException {
        return new DirectoryEclipseBundleOption(bundleInfo);
    }

}
