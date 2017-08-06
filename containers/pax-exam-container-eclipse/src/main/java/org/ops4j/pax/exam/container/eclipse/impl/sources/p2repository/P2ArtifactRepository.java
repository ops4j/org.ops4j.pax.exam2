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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.parser.AbstractParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.P2ArtifactRepositoryParser;
import org.ops4j.pax.exam.container.eclipse.impl.repository.EclipseClassifiedVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository.P2Cache.MetaDataProperties;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the ArtifactRepository of P2
 * 
 * @author Christoph Läubrich
 *
 */
public class P2ArtifactRepository extends BundleAndFeatureSource {

    private static final String CACHE_KEY_BUNDLE = "bundle:";
    private static final String CACHE_KEY_FEATURE = "feature:";

    private static final String CACHE_KEY_LASTMODIFIED = "P2ArtifactRepository.lastmodified";

    private static final Logger LOG = LoggerFactory.getLogger(P2ArtifactRepository.class);

    private final P2BundleSource bundleSource = new P2BundleSource();
    private final P2FeatureSource featureSource = new P2FeatureSource();

    private final String name;

    private final long lastModified;

    public P2ArtifactRepository(String name, P2RepositoryFile root) throws IOException {
        this.name = name;
        try {
            lastModified = addFile(root);
        }
        catch (XPathExpressionException | InvalidSyntaxException e) {
            throw new IOException(e);
        }
    }

    private long addFile(P2RepositoryFile file)
        throws XPathExpressionException, IOException, InvalidSyntaxException {
        if (file.isComposite()) {
            long lastmod = -1;
            List<P2RepositoryFile> childs = file.getChilds();
            for (P2RepositoryFile child : childs) {
                lastmod = Math.max(lastmod, addFile(child));
            }
            return lastmod;
        }
        else if (file.isArtifactRepository()) {
            List<ArtifactInfo<URL>> bundles;
            List<ArtifactInfo<URL>> features;
            String reproName = name + file.getURL();
            MetaDataProperties cache = P2Cache.getMetaDataProperties(file.getURL());
            if (!cache.isModified(CACHE_KEY_LASTMODIFIED, file.getLastModified())) {
                LOG.info("Use cached data for {}@{}...", file.getType(), file.getURL());
                bundles = new ArrayList<>();
                features = new ArrayList<>();
                Set<String> names = cache.stringPropertyNames();
                for (String name : names) {
                    if (name.startsWith(CACHE_KEY_BUNDLE)) {
                        bundles.add(decode(name.substring(CACHE_KEY_BUNDLE.length()),
                            cache.getProperty(name)));
                    }
                    else if (name.startsWith(CACHE_KEY_FEATURE)) {
                        features.add(decode(name.substring(CACHE_KEY_FEATURE.length()),
                            cache.getProperty(name)));
                    }
                }
            }
            else {
                cache.clear(CACHE_KEY_FEATURE);
                cache.clear(CACHE_KEY_BUNDLE);
                LOG.info("Parse {}@{}...", file.getType(), file.getURL());
                P2ArtifactRepositoryParser parser = new P2ArtifactRepositoryParser(
                    file.getIndex().getURL(), file.getRespository());
                LOG.info("... {} artifacts parsed.", parser.getCount());
                bundles = parser.getArtifacts(EclipseClassifiedVersionedArtifact.CLASSIFIER_BUNDLE)
                    .getArtifacts();
                features = parser
                    .getArtifacts(EclipseClassifiedVersionedArtifact.CLASSIFIER_FEATURE)
                    .getArtifacts();
                for (ArtifactInfo<URL> bundle : bundles) {
                    cache.setProperty(CACHE_KEY_BUNDLE + bundle.getId() + ":" + bundle.getVersion(),
                        bundle.getContext().toExternalForm());
                }
                for (ArtifactInfo<URL> feature : features) {
                    cache.setProperty(
                        CACHE_KEY_FEATURE + feature.getId() + ":" + feature.getVersion(),
                        feature.getContext().toExternalForm());
                }
                cache.setProperty(CACHE_KEY_LASTMODIFIED, file.getLastModified());
                cache.store();
            }
            bundleSource.addBundles(reproName, bundles);
            featureSource.addFeatures(reproName, features);
            return file.getLastModified();
        }
        else {
            LOG.info("Ignore P2RepositoryFile of type {}@{}...", file.getType(), file.getURL());
            return -1;
        }

    }

    private ArtifactInfo<URL> decode(String key, String value) throws MalformedURLException {
        URL url = new URL(value);
        int index = key.lastIndexOf(':');
        String symbolicName = key.substring(0, index);
        Version version = AbstractParser.stringToVersion(key.substring(index + 1));
        return new ArtifactInfo<URL>(symbolicName, version, url);
    }

    @Override
    protected EclipseBundleSource getBundleSource() {
        return bundleSource;
    }

    @Override
    protected EclipseFeatureSource getFeatureSource() {
        return featureSource;
    }

    public long getLastModified() {
        return lastModified;
    }

}
