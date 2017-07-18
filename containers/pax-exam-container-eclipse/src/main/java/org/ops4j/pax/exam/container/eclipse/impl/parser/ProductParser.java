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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.ops4j.pax.exam.container.eclipse.impl.BundleInfo;
import org.osgi.framework.Version;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parses Eclipse product files
 * 
 * @author Christoph Läubrich
 *
 */
public class ProductParser extends AbstractParser {

    private List<BundleInfo<PluginConfiguration>> plugins = new ArrayList<>();

    public ProductParser(InputStream stream) throws IOException {
        try {
            Element document = parse(stream);
            // fetch configs
            Map<String, PluginConfiguration> configs = new HashMap<>();
            for (Node node : evaluate(document, "/product/configurations/plugin")) {
                String id = getAttribute(node, "id", true);
                boolean autoStart = Boolean.parseBoolean(getAttribute(node, "autoStart", false));
                int startLevel = Integer.parseInt(getAttribute(node, "startLevel", true));
                configs.put(id, new PluginConfiguration(autoStart, startLevel));
            }
            // fetch plugins
            for (Node node : evaluate(document, "/product/plugins/plugin")) {
                String id = getAttribute(node, "id", true);
                String version = getAttribute(node, "version", false);
                Version bundleVersion;
                if (version == null || version.isEmpty()) {
                    bundleVersion = Version.emptyVersion;
                }
                else {
                    bundleVersion = Version.parseVersion(version);
                }
                plugins
                    .add(new BundleInfo<PluginConfiguration>(id, bundleVersion, configs.get(id)));
            }
        }
        catch (XPathExpressionException e) {
            throw new IOException("parsing product failed!", e);
        }
    }

    public List<BundleInfo<PluginConfiguration>> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public static final class PluginConfiguration {

        public final boolean autoStart;
        public final int startLevel;

        public PluginConfiguration(boolean autoStart, int startLevel) {
            this.autoStart = autoStart;
            this.startLevel = startLevel;
        }

        @Override
        public String toString() {
            return "startlevel=" + startLevel + ":autostart=" + autoStart;
        }
    }

}
