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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.osgi.internal.framework.FilterImpl;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfoMap;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
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
public class P2ArtifactRepositoryParser extends AbstractParser {

    private static final Logger LOG = LoggerFactory.getLogger(P2ArtifactRepositoryParser.class);

    private final Map<String, ArtifactInfoMap<URL>> artifactMap = new HashMap<>();

    public P2ArtifactRepositoryParser(URL baseUrl, Element artifacts)
        throws IOException, XPathExpressionException, InvalidSyntaxException {
        Map<Filter, String> outputMap = new LinkedHashMap<>();
        for (Node node : evaluate(artifacts, "/repository/mappings/rule")) {
            String filterString = getAttribute(node, "filter", true);
            String output = getAttribute(node, "output", true);
            FilterImpl filter = FilterImpl.newInstance(filterString, false);
            LOG.debug("put filter {} for output mapping {}...", filter, output);
            outputMap.put(filter, output);
        }
        for (Node node : evaluate(artifacts, "/repository/artifacts/artifact")) {
            String classifier = getAttribute(node, "classifier", true);
            ArtifactInfoMap<URL> map = getArtifacts(classifier);
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
            String externalForm = baseUrl.toExternalForm();
            if (externalForm.endsWith("/")) {
                externalForm = externalForm.substring(0, externalForm.length() - 1);
            }
            attributes.put("repoUrl", externalForm);
            URL artifactURL = new URL(replaceAttributes(output, attributes));
            String id = getAttribute(node, "id", true);
            Version version = stringToVersion(getAttribute(node, "version", false));
            map.add(new ArtifactInfo<URL>(id, version, artifactURL));
        }
    }

    private String replaceAttributes(String output, Map<String, String> attributes) {
        for (Entry<String, String> entry : attributes.entrySet()) {
            output = output.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return output;
    }

    public int getCount() {
        int artifacts = 0;
        for (ArtifactInfoMap<URL> map : artifactMap.values()) {
            artifacts += map.size();
        }
        return artifacts;
    }

    public ArtifactInfo<URL> getArtifact(String classifier, String id, VersionRange versionRange) {
        return getArtifacts(classifier).get(id, versionRange);
    }

    public synchronized ArtifactInfoMap<URL> getArtifacts(String classifier) {
        ArtifactInfoMap<URL> artifactInfoMap = artifactMap.get(classifier);
        if (artifactInfoMap == null) {
            artifactInfoMap = new ArtifactInfoMap<>();
            artifactMap.put(classifier, artifactInfoMap);
        }
        return artifactInfoMap;
    }

}
