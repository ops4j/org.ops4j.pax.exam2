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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.impl.BundleInfo;
import org.osgi.framework.Version;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * parses eclipse feature files
 * 
 * @author Christoph Läubrich
 *
 */
public class FeatureParser extends AbstractParser {

    private String id;
    private String label;
    private Version version;

    private final List<BundleInfo<Boolean>> plugins = new ArrayList<>();
    private final List<FeatureBundleInfo> included = new ArrayList<>();

    public FeatureParser(File folder) throws IOException {
        this(new FileInputStream(new File(folder, BundleInfo.FEATURE_XML_LOCATION)));
    }

    public FeatureParser(InputStream stream) throws IOException {
        try {
            Element element = parse(stream);
            id = getAttribute(element, "id", true);
            label = getAttribute(element, "label", false);
            version = stringToVersion(getAttribute(element, "version", false));
            for (Node node : evaluate(element, "/feature/plugin")) {
                String pluginId = getAttribute(node, "id", true);
                String pluginVersion = getAttribute(node, "version", false);
                String pluginUnpack = getAttribute(node, "unpack", false);
                plugins.add(new BundleInfo<Boolean>(pluginId, stringToVersion(pluginVersion),
                    Boolean.parseBoolean(pluginUnpack)));
            }
            for (Node node : evaluate(element, "/feature/includes")) {
                String pluginId = getAttribute(node, "id", true);
                String pluginVersion = getAttribute(node, "version", false);
                String pluginOptional = getAttribute(node, "unpack", false);
                included.add(new FeatureBundleInfo(pluginId, stringToVersion(pluginVersion),
                    Boolean.parseBoolean(pluginOptional)));
            }

        }
        catch (XPathExpressionException e) {
            throw new IOException(e);
        }
        finally {
            stream.close();
        }
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Version getVersion() {
        return version;
    }

    public List<BundleInfo<Boolean>> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public List<FeatureBundleInfo> getIncluded() {
        return Collections.unmodifiableList(included);
    }

    public class FeatureBundleInfo extends BundleInfo<Boolean> implements EclipseFeature {

        public FeatureBundleInfo(String symbolicName, Version version, boolean context) {
            super(symbolicName, version, context);
        }

        @Override
        public boolean isOptional() {
            return getContext();
        }

    }
}
