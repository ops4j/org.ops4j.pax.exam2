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
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.ops4j.pax.exam.container.eclipse.EclipseProvision.IncludeMode;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.osgi.framework.Version;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parses an Eclipse target file
 * 
 * @author Christoph Läubrich
 *
 */
public class TargetPlatformParser extends AbstractParser {

    private final List<TargetPlatformLocation> locations = new ArrayList<>();

    public TargetPlatformParser(InputStream stream) throws IOException {
        Element document = parse(stream);
        try {
            for (Node node : evaluate(document, "/target/locations/location")) {
                LocationType type = LocationType.valueOf(getAttribute(node, "type", true));
                if (type == LocationType.Directory) {
                    locations
                        .add(new DirectoryTargetPlatformLocation(getAttribute(node, "path", true)));
                }
                else if (type == LocationType.Feature) {
                    String version = getAttribute(node, "version", false);
                    locations
                        .add(new FeatureTargetPlatformLocation(getAttribute(document, "path", true),
                            getAttribute(node, "id", true), stringToVersion(version)));
                }
                else if (type == LocationType.Profile) {
                    locations
                        .add(new ProfileTargetPlatformLocation(getAttribute(node, "path", true)));
                }
                else if (type == LocationType.InstallableUnit) {
                    String repository = (String) getXPath().evaluate("./repository/@location", node,
                        XPathConstants.STRING);
                    List<ArtifactInfo<?>> units = new ArrayList<>();
                    for (Node unit : evaluate(node, "./unit")) {
                        String id = getAttribute(unit, "id", true);
                        Version version = stringToVersion(getAttribute(unit, "version", false));
                        units.add(new ArtifactInfo<Void>(id, version, null));
                    }
                    IncludeMode mode;
                    try {
                        mode = IncludeMode.valueOf(getAttribute(node, "includeMode", false));
                    }
                    catch (RuntimeException e) {
                        // default to slicer mode...
                        mode = IncludeMode.SLICER;
                    }
                    locations.add(new InstallableUnitTargetPlatformLocation(repository, units, mode,
                        attributesToMap(node)));
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
        public final Version version;

        private FeatureTargetPlatformLocation(String path, String id, Version version) {
            super(path, LocationType.Feature);
            this.id = id;
            this.version = version;
        }

    }

    public static class InstallableUnitTargetPlatformLocation extends TargetPlatformLocation {

        public final List<ArtifactInfo<?>> units;

        public final String repository;

        public final Map<String, String> flags;

        public final IncludeMode mode;

        private InstallableUnitTargetPlatformLocation(String repository,
            List<ArtifactInfo<?>> units, IncludeMode mode, Map<String, String> flags) {
            super(LocationType.InstallableUnit);
            this.repository = repository;
            this.mode = mode;
            this.flags = Collections.unmodifiableMap(flags);
            this.units = Collections.unmodifiableList(units);
        }

        @Override
        public String toString() {
            return "InstallableUnitTargetPlatformLocation:" + repository + ":" + mode + ":" + units;
        }

    }
}
