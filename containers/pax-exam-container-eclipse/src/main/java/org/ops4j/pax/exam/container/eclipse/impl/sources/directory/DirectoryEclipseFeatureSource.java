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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseFeatureSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads Eclipse features from a folder
 * 
 * @author Christoph Läubrich
 *
 */
public class DirectoryEclipseFeatureSource
    extends AbstractEclipseFeatureSource<DirectoryFeatureFile> {

    private static final Logger LOG = LoggerFactory.getLogger(DirectoryEclipseFeatureSource.class);

    public DirectoryEclipseFeatureSource() {
    }

    public DirectoryEclipseFeatureSource(File directory) throws IOException {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                try {
                    if (!readFeature(file)) {
                        LOG.info("Skip {}, it is not a feature", file.getAbsolutePath());
                    }
                }
                catch (Exception e) {
                    LOG.warn("Can't read feature from location {} ({})...", file.getAbsolutePath(),
                        e.toString());
                }
            }
        }
    }

    public boolean readFeature(File file) throws IOException {
        if (file.isDirectory()) {
            File featureFile = new File(file, ArtifactInfo.FEATURE_XML_LOCATION);
            if (featureFile.exists()) {
                FeatureParser featureParser = new FeatureParser(new FileInputStream(featureFile));
                ArtifactInfo<DirectoryFeatureFile> explodedFeature = new ArtifactInfo<DirectoryFeatureFile>(
                    featureParser.getId(), featureParser.getVersion(),
                    new DirectoryFeatureFile(file, featureParser));
                if (add(explodedFeature)) {
                    LOG.debug("Add exploded feature {}...", explodedFeature);
                }
                return true;
            }
        }
        else if (file.getName().toLowerCase().endsWith(".jar")) {
            try (JarFile jarFile = new JarFile(file)) {
                ZipEntry entry = jarFile.getEntry(ArtifactInfo.FEATURE_XML_LOCATION);
                if (entry != null) {
                    FeatureParser featureParser = new FeatureParser(jarFile.getInputStream(entry));
                    ArtifactInfo<DirectoryFeatureFile> feature = new ArtifactInfo<DirectoryFeatureFile>(
                        featureParser.getId(), featureParser.getVersion(),
                        new DirectoryFeatureFile(file, featureParser));
                    if (add(feature)) {
                        LOG.debug("Add feature {}...", feature);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected EclipseFeatureOption getArtifact(ArtifactInfo<DirectoryFeatureFile> featureInfo)
        throws IOException {
        return new DirectoryEclipseFeatureOption(featureInfo);
    }

}
