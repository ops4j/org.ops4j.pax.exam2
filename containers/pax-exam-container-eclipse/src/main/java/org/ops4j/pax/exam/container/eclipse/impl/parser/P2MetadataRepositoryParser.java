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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;

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
public class P2MetadataRepositoryParser extends AbstractParser {

    private final ArtifactInfoMap<Unit> unitMap;
    private final List<Unit> units;

    public P2MetadataRepositoryParser(Element content)
        throws IOException, XPathException, InvalidSyntaxException {
        Node unitsNode = getNode(content, "/repository/units");
        int cap = getSize(unitsNode, 10);
        unitMap = new ArtifactInfoMap<>(Math.max(10, cap / 3));
        units = new ArrayList<>(cap);
        XPath x = getXPath();
        XPathExpression provideExpression = x.compile("./provides/provided");
        XPathExpression requireExpression = x.compile("./requires/required");
        XPathExpression artifactExpression = x.compile("./artifacts/artifact");
        XPathExpression unitExpression = x.compile("./unit");
        for (Node node : evaluate(unitsNode, unitExpression, true)) {
            String unitID = getAttribute(node, "id", true);
            Version unitVersion = stringToVersion(getAttribute(node, "version", false));
            List<Provides> provides = readProvides(evaluate(node, provideExpression));
            List<Requires> requires = readRequires(evaluate(node, requireExpression));
            List<Artifact> artifacts = readArtifacts(evaluate(node, artifactExpression));
            Unit unit = new Unit(unitID, unitVersion, Collections.emptyMap(), provides, requires,
                artifacts);
            unitMap.add(new ArtifactInfo<Unit>(unitID, unitVersion, unit));
            units.add(unit);
        }
    }

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

    private List<Artifact> readArtifacts(Iterable<Node> evaluate) {
        List<Artifact> map = new ArrayList<>();
        for (Node node : evaluate) {
            map.add(new Artifact(getAttribute(node, "id", true),
                stringToVersion(getAttribute(node, "version", false)),
                getAttribute(node, "classifier", true)));
        }
        return map;
    }

    private List<Requires> readRequires(Iterable<Node> evaluate) {
        ArrayList<Requires> list = new ArrayList<>();
        for (Node node : evaluate) {
            VersionRange range = stringToVersionRange(getAttribute(node, "range", false));
            String namespace = getAttribute(node, "namespace", false);
            String match = getAttribute(node, "match", false);
            String name = getAttribute(node, "name", false);
            String matchParameters = getAttribute(node, "matchParameters", false);
            Boolean optional = Boolean.parseBoolean(getAttribute(node, "optional", false));
            Boolean greedy = Boolean.parseBoolean(getAttribute(node, "greedy", false));
            list.add(
                new Requires(namespace, name, range, match, matchParameters, optional, greedy));
        }
        return list;
    }

    private List<Provides> readProvides(Iterable<Node> evaluate) {
        ArrayList<Provides> list = new ArrayList<>();
        for (Node node : evaluate) {
            list.add(new Provides(getAttribute(node, "namespace", true),
                getAttribute(node, "name", true),
                stringToVersion(getAttribute(node, "version", false))));
        }
        return list;
    }

}
