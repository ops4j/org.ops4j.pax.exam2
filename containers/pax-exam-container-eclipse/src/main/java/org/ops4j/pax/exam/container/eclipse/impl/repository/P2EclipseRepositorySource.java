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
package org.ops4j.pax.exam.container.eclipse.impl.repository;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact.EclipseClassifiedVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.AbstractEclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfoMap;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.P2RepositoryArtifactsParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.P2RepositoryContentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads the information of a single repository and provides this as a source, see
 * {@link RepositoryResolverEclipseBundleSource} for a way of using this to resolve dependent
 * information
 * 
 * @author Christoph Läubrich
 *
 */
public class P2EclipseRepositorySource extends AbstractEclipseUnitSource<URL, URL, Unit>
    implements EclipseUnitSource {

    private static final Logger LOG = LoggerFactory.getLogger(P2EclipseRepositorySource.class);

    private final URL url;
    private final String name;

    private final P2RepositoryArtifactsParser artifactsParser = new P2RepositoryArtifactsParser();

    private final P2RepositoryContentParser contentParser = new P2RepositoryContentParser();
    /**
     * Since features information requires artifact retrieval we will cache this to prevent Net-I/O
     * on successive calls
     */
    private final Map<String, RepositoryEclipseFeatureOption> featureCache = new HashMap<>();

    public P2EclipseRepositorySource(URL url, String name) throws IOException {
        this.url = url;
        this.name = name;
        LOG.info("Connecting to repository at {}...", url);
        LOG.info("Reading Artifacts-Repository...");
        if (!artifactsParser.openRepositoryFile(url)) {
            LOG.warn("Can't open Artifacts-Repository, bundle/feature information will be empty!");
        }
        LOG.info("Reading Content-Repository...");
        if (!contentParser.openRepositoryFile(url)) {
            LOG.warn("Can't open Content-Repository, unit information will be empty!");
        }
        LOG.info("Done! Artifacts = {}, Units = {}", artifactsParser.getCount(),
            contentParser.getCount());
    }

    private FeatureParser readFeatureDescriptor(URL url) throws IOException {
        try (JarInputStream stream = new JarInputStream(url.openStream())) {
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                if (entry.getName().equals(ArtifactInfo.FEATURE_XML_LOCATION)) {
                    return new FeatureParser((stream));
                }
            }
        }
        throw new IOException(
            "file " + ArtifactInfo.FEATURE_XML_LOCATION + " not found at URL " + url);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + "@" + url;
    }

    @Override
    protected EclipseFeatureOption getFeature(ArtifactInfo<URL> featureInfo) throws IOException {
        String key = featureInfo.getId() + ":" + featureInfo.getVersion();
        RepositoryEclipseFeatureOption cached = featureCache.get(key);
        if (cached != null) {
            return cached;
        }
        URL url = featureInfo.getContext();
        final FeatureParser featureParser = readFeatureDescriptor(url);
        RepositoryEclipseFeatureOption option = new RepositoryEclipseFeatureOption(featureInfo,
            featureParser, this);
        featureCache.put(key, option);
        return option;
    }

    @Override
    protected EclipseBundleOption getBundle(ArtifactInfo<URL> bundleInfo) throws IOException {
        return new RepositoryEclipseBundleOption(bundleInfo, this);
    }

    @Override
    protected EclipseInstallableUnit getUnit(ArtifactInfo<Unit> unitInfo) throws IOException {
        return new RepositoryEclipseInstallableUnit(unitInfo, this);
    }

    @Override
    protected ArtifactInfoMap<URL> getBundles() {
        return artifactsParser.getArtifacts(EclipseClassifiedVersionedArtifact.CLASSIFIER_BUNDLE);
    }

    @Override
    protected ArtifactInfoMap<URL> getFeatures() {
        return artifactsParser.getArtifacts(EclipseClassifiedVersionedArtifact.CLASSIFIER_FEATURE);
    }

    @Override
    protected ArtifactInfoMap<Unit> getUnits() {
        return contentParser.getUnitMap();
    }

}
