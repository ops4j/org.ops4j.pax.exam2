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
package org.ops4j.pax.exam.container.eclipse.impl.parser;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.osgi.internal.framework.FilterImpl;
import org.ops4j.pax.exam.container.eclipse.impl.BundleInfo;
import org.ops4j.pax.exam.container.eclipse.impl.BundleInfoMap;
import org.ops4j.pax.exam.container.eclipse.impl.RepositoryEclipseBundleSource;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parses the artifacts file from a repro
 * 
 * @author Christoph Läubrich
 *
 */
public class P2RepositoryArtifactsParser extends AbstractParser {

    private static final Logger LOG = LoggerFactory.getLogger(P2RepositoryArtifactsParser.class);
    private static final String ARTIFACTS_FILE = "artifacts";

    private final BundleInfoMap<URL> bundleMap = new BundleInfoMap<>();

    private final BundleInfoMap<URL> featureMap = new BundleInfoMap<>();

    public P2RepositoryArtifactsParser(URL url) throws IOException {
        LOG.info("Connecting to URL {}", url);
        Element artifacts = readXML(new URL(url, ARTIFACTS_FILE + ".jar"), ARTIFACTS_FILE);
        try {
            Map<Filter, String> outputMap = new LinkedHashMap<>();
            for (Node node : evaluate(artifacts, "/repository/mappings/rule")) {
                String filterString = getAttribute(node, "filter", true);
                String output = getAttribute(node, "output", true);
                FilterImpl filter = FilterImpl.newInstance(filterString, false);
                LOG.debug("put filter {} for output mapping {}...", filter, output);
                outputMap.put(filter, output);
            }
            Iterable<Node> evaluate = evaluate(artifacts, "/repository/artifacts/artifact");
            for (Node node : evaluate) {
                String classifier = getAttribute(node, "classifier", true);
                BundleInfoMap<URL> map;
                if (classifier.equals(RepositoryEclipseBundleSource.CLASSIFIER_BUNDLE)) {
                    map = bundleMap;
                }
                else if (classifier.equals(RepositoryEclipseBundleSource.CLASSIFIER_FEATURE)) {
                    map = featureMap;
                }
                else {
                    LOG.info("Skip artifact with classifier {}...", classifier);
                    continue;
                }
                Map<String, String> attributes = attributesToMap(node);
                String output = null;
                for (Entry<Filter, String> entry : outputMap.entrySet()) {
                    if (entry.getKey().matches(attributes)) {
                        output = entry.getValue();
                        break;
                    }
                }
                if (output == null) {
                    throw new IOException("No output mapping found for attribute set " + attributes
                        + " and filters " + outputMap);
                }
                String externalForm = url.toExternalForm();
                if (externalForm.endsWith("/")) {
                    externalForm = externalForm.substring(0, externalForm.length() - 1);
                }
                attributes.put("repoUrl", externalForm);
                URL artifactURL = new URL(replaceAttributes(output, attributes));
                String id = getAttribute(node, "id", true);
                Version version = stringToVersion(getAttribute(node, "version", false));
                map.add(new BundleInfo<URL>(id, version, artifactURL));
            }
        }
        catch (XPathExpressionException e) {
            throw new IOException(e);
        }
        catch (InvalidSyntaxException e) {
            throw new IOException("can't read filter string", e);
        }
    }

    private String replaceAttributes(String output, Map<String, String> attributes) {
        for (Entry<String, String> entry : attributes.entrySet()) {
            output = output.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return output;
    }

    private Element readXML(URL url, String xmlFile) throws IOException {
        String searchFile = xmlFile + ".xml";
        try (JarInputStream stream = new JarInputStream(url.openStream())) {
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                if (entry.getName().equals(searchFile)) {
                    return parse(stream);
                }
            }
        }
        throw new IOException("file " + searchFile + " not found at URL " + url);
    }

    public BundleInfoMap<URL> getBundles() {
        return bundleMap;
    }

    public BundleInfoMap<URL> getFeatures() {
        return featureMap;
    }
}
