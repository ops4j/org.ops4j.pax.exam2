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

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Parses an Eclipse target file
 * 
 * @author Christoph Läubrich
 *
 */
public class TargetPlatformParser extends AbstractParser {

    private List<TargetPlatformLocation> locations = new ArrayList<>();

    public TargetPlatformParser(InputStream stream) throws IOException {
        Element document = parse(stream);
        try {
            Iterable<Node> evaluate = evaluate(document, "/target/locations/location");
            for (Node node : evaluate) {
                LocationType type = LocationType.valueOf(getAttribute(node, "type", true));
                if (type == LocationType.Directory) {
                    locations
                        .add(new DirectoryTargetPlatformLocation(getAttribute(node, "path", true)));
                }
                else if (type == LocationType.Feature) {
                    locations.add(new FeatureTargetPlatformLocation(
                        getAttribute(document, "path", true), getAttribute(node, "id", true)));
                }
                else if (type == LocationType.Profile) {
                    locations
                        .add(new ProfileTargetPlatformLocation(getAttribute(node, "path", true)));
                }
                else if (type == LocationType.InstallableUnit) {
                    String repository = (String) getXPath().evaluate("/repository[location]", node,
                        XPathConstants.STRING);
                    List<String> units = new ArrayList<>();
                    for (Node unit : evaluate(node, "/unit")) {
                        String str = getAttribute(unit, "id", true);
                        String v = getAttribute(unit, "version", false);
                        if (v != null && !v.isEmpty()) {
                            str = str + ":" + v;
                        }
                        units.add(str);
                    }
                    Map<String, String> flags = new HashMap<>();
                    NamedNodeMap attributes = node.getAttributes();
                    int length = attributes.getLength();
                    for (int i = 0; i < length; i++) {
                        Node item = attributes.item(i);
                        flags.put(item.getNodeName(), item.getNodeValue());
                    }
                    locations
                        .add(new InstallableUnitTargetPlatformLocation(repository, units, flags));
                }
            }
        }
        catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    public List<TargetPlatformLocation> getLocations() {
        return Collections.unmodifiableList(locations);
    }

    public enum LocationType {
        /**
         * Installation like SDK
         */
        Profile,
        /**
         * Plain directory
         */
        Directory,
        /**
         * a feature from a location with the given id
         */
        Feature,
        /**
         * P2 repro like
         */
        InstallableUnit;
    }

    public static abstract class TargetPlatformLocation {

        public final LocationType type;

        private TargetPlatformLocation(LocationType type) {
            this.type = type;
        }
    }

    public abstract static class PathTargetPlatformLocation extends TargetPlatformLocation {

        public final String path;

        private PathTargetPlatformLocation(String path, LocationType type) {
            super(type);
            this.path = path;
        }
    }

    public static class DirectoryTargetPlatformLocation extends PathTargetPlatformLocation {

        public final String path;

        private DirectoryTargetPlatformLocation(String path) {
            super(path, LocationType.Directory);
            this.path = path;
        }

    }

    public static class ProfileTargetPlatformLocation extends PathTargetPlatformLocation {

        public final String path;

        private ProfileTargetPlatformLocation(String path) {
            super(path, LocationType.Directory);
            this.path = path;
        }

    }

    public static class FeatureTargetPlatformLocation extends PathTargetPlatformLocation {

        public final String id;

        private FeatureTargetPlatformLocation(String path, String id) {
            super(path, LocationType.Feature);
            this.id = id;
        }

    }

    public static class InstallableUnitTargetPlatformLocation extends TargetPlatformLocation {

        public final List<String> units;

        public final String repository;

        public final Map<String, String> flags;

        private InstallableUnitTargetPlatformLocation(String repository, List<String> units,
            Map<String, String> flags) {
            super(LocationType.InstallableUnit);
            this.repository = repository;
            this.flags = Collections.unmodifiableMap(flags);
            this.units = Collections.unmodifiableList(units);
        }

    }
}
