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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleSource;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Eclipse-Installation based bundle source
 * 
 * @author Christoph Läubrich
 *
 */
public final class InstallationEclipseBundleSource implements EclipseBundleSource {

    private static final Logger LOG = LoggerFactory
        .getLogger(InstallationEclipseBundleSource.class);

    private final BundleInfoMap<File> bundleMap = new BundleInfoMap<>();

    public InstallationEclipseBundleSource(File baseFolder) throws IOException {
        File pluginsFolder = new File(baseFolder, "plugins/");
        if (!pluginsFolder.exists()) {
            pluginsFolder = baseFolder;
        }
        if (!pluginsFolder.exists()) {
            throw new FileNotFoundException(
                "Folder " + baseFolder.getAbsolutePath() + " not found!");
        }
        for (File file : pluginsFolder.listFiles()) {
            if (file.isDirectory()) {
                readExplodedBundle(file);
            }
            else if (file.getName().toLowerCase().endsWith(".jar")) {
                try {
                    try (JarFile jarFile = new JarFile(file)) {
                        BundleInfo<File> bundleInfo = new BundleInfo<File>(jarFile.getManifest(),
                            file);
                        LOG.info("Add bundle {}...", bundleInfo);
                        bundleMap.add(bundleInfo);
                    }
                }
                catch (IOException e) {
                    LOG.warn("can't read jar file {} ({})", file, e.toString());
                }
            }
        }
    }

    private void readExplodedBundle(File file) {
        File metaInf = new File(file, "META-INF");
        if (metaInf.exists()) {
            try (FileInputStream is = new FileInputStream(new File(metaInf, "MANIFEST.MF"))) {
                Manifest manifest = new Manifest(is);
                BundleInfo<File> bundleInfo = new BundleInfo<File>(manifest, file);
                LOG.info("Add exploded bundle {}...", bundleInfo);
                bundleMap.add(bundleInfo);
            }
            catch (IOException e) {
                LOG.warn("Can't read manifest for exploded Bundle {}...", file.getName());
            }
        }
    }

    @Override
    public Option resolve(String bundleName, String bundleVersion) throws BundleNotFoundException {
        File file = bundleMap.getContext(bundleName, Version.parseVersion(bundleVersion));
        if (file == null) {
            throw new BundleNotFoundException(
                "Can't resolve bundle '" + bundleName + ":" + bundleVersion + "'");
        }
        else if (!file.exists()) {
            throw new BundleNotFoundException(
                "Can't resolve bundle '" + file.getAbsolutePath() + "' does not exists");
        }
        return CoreOptions.bundle(file.getAbsolutePath());
    }

}
