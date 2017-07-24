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

import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpressionException;

import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfoMap;
import org.ops4j.pax.exam.container.eclipse.impl.repository.Artifact;
import org.ops4j.pax.exam.container.eclipse.impl.repository.Provides;
import org.ops4j.pax.exam.container.eclipse.impl.repository.Requires;
import org.ops4j.pax.exam.container.eclipse.impl.repository.Unit;
import org.osgi.framework.InvalidSyntaxException;
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
public class P2RepositoryContentParser extends RepositoryXMLParser {

    private static final String CONTENT_FILE = "content";

    private final ArtifactInfoMap<Unit> unitMap = new ArtifactInfoMap<>();
    private final List<Unit> units = new ArrayList<>();

    public Collection<Unit> getUnits() {
        return Collections.unmodifiableCollection(units);
    }

    public Unit getUnit(String id, Version version) {
        ArtifactInfo<Unit> info = unitMap.get(id, version);
        if (info == null) {
            return null;
        }
        return info.getContext();
    }

    public int getCount() {
        return units.size();
    }

    public ArtifactInfoMap<Unit> getUnitMap() {
        return unitMap;
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

    @Override
    protected void readXML(URL baseUrl, Element content)
        throws IOException, XPathException, InvalidSyntaxException {
        for (Node node : evaluate(content, "/repository/units/unit")) {
            String unitID = getAttribute(node, "id", true);
            Version version = stringToVersion(getAttribute(node, "version", false));
            Unit unit = createUnit(node, unitID, version);
            unitMap.add(new ArtifactInfo<Unit>(unitID, version, unit));
            units.add(unit);
        }
    }

    @Override
    protected String getXMLName() {
        return CONTENT_FILE;
    }

}
