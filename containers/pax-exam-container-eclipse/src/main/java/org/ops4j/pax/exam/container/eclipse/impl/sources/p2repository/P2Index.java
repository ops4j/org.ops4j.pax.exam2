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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.xpath.XPathExpressionException;

import org.ops4j.pax.exam.container.eclipse.impl.parser.AbstractParser;
import org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository.P2Cache.MetaDataProperties;
import org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository.P2Cache.P2CacheStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handles the concept of a "P2 Index", that can consit of a p2.index file and a default search
 * order
 * 
 * @author Christoph Läubrich
 *
 */
public final class P2Index {

    private static final String ARTIFACTS_FACTORY_DEFAULT = "compositeArtifacts.xml,artifacts.xml";
    private static final String METADATA_FACTORY_DEFAULT = "compositeContent.xml,content.xml";
    private static final String PROPERTY_ARTIFACT_FACTORY = "artifact.repository.factory.order";
    private static final String PROPERTY_METADATA_FACTORY = "metadata.repository.factory.order";
    private static final String COMPOSITE_METADATA_REPOSITORY = "org.eclipse.equinox.internal.p2.metadata.repository.CompositeMetadataRepository";
    private static final String COMPOSITE_ARTIFACT_REPOSITORY = "org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository";
    private static final String LOCAL_METADATA_REPOSITORY = "org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository";
    private static final String SIMPLE_ARTIFACT_REPOSITORY = "org.eclipse.equinox.p2.artifact.repository.simpleRepository";

    private static final Logger LOG = LoggerFactory.getLogger(P2Index.class);

    private final List<String> metadataNames;
    private final List<String> artifactNames;
    private final URL url;
    private P2RepositoryFile artifactRepository;
    private P2RepositoryFile metadataRepository;
    private final ConcurrentHashMap<String, P2Index> cache;

    private final boolean isdefault;
    private final Properties indexProperties;
    private final URL indexUrl;
    private final MetaDataProperties metadata;

    public P2Index(URL url) throws IOException {
        this(url, new ConcurrentHashMap<>());
        cache.putIfAbsent(url.toExternalForm(), this);
    }

    private P2Index(URL url, ConcurrentHashMap<String, P2Index> cache) throws IOException {
        this.url = url;
        this.cache = cache;
        metadata = P2Cache.getMetaDataProperties(url);
        indexProperties = new Properties();
        indexUrl = appendSegment(url, "p2.index");
        InputStream stream = P2Cache.tryOpen(indexUrl);
        isdefault = stream == null;
        if (stream != null) {
            try {
                indexProperties.load(stream);
            }
            finally {
                stream.close();
            }
        }
        metadataNames = Collections.unmodifiableList(getXMLNames(
            indexProperties.getProperty(PROPERTY_METADATA_FACTORY, METADATA_FACTORY_DEFAULT)));
        artifactNames = Collections.unmodifiableList(getXMLNames(
            indexProperties.getProperty(PROPERTY_ARTIFACT_FACTORY, ARTIFACTS_FACTORY_DEFAULT)));
    }

    public URL getURL() {
        return url;
    }

    public P2RepositoryFile getMetadataRepository() throws MalformedURLException, IOException {
        if (metadataRepository == null) {
            metadataRepository = fromMetaCache("meta");
            if (metadataRepository == null) {
                ReproOpenResult file = Parser.readReproFile(url, metadataNames);
                if (isdefault) {
                    indexProperties.put(PROPERTY_METADATA_FACTORY,
                        file.name + "," + METADATA_FACTORY_DEFAULT);
                    writeProperties();
                }
                metadataRepository = new P2RepositoryFileImpl(this, file.root, file.url,
                    file.lastModified);
                String type = metadataRepository.getType();
                storeMeta("meta", file, type);
            }
        }
        return metadataRepository;
    }

