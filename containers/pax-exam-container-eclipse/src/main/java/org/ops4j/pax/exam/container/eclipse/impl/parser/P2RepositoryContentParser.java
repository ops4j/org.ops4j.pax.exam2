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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.xpath.XPathExpressionException;

import org.ops4j.pax.exam.container.eclipse.impl.BundleInfo;
import org.ops4j.pax.exam.container.eclipse.impl.BundleInfoMap;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * parses the content file from a repro
 * 
 * @author Christoph Läubrich
 *
 */
public class P2RepositoryContentParser extends AbstractParser {

    private static final String CONTENT_FILE = "content";

    private BundleInfoMap<Unit> unitMap = new BundleInfoMap<>();
    private List<Unit> units = new ArrayList<>();

    public P2RepositoryContentParser(URL url) throws IOException {
        Element content = readXML(new URL(url, CONTENT_FILE + ".jar"), CONTENT_FILE);
        try {
            for (Node node : evaluate(content, "/repository/units/unit")) {
                String unitID = getAttribute(node, "id", true);
                Version version = stringToVersion(getAttribute(node, "version", false));
                Unit unit = createUnit(node, unitID, version);
                unitMap.add(new BundleInfo<P2RepositoryContentParser.Unit>(unitID, version, unit));
                units.add(unit);
            }
        }
        catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    public Collection<Unit> getUnits() {
        return Collections.unmodifiableCollection(units);
    }

    public Unit getUnit(String id, Version version) {
        BundleInfo<Unit> info = unitMap.get(id, version, true);
        if (info == null) {
            return null;
        }
        return info.getContext();
    }

    public Unit getUnit(String id, VersionRange versionRange) {
        BundleInfo<Unit> info = unitMap.get(id, versionRange);
        if (info == null) {
            return null;
        }
        return info.getContext();
    }

    private static Unit createUnit(Node node, String unitID, Version unitVersion)
        throws XPathExpressionException {
        Map<String, String> properties = readProperties(evaluate(node, "./properties/property"));
        List<Provides> provides = readProvides(evaluate(node, "./provides/provided"));
        List<Requires> requires = readRequires(evaluate(node, "./requires/required"));
        List<Artifact> artifacts = readArtifacts(evaluate(node, "./artifacts/artifact"));
        return new Unit(unitID, unitVersion, properties, provides, requires, artifacts);
    }

    private static List<Artifact> readArtifacts(Iterable<Node> evaluate) {
        List<Artifact> map = new ArrayList<>();
        for (Node node : evaluate) {
            map.add(new Artifact(getAttribute(node, "id", true),
                stringToVersion(getAttribute(node, "version", false)),
                getAttribute(node, "classifier", true)));
        }
        return map;
    }

    private static List<Requires> readRequires(Iterable<Node> evaluate) {
        ArrayList<Requires> list = new ArrayList<>();
        for (Node node : evaluate) {
            VersionRange range;
            String rangeAttr = getAttribute(node, "range", false);
            if (rangeAttr != null & !rangeAttr.isEmpty()) {
                range = VersionRange.valueOf(rangeAttr);
            }
            else {
                range = null;
            }
            list.add(new Requires(getAttribute(node, "namespace", true),
                getAttribute(node, "name", true), range));
        }
        return list;
    }

    private static List<Provides> readProvides(Iterable<Node> evaluate) {
        ArrayList<Provides> list = new ArrayList<>();
        for (Node node : evaluate) {
            list.add(new Provides(getAttribute(node, "namespace", true),
                getAttribute(node, "name", true),
                stringToVersion(getAttribute(node, "version", false))));
        }
        return list;
    }

    private static Map<String, String> readProperties(Iterable<Node> evaluate) {
        HashMap<String, String> map = new HashMap<>();
        for (Node node : evaluate) {
            map.put(getAttribute(node, "name", true), getAttribute(node, "value", true));
        }
        return map;
    }

    private static Element readXML(URL url, String xmlFile) throws IOException {
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

    public static final class Unit {

        public final String id;
        public final Version version;
        public final Map<String, String> properties;
        public final List<Provides> provides;
        public final List<Requires> requires;
        public final List<Artifact> artifacts;

        public Unit(String id, Version version, Map<String, String> properties,
            List<Provides> provides, List<Requires> requires, List<Artifact> artifacts) {
            this.id = id;
            this.version = version;
            this.properties = Collections.unmodifiableMap(properties);
            this.provides = Collections.unmodifiableList(provides);
            this.requires = Collections.unmodifiableList(requires);
            this.artifacts = Collections.unmodifiableList(artifacts);
        }

        @Override
        public String toString() {
            return "Unit:" + id + ":" + version;
        }

    }

    public static final class Artifact {

        public final String id;
        public final Version version;
        public final String classifier;

        public Artifact(String id, Version version, String classifier) {
            this.id = id;
            this.version = version;
            this.classifier = classifier;
        }

        @Override
        public String toString() {
            return "Artifact:" + id + ":" + version + ":" + classifier;
        }

    }

    public static final class Provides {

        public final String namespace;
        public final String name;
        public final Version version;

        public Provides(String namespace, String name, Version version) {
            this.namespace = namespace;
            this.name = name;
            this.version = version;
        }

        @Override
        public String toString() {
            return "Provides:" + namespace + ":" + name + ":" + version;
        }

    }

    public static final class Requires {

        public final String namespace;
        public final String name;
        public final VersionRange versionRange;

        public Requires(String namespace, String name, VersionRange versionRange) {
            this.namespace = namespace;
            this.name = name;
            this.versionRange = versionRange;
        }

        @Override
        public String toString() {
            return "Requires:" + namespace + ":" + name + ":" + versionRange;
        }

    }
}
