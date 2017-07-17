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
                try {
                    BundleInfo<File> explodedBundle = BundleInfo.readExplodedBundle(file, file);
                    LOG.info("Add exploded bundle {}...", explodedBundle);
                    bundleMap.add(explodedBundle);
                }
                catch (IOException e) {
                    LOG.warn("Can't read exploded bundle from folder {} ({})...", file.getName(),
                        e.toString());
                }
            }
            else if (file.getName().toLowerCase().endsWith(".jar")) {
                try {
                    try (JarFile jarFile = new JarFile(file)) {
                        Manifest mf = jarFile.getManifest();
                        if (BundleInfo.isBundle(mf)) {
                            BundleInfo<File> bundleInfo = new BundleInfo<File>(mf, file);
                            LOG.info("Add bundle {}...", bundleInfo);
                            bundleMap.add(bundleInfo);
                        }
                        else {
                            LOG.info("Skip file {} it is not a bundle... {}", file,
                                mf.getMainAttributes());
                        }
                    }
                }
                catch (IOException | IllegalArgumentException e) {
                    LOG.warn("can't read jar file {} ({})", file, e.toString());
                }
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