    private P2RepositoryFile fromMetaCache(String ident) throws MalformedURLException {
        String name = metadata.getProperty("repro." + ident + ".file.name");
        if (name != null) {
            URL url = new URL(metadata.getProperty("repro." + ident + ".file.url"));
            String type = metadata.getProperty("repro." + ident + ".file.type");
            long lastMod = Long
                .parseLong(metadata.getProperty("repro." + ident + ".file.modified"));
            return new P2RepositoryFileImpl(this, type, name, url, lastMod);
        }
        else {
            return null;
        }
    }

    private void storeMeta(String ident, ReproOpenResult file, String type) {
        metadata.setProperty("repro." + ident + ".file.name", file.name);
        metadata.setProperty("repro." + ident + ".file.url", file.url.toExternalForm());
        metadata.setProperty("repro." + ident + ".file.type", type);
        metadata.setProperty("repro." + ident + ".file.modified",
            String.valueOf(file.lastModified));
        metadata.store();
    }

    private void writeProperties() {
        try {
            File file = P2Cache.getCacheFile(indexUrl);
            try (FileOutputStream stream = new FileOutputStream(file)) {
                indexProperties.store(stream, "generated by pax exam");
                file.setLastModified(0);
            }
        }
        catch (Exception e) {
            // ignore then...
            LOG.trace("writing index file to cache failed", e);
        }

    }

    public P2RepositoryFile getArtifactRepository() throws MalformedURLException, IOException {
        if (artifactRepository == null) {
            artifactRepository = fromMetaCache("artifact");
            if (artifactRepository == null) {
                ReproOpenResult file = Parser.readReproFile(url, artifactNames);
                artifactRepository = new P2RepositoryFileImpl(this, file.root, file.url,
                    file.lastModified);
                if (isdefault) {
                    indexProperties.put(PROPERTY_ARTIFACT_FACTORY,
                        file.name + "," + ARTIFACTS_FACTORY_DEFAULT);
                    writeProperties();
                }
                String type = artifactRepository.getType();
                storeMeta("artifact", file, type);
            }
        }
        return artifactRepository;
    }

    public static class P2RepositoryFileImpl implements P2RepositoryFile {

        private Element root;
        private final P2Index index;
        private final String type;
        private final URL reproURL;
        private String name;
        private long lastModified;

        public P2RepositoryFileImpl(P2Index index, String type, String name, URL reproURL,
            long lastModified) {
            this.index = index;
            this.type = type;
            this.name = name;
            this.reproURL = reproURL;
            this.lastModified = lastModified;
        }

        public P2RepositoryFileImpl(P2Index index, Element root, URL reproURL, long lastModified) {
            this.index = index;
            this.root = root;
            this.reproURL = reproURL;
            this.lastModified = lastModified;
            this.type = root == null ? "-null-" : Parser.getType(root);
        }

        @Override
        public URL getURL() {
            return reproURL;
        }

        @Override
        public P2Index getIndex() {
            return index;
        }

        @Override
        public boolean isComposite() {
            return COMPOSITE_ARTIFACT_REPOSITORY.equals(type)
                || COMPOSITE_METADATA_REPOSITORY.equals(type);
        }

        @Override
        public boolean isRepository() {
            return SIMPLE_ARTIFACT_REPOSITORY.equals(type)
                || LOCAL_METADATA_REPOSITORY.equals(type);
        }

        @Override
        public boolean isArtifactRepository() {
            return SIMPLE_ARTIFACT_REPOSITORY.equals(type)
                || COMPOSITE_ARTIFACT_REPOSITORY.equals(type);
        }

