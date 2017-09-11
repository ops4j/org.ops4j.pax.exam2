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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
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

    private final List<ProductEclipseBundle> plugins = new ArrayList<>();
    private final List<ProductEclipseFeature> features = new ArrayList<>();
    private final Map<String, PluginConfiguration> configuration = new LinkedHashMap<>();
    private String application;
    private String productID;
    private boolean useFeatures;

    public ProductParser(InputStream stream) throws IOException {
        try {
            Element document = parse(stream);
            application = getAttribute(document, "application", false);
            productID = getAttribute(document, "id", false);
            useFeatures = Boolean.parseBoolean(getAttribute(document, "useFeatures", false));
            // fetch configs
            for (Node node : evaluate(document, "/product/configurations/plugin")) {
                String id = getAttribute(node, "id", true);
                boolean autoStart = Boolean.parseBoolean(getAttribute(node, "autoStart", false));
                int startLevel = Integer.parseInt(getAttribute(node, "startLevel", true));
                configuration.put(id, new PluginConfiguration(autoStart, startLevel));
            }
            // fetch plugins
            for (Node node : evaluate(document, "/product/plugins/plugin")) {
                String id = getAttribute(node, "id", true);
                String version = getAttribute(node, "version", false);
                boolean fragment = Boolean.parseBoolean(getAttribute(node, "fragment", false));
                plugins.add(new ProductEclipseBundle(id, stringToVersion(version), fragment));
            }
            // fetch features
            for (Node node : evaluate(document, "/product/features/feature")) {
                String id = getAttribute(node, "id", true);
                String version = getAttribute(node, "version", false);
                features.add(new ProductEclipseFeature(id, stringToVersion(version)));
            }
        }
        catch (XPathExpressionException e) {
            throw new IOException("parsing product failed!", e);
        }
    }

    public String getApplication() {
        return application;
    }

    public String getProductID() {
        return productID;
    }

    public boolean useFeatures() {
        return useFeatures;
    }

    public List<ProductEclipseBundle> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    public Map<String, PluginConfiguration> getConfiguration() {
        return Collections.unmodifiableMap(configuration);
    }

    public List<ProductEclipseFeature> getFeatures() {
        return Collections.unmodifiableList(features);
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

    public static final class ProductEclipseFeature extends ArtifactInfo<Boolean>
        implements EclipseFeature {

        public ProductEclipseFeature(String symbolicName, Version version) {
            super(symbolicName, version, false);
        }

        @Override
        public boolean isOptional() {
            return getContext();
        }

    }

    public static final class ProductEclipseBundle extends ArtifactInfo<Boolean>
        implements EclipseBundle {

        public ProductEclipseBundle(String symbolicName, Version version, boolean isFragment) {
            super(symbolicName, version, isFragment);
        }

        @Override
        public boolean isFragment() {
            return getContext();
        }

    }

}
