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
package org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfoMap;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseFeatureSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Source of features of a P2 repro
 * 
 * @author Christoph Läubrich
 *
 */
public class P2FeatureSource extends AbstractEclipseFeatureSource<P2Feature> {

    private static final Logger LOG = LoggerFactory.getLogger(P2FeatureSource.class);

    /**
     * Since features information requires artifact retrieval we will cache this to prevent Net-I/O
     * on successive calls
     */
    private final Map<String, P2EclipseFeatureOption> featureCache = new HashMap<>();

    @Override
    protected EclipseFeatureOption getArtifact(ArtifactInfo<P2Feature> featureInfo)
        throws IOException {
        String key = featureInfo.getId() + ":" + featureInfo.getVersion();
        P2EclipseFeatureOption cached = featureCache.get(key);
        if (cached != null) {
            return cached;
        }
        URL url = featureInfo.getContext().getUrl();
        final FeatureParser featureParser = readFeatureDescriptor(url);
        P2EclipseFeatureOption option = new P2EclipseFeatureOption(featureInfo, featureParser);
        featureCache.put(key, option);
        return option;
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

    public void addFeatures(String reproName, ArtifactInfoMap<URL> artifacts) {
        for (ArtifactInfo<URL> info : artifacts.getArtifacts()) {
            if (add(
                new ArtifactInfo<P2Feature>(info, new P2Feature(info.getContext(), reproName)))) {
                LOG.info("Add feature {}:{}:{}",
                    new Object[] { info.getId(), info.getVersion(), reproName });
            }
        }
    }

}