        @Override
        public boolean isMetadataRepository() {
            return LOCAL_METADATA_REPOSITORY.equals(type)
                || COMPOSITE_METADATA_REPOSITORY.equals(type);
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public long getLastModified() {
            return lastModified;
        }

        private synchronized Element getRoot() throws IOException {
            if (root == null) {
                ReproOpenResult file = Parser.readReproFile(reproURL,
                    Collections.singletonList(name));
                if (file == null) {
                    throw new IllegalStateException("invalid root, try to clear cache!");
                }
                root = file.root;
                lastModified = file.lastModified;
            }
            return root;
        }

        @Override
        public Element getRespository() throws IOException {
            if (!isRepository()) {
                if (isComposite()) {
                    throw new IllegalStateException(
                        "this is a composite repository, use getChilds to get Children");
                }
                else {
                    throw new IllegalStateException("not a valid repository");
                }
            }
            return getRoot();
        }

        @Override
        public List<P2RepositoryFile> getChilds() throws IOException {
            if (!isComposite()) {
                throw new IllegalStateException("can only be called on composite repositories!");
            }
            try {
                List<String> childs = Parser.parseChilds(getRoot());
                List<P2RepositoryFile> result = new ArrayList<>();
                for (String child : childs) {
                    URL childUrl = appendSegment(index.url, child);
                    String key = childUrl.toExternalForm();
                    P2Index childIndex = index.cache.get(key);
                    if (childIndex == null) {
                        childIndex = new P2Index(childUrl, index.cache);
                        P2Index put = index.cache.putIfAbsent(key, childIndex);
                        if (put != null) {
                            childIndex = put;
                        }
                    }
                    if (isArtifactRepository()) {
                        result.add(childIndex.getArtifactRepository());
                    }
                    else if (isMetadataRepository()) {
                        result.add(childIndex.getMetadataRepository());
                    }
                }
                return result;
            }
            catch (XPathExpressionException e) {
                throw new IOException(e);
            }
        }

    }

    private static List<String> getXMLNames(String property) {
        List<String> result = new ArrayList<>();
        for (String value : property.split(",")) {
            value = value.trim();
            if (value.equals("!")) {
                continue;
            }
            if (value.toLowerCase().endsWith(".xml")) {
                String baseName = value.substring(0, value.length() - 4);
                result.add(baseName + ".jar");
            }
            result.add(value);
        }
        return result;
    }

    private static URL appendSegment(URL url, String segment) throws MalformedURLException {
        String[] externalForm = url.toExternalForm().split("\\?", 2);
        if (!externalForm[0].endsWith("/")) {
            externalForm[0] = externalForm[0] + "/";
        }
        if (externalForm.length == 2) {
            segment = segment + "?" + externalForm[1];
        }
        return new URL(externalForm[0] + segment);
    }

    private static class Parser extends AbstractParser {

        private static List<String> parseChilds(Element root) throws XPathExpressionException {
            List<String> locations = new ArrayList<>();
            for (Node node : evaluate(root, "/repository/children/child")) {
                locations.add(getAttribute(node, "location", true));
            }
            return locations;
        }

        private static String getType(Element element) {
            return getAttribute(element, "type", true);
        }

        private static ReproOpenResult readReproFile(URL url, List<String> names)
            throws MalformedURLException, IOException {
            for (String name : names) {
                URL openUrl = appendSegment(url, name);
                P2CacheStream open = P2Cache.tryOpen(openUrl);
                if (open != null) {
                    if (name.endsWith(".xml")) {
                        return new ReproOpenResult(parse(open), openUrl, name,
                            open.getLastModified());
                    }
                    else if (name.endsWith(".jar")) {
                        try {
                            try (JarInputStream stream = new JarInputStream(open)) {
                                JarEntry entry;
                                while ((entry = stream.getNextJarEntry()) != null) {
                                    if (entry.getName()
                                        .equals(name.substring(0, name.length() - 3) + "xml")) {
                                        return new ReproOpenResult(parse(stream), openUrl, name,
                                            open.getLastModified());
                                    }
                                }
                            }
                        }
                        finally {
                            open.close();
                        }
                    }
                    else {
                        // TODO XZ-Format maybe content type guessing by reading first bytes??
                    }
                }
            }
            return null;
        }

    }

    private static final class ReproOpenResult {

        private final Element root;
        private final URL url;
        private final String name;
        private final long lastModified;

        public ReproOpenResult(Element parse, URL openUrl, String name, long lastModified) {
            this.root = parse;
            this.url = openUrl;
            this.name = name;
            this.lastModified = lastModified;
        }

    }

}
