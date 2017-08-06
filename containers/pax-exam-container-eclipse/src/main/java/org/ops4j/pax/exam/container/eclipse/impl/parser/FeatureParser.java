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

import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption.EclipseFeatureBundle;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
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

    private final List<PluginInfo> plugins = new ArrayList<>();
    private final List<FeatureInfo> included = new ArrayList<>();

    public FeatureParser(File folder) throws IOException {
        this(new FileInputStream(new File(folder, ArtifactInfo.FEATURE_XML_LOCATION)));
    }

    public FeatureParser(InputStream stream) throws IOException {
        try {
            Element element = parse(stream);
            id = getAttribute(element, "id", true);
            label = getAttribute(element, "label", false);
            version = stringToVersion(getAttribute(element, "version", false));
            for (Node node : evaluate(element, "/feature/plugin")) {
                plugins.add(new PluginInfo(node));
            }
            for (Node node : evaluate(element, "/feature/includes")) {
                String pluginId = getAttribute(node, "id", true);
                String pluginVersion = getAttribute(node, "version", false);
                String pluginOptional = getAttribute(node, "unpack", false);
                included.add(new FeatureInfo(pluginId, stringToVersion(pluginVersion),
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

    public List<PluginInfo> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public List<FeatureInfo> getIncluded() {
        return Collections.unmodifiableList(included);
    }

    public class PluginInfo extends ArtifactInfo<Void> implements EclipseFeatureBundle {

        private final boolean unpack;
        private final boolean fragment;
        private final String os;
        private final String ws;
        private final String arch;

        public PluginInfo(Node node) {
            super(getAttribute(node, "id", true),
                stringToVersion(getAttribute(node, "version", false)), null);
            unpack = Boolean.parseBoolean(getAttribute(node, "unpack", false));
            fragment = Boolean.parseBoolean(getAttribute(node, "fragment", false));
            os = getAttribute(node, "os", false);
            ws = getAttribute(node, "ws", false);
            arch = getAttribute(node, "arch", false);
        }

        @Override
        public boolean isFragment() {
            return fragment;
        }

        @Override
        public boolean isUnpack() {
            return unpack;
        }

        @Override
        public boolean matches(EclipseEnvironment environment) {
            if (!environment.matches(os, "osgi.os", "os.name")) {
                return false;
            }
            if (!environment.matches(ws, "osgi.ws")) {
                return false;
            }
            if (!environment.matches(arch, "osgi.arch", "os.arch")) {
                return false;
            }
            return true;
        }

    }

    public class FeatureInfo extends ArtifactInfo<Boolean> implements EclipseFeature {

        public FeatureInfo(String symbolicName, Version version, boolean context) {
            super(symbolicName, version, context);
        }

        @Override
        public boolean isOptional() {
            return getContext();
        }

    }
}
