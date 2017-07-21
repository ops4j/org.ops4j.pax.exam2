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
package org.ops4j.pax.exam.container.eclipse.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.P2RepositoryArtifactsParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.P2RepositoryContentParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.P2RepositoryContentParser.Artifact;
import org.ops4j.pax.exam.container.eclipse.impl.parser.P2RepositoryContentParser.Provides;
import org.ops4j.pax.exam.container.eclipse.impl.parser.P2RepositoryContentParser.Requires;
import org.ops4j.pax.exam.container.eclipse.impl.parser.P2RepositoryContentParser.Unit;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a Bundlesource that reads bundles, features and units from a Eclipse Repository
 * 
 * @author Christoph Läubrich
 *
 */
public class RepositoryEclipseBundleSource implements EclipseUnitSource {

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryEclipseBundleSource.class);

    public static final String CLASSIFIER_BUNDLE = "osgi.bundle";
    public static final String CLASSIFIER_FEATURE = "org.eclipse.update.feature";

    public static final String NAMESPACE_IU = "org.eclipse.equinox.p2.iu";
    public static final String NAMESPACE_BUNDLE = "osgi.bundle";
    public static final String NAMESPACE_PACKAGE = "java.package";

    private List<RepositoryLocation> locations = new ArrayList<>();

    public RepositoryEclipseBundleSource(URL... urls) throws IOException {
        for (URL url : urls) {
            LOG.info("Connecting to {}...", url);
            locations.add(new RepositoryLocation(url));
        }
    }

    private FeatureParser readFeatureDescriptor(URL url) throws IOException {
        try (JarInputStream stream = new JarInputStream(url.openStream())) {
            JarEntry entry;
            while ((entry = stream.getNextJarEntry()) != null) {
                if (entry.getName().equals(BundleInfo.FEATURE_XML_LOCATION)) {
                    return new FeatureParser((stream));
                }
            }
        }
        throw new IOException(
            "file " + BundleInfo.FEATURE_XML_LOCATION + " not found at URL " + url);
    }

    @Override
    public List<EclipseBundleOption> unit(String id, Version version, IncludeMode mode)
        throws IOException {
        for (RepositoryLocation location : locations) {
            Unit unit = location.contentParser.getUnit(id, version);
            if (unit != null) {
                return getBundleList(mode, location, unit);
            }
        }
        throw new BundleNotFoundException(
            "Unit " + id + ":" + version + " not found in this repository!");
    }

    @Override
    public List<EclipseBundleOption> unit(String id, VersionRange versionRange, IncludeMode mode)
        throws IOException, BundleNotFoundException {
        for (RepositoryLocation location : locations) {
            Unit unit = location.contentParser.getUnit(id, versionRange);
            if (unit != null) {
                return getBundleList(mode, location, unit);
            }
        }
        throw new BundleNotFoundException(
            "Unit " + id + ":" + versionRange + " not found in this repository!");
    }

    private List<EclipseBundleOption> getBundleList(IncludeMode mode, RepositoryLocation location,
        Unit unit) throws BundleNotFoundException, IOException {
        BundleInfoMap<EclipseBundleOption> bundleInfoMap = new BundleInfoMap<>();
        ResolvedRequirements resolvedRequirements = new ResolvedRequirements();
        resolveUnit(new UnitLocation(unit, location), bundleInfoMap, mode, resolvedRequirements);
        Collection<List<BundleInfo<EclipseBundleOption>>> values = bundleInfoMap.asMap().values();
        List<EclipseBundleOption> list = new ArrayList<>();
        for (List<BundleInfo<EclipseBundleOption>> value : values) {
            for (BundleInfo<EclipseBundleOption> info : value) {
                list.add(info.getContext());
            }
        }
        return list;
    }

    private void resolveUnit(UnitLocation unitLocation,
        BundleInfoMap<EclipseBundleOption> bundleInfoMap, IncludeMode mode,
        ResolvedRequirements resolvedRequirements) throws BundleNotFoundException, IOException {
        LOG.info("Resolve unit {}:{}...", unitLocation.unit.id, unitLocation.unit.version);
        List<EclipseFeatureOption> features = new ArrayList<>();
        for (Artifact artifact : unitLocation.unit.artifacts) {
            if (CLASSIFIER_BUNDLE.equals(artifact.classifier)) {
                if (bundleInfoMap.get(artifact.id, artifact.version, true) == null) {
                    EclipseBundleOption fromRepository = getBundleFromRepository(artifact.id,
                        artifact.version, null, unitLocation.location);
                    if (fromRepository == null) {
                        throw new BundleNotFoundException("can't resolve artifact " + artifact.id
                            + ":" + artifact.version + "@" + unitLocation.location.url);
                    }
                    bundleInfoMap
                        .add(new BundleInfo<EclipseBundleOption>(fromRepository, fromRepository));
                }
            }
            else if (CLASSIFIER_FEATURE.equals(artifact.classifier)) {
                EclipseFeatureOption fromRepository = getFeatureFromRepository(artifact.id,
                    artifact.version, null, unitLocation.location);
                if (fromRepository == null) {
                    throw new BundleNotFoundException("can't resolve artifact " + artifact.id + ":"
                        + artifact.version + "@" + unitLocation.location.url);
                }
                features.add(fromRepository);
            }
            else {
                LOG.warn("Ignore artifact {}:{} with unkown classifier: {}",
                    new Object[] { artifact.id, artifact.version, artifact.classifier });
            }
        }
        if (!features.isEmpty()) {
            FeatureEclipseBundleSource feature = new FeatureEclipseBundleSource(this,
                features.toArray(new EclipseFeatureOption[0]));
            List<EclipseBundleOption> includedBundles = feature.getIncludedBundles();
            for (EclipseBundleOption bundle : includedBundles) {
                if (bundleInfoMap.get(bundle, true) == null) {
                    bundleInfoMap.add(new BundleInfo<EclipseBundleOption>(bundle, bundle));
                }
            }
        }
        for (Requires requires : unitLocation.unit.requires) {
            if (resolvedRequirements.isResolved(requires)) {
                LOG.info("Skip requirement {}:{}:{} because it is already resolved...",
                    new Object[] { requires.namespace, requires.name, requires.versionRange });
                continue;
            }
            if (NAMESPACE_IU.equals(requires.namespace)) {
                // Requires a another unit...
                UnitLocation requiredUnit = getRequiredUnit(requires.name, requires.versionRange,
                    unitLocation.location, mode);
                if (requiredUnit != null) {
                    resolvedRequirements.addResolved(requiredUnit.unit.provides);
                    resolveUnit(requiredUnit, bundleInfoMap, mode, resolvedRequirements);
                }
            }
            else if (NAMESPACE_BUNDLE.equals(requires.namespace)) {
                EclipseBundleOption requiredBundle = getRequiredBundle(requires.name,
                    requires.versionRange, unitLocation.location, mode);
                if (requiredBundle != null) {
                    if (bundleInfoMap.get(requiredBundle, true) == null) {
                        bundleInfoMap.add(
                            new BundleInfo<EclipseBundleOption>(requiredBundle, requiredBundle));
                    }
                }
            }
            else if (NAMESPACE_PACKAGE.equals(requires.namespace)) {
                UnitLocation requiredPackageUnit = getRequiredPackage(requires.name,
                    requires.versionRange, unitLocation.location, mode);
                if (requiredPackageUnit != null) {
                    resolvedRequirements.addResolved(requiredPackageUnit.unit.provides);
                    resolveUnit(requiredPackageUnit, bundleInfoMap, mode, resolvedRequirements);
                }
            }
            else if (mode == IncludeMode.SLICER) {
                LOG.info(
                    "Can't resolve requirement {}:{} because of unsupported namespace {} - ignoring due to slicer mode",
                    new Object[] { requires.name, requires.versionRange, requires.namespace });
            }
            else {
                throw new BundleNotFoundException(
                    "Can't resolve requirement " + requires.name + ":" + requires.versionRange
                        + " because of unsupported namespace " + requires.namespace);
            }
        }

    }

    private UnitLocation getRequiredPackage(String name, VersionRange versionRange,
        RepositoryLocation parentLocation, IncludeMode mode) throws BundleNotFoundException {
        Unit unit = getPackageProvidingUnitFromLocation(versionRange, parentLocation);
        if (unit != null) {
            return new UnitLocation(unit, parentLocation);
        }
        else if (mode == IncludeMode.SLICER) {
            LOG.info(
                "Can't resolve osgi-bundle {}:{} directly - ignore because of mode = slicer...",
                name, versionRange);
            return null;
        }
        // now ask all other...
        for (RepositoryLocation location : locations) {
            if (location == parentLocation) {
                continue;
            }
            Unit reproUnit = getPackageProvidingUnitFromLocation(versionRange, location);
            if (reproUnit != null) {
                return new UnitLocation(reproUnit, location);
            }
        }
        throw new BundleNotFoundException(
            "can't resolve package-requirement " + name + ":" + versionRange);
    }

    private static Unit getPackageProvidingUnitFromLocation(VersionRange versionRange,
        RepositoryLocation location) {
        Collection<Unit> units = location.contentParser.getUnits();
        for (Unit unit : units) {
            for (Provides provides : unit.provides) {
                if (NAMESPACE_PACKAGE.equals(provides.namespace)
                    && versionRange.includes(provides.version)) {
                    return unit;
                }
            }
        }
        return null;
    }

    private EclipseBundleOption getRequiredBundle(String name, VersionRange versionRange,
        RepositoryLocation parentLocation, IncludeMode mode) throws BundleNotFoundException {
        EclipseBundleOption bundle = getBundleFromRepository(name, null, versionRange,
            parentLocation);
        if (bundle != null) {
            return bundle;
        }
        else if (mode == IncludeMode.SLICER) {
            LOG.info(
                "Can't resolve osgi-bundle {}:{} directly - ignore because of mode = slicer...",
                name, versionRange);
            return null;
        }
        // now ask all other...
        for (RepositoryLocation location : locations) {
            if (location == parentLocation) {
                continue;
            }
            EclipseBundleOption reproBundle = getBundleFromRepository(name, null, versionRange,
                location);
            if (reproBundle != null) {
                return reproBundle;
            }
        }
        throw new BundleNotFoundException(
            "can't resolve bundle-requirement " + name + ":" + versionRange);
    }

    private UnitLocation getRequiredUnit(String unitName, VersionRange unitVersionRange,
        RepositoryLocation parentLocation, IncludeMode mode) throws BundleNotFoundException {
        // First ask the parent
        Unit parentUnit = parentLocation.contentParser.getUnit(unitName, unitVersionRange);
        if (parentUnit != null) {
            return new UnitLocation(parentUnit, parentLocation);
        }
        else if (mode == IncludeMode.SLICER) {
            LOG.info("Can't resolve unit {}:{} directly - ignore because of mode = slicer...",
                unitName, unitVersionRange);
            return null;
        }
        // now ask all other...
        for (RepositoryLocation location : locations) {
            if (location == parentLocation) {
                continue;
            }
            Unit reproUnit = location.contentParser.getUnit(unitName, unitVersionRange);
            if (reproUnit != null) {
                return new UnitLocation(reproUnit, location);
            }
        }
        throw new BundleNotFoundException(
            "can't resolve unit-requirement " + unitName + ":" + unitVersionRange);
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, Version bundleVersion)
        throws IOException, BundleNotFoundException {
        for (RepositoryLocation location : locations) {
            EclipseBundleOption fromRepository = getBundleFromRepository(bundleName, bundleVersion,
                null, location);
            if (fromRepository != null) {
                return fromRepository;
            }
        }
        throw new BundleNotFoundException(
            "bundle " + bundleName + ":" + bundleVersion + " not found in repository");
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, VersionRange bundleVersionRange)
        throws IOException, BundleNotFoundException {
        for (RepositoryLocation location : locations) {
            EclipseBundleOption fromRepository = getBundleFromRepository(bundleName, null,
                bundleVersionRange, location);
            if (fromRepository != null) {
                return fromRepository;
            }
        }
        throw new BundleNotFoundException(
            "bundle " + bundleName + ":" + bundleVersionRange + " not found in repository");
    }

    private EclipseBundleOption getBundleFromRepository(String bundleName, Version bundleVersion,
        VersionRange bundleVersionRange, RepositoryLocation location) {
        BundleInfo<URL> info;
        if (bundleVersion != null) {
            info = location.artifactsParser.getBundles().get(bundleName, bundleVersion, true);
        }
        else if (bundleVersionRange != null) {
            info = location.artifactsParser.getBundles().get(bundleName, bundleVersionRange);
        }
        else {
            info = null;
        }
        if (info != null) {
            return new RepositoryEclipseBundleOption(info, location);
        }
        else {
            return null;
        }
    }

    @Override
    public EclipseFeatureOption feature(String featureName, Version featureVersion)
        throws IOException, BundleNotFoundException {
        for (RepositoryLocation location : locations) {
            EclipseFeatureOption fromRepository = getFeatureFromRepository(featureName,
                featureVersion, null, location);
            if (fromRepository != null) {
                return fromRepository;
            }
        }
        throw new BundleNotFoundException(
            "feature " + featureName + ":" + featureVersion + " not found in repository");
    }

    @Override
    public EclipseFeatureOption feature(String featureName, VersionRange featureVersionRange)
        throws IOException, BundleNotFoundException {
        for (RepositoryLocation location : locations) {
            EclipseFeatureOption fromRepository = getFeatureFromRepository(featureName, null,
                featureVersionRange, location);
            if (fromRepository != null) {
                return fromRepository;
            }
        }
        throw new BundleNotFoundException(
            "feature " + featureName + ":" + featureVersionRange + " not found in repository");
    }

    private EclipseFeatureOption getFeatureFromRepository(String featureName,
        Version featureVersion, VersionRange featureVersionRange, RepositoryLocation location)
        throws IOException {
        String key = featureName + ":" + featureVersion + ":" + featureVersionRange;
        EclipseFeatureOption cached = location.featureCache.get(key);
        if (cached != null) {
            return cached;
        }
        BundleInfo<URL> info;
        if (featureVersion != null) {
            info = location.artifactsParser.getFeatures().get(featureName, featureVersion, true);
        }
        else if (featureVersionRange != null) {
            info = location.artifactsParser.getFeatures().get(featureName, featureVersionRange);
        }
        else {
            info = null;
        }
        if (info != null) {
            URL url = info.getContext();
            final FeatureParser featureParser = readFeatureDescriptor(url);
            EclipseFeatureOption option = new RepositoryEclipseFeatureOption(info, featureParser,
                location);
            location.featureCache.put(key, option);
            return option;
        }
        else {
            return null;
        }
    }

    private final class RepositoryEclipseBundleOption extends AbstractEclipseBundleOption<URL> {

        private RepositoryLocation location;

        private RepositoryEclipseBundleOption(BundleInfo<URL> bundleInfo,
            RepositoryLocation location) {
            super(bundleInfo);
            this.location = location;
        }

        @Override
        protected Option toOption(BundleInfo<URL> bundleInfo) {
            return CoreOptions.bundle(bundleInfo.getContext().toExternalForm());
        }

        @Override
        public String toString() {
            return super.toString() + "@" + location.url;
        }
    }

    private final class RepositoryEclipseFeatureOption extends AbstractEclipseFeatureOption<URL> {

        private final FeatureParser featureParser;
        private RepositoryLocation location;

        private RepositoryEclipseFeatureOption(BundleInfo<URL> bundleInfo,
            FeatureParser featureParser, RepositoryLocation location) {
            super(bundleInfo);
            this.featureParser = featureParser;
            this.location = location;
        }

        @Override
        protected List<? extends EclipseFeature> getIncluded(BundleInfo<URL> bundleInfo) {
            return featureParser.getIncluded();
        }

        @Override
        protected List<? extends EclipseBundle> getBundles(BundleInfo<URL> bundleInfo) {
            return featureParser.getPlugins();
        }

        @Override
        protected boolean isOptional(BundleInfo<URL> bundleInfo) {
            return false;
        }

        @Override
        protected Option toOption(BundleInfo<URL> bundleInfo) {
            return CoreOptions.bundle(bundleInfo.getContext().toExternalForm());
        }

        @Override
        public String toString() {
            return super.toString() + "@" + location.url;
        }
    }

    private static final class ResolvedRequirements {

        private Map<String, BundleInfoMap<Provides>> resolvedMap = new HashMap<>();

        public boolean isResolved(Requires requires) {
            BundleInfoMap<Provides> resolved = resolvedMap.get(requires.namespace);
            return resolved != null && resolved.get(requires.name, requires.versionRange) != null;
        }

        public void addResolved(List<Provides> providedRequirements) {
            for (Provides provided : providedRequirements) {
                BundleInfoMap<Provides> resolved = resolvedMap.get(provided.namespace);
                if (resolved == null) {
                    resolved = new BundleInfoMap<>();
                    resolvedMap.put(provided.namespace, resolved);
                }
                if (resolved.get(provided.name, provided.version, true) == null) {
                    resolved.add(new BundleInfo<P2RepositoryContentParser.Provides>(provided.name,
                        provided.version, provided));
                }
            }
        }

    }

    private static final class UnitLocation {

        private final Unit unit;
        private final RepositoryLocation location;

        public UnitLocation(Unit unit, RepositoryLocation location) {
            this.unit = unit;
            this.location = location;
        }

    }

    private static final class RepositoryLocation {

        private final URL url;
        private final P2RepositoryArtifactsParser artifactsParser;
        private final P2RepositoryContentParser contentParser;
        /**
         * Since features information requires artifact retrieval we will cache this to prevent
         * Net-I/O on successive calls
         */
        private final Map<String, EclipseFeatureOption> featureCache = new HashMap<>();

        public RepositoryLocation(URL url) throws IOException {
            this.url = url;
            artifactsParser = new P2RepositoryArtifactsParser(url);
            contentParser = new P2RepositoryContentParser(url);
        }

    }

}
