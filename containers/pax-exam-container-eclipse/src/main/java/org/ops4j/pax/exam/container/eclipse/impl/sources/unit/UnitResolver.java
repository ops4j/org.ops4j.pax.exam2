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
package org.ops4j.pax.exam.container.eclipse.impl.sources.unit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.ResolvedArtifacts;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitProviding;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitRequirement;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision.IncludeMode;
import org.ops4j.pax.exam.container.eclipse.EclipseRepository;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment.ModifiableEclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.parser.AbstractParser;
import org.ops4j.pax.exam.container.eclipse.impl.repository.ResolvedRequirements;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureAndUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository.P2Cache;
import org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository.P2Cache.MetaDataProperties;
import org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository.P2Resolver;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The resolver takes a list of repros, a mode and a list of units and resolve the content of those
 * into a new Eclipse Source. Depending on the mode requirements of the given units are transitivly
 * resolved
 * 
 * @author Christoph Läubrich
 *
 */
public class UnitResolver extends BundleAndFeatureAndUnitSource {

    private static final String CACHE_KEY_LASTMODIFIED = "UnitResolver.lastmodified";
    private static final String CACHE_KEY_UNIT = "UnitResolver.unit:";

    private static final String KEY_BUNDLE = "bundle:";
    private static final String KEY_FEATURE = "feature:";
    private static final String KEY_UNIT = "unit:";

    private static final Logger LOG = LoggerFactory.getLogger(UnitResolver.class);

    private final EclipseUnitSource[] repositories;
    private final IncludeMode mode;
    private final ResolvedRequirements resolvedRequirements = new ResolvedRequirements();
    private final ContextEclipseBundleSource bundleSource = new ContextEclipseBundleSource();
    private final ContextEclipseFeatureSource featureSource = new ContextEclipseFeatureSource();
    private final ContextEclipseUnitSource unitSource = new ContextEclipseUnitSource();
    private final ModifiableEclipseEnvironment enviroument;

    public UnitResolver(Collection<? extends EclipseUnitSource> repositories, IncludeMode mode,
        Collection<EclipseInstallableUnit> units, boolean includeFeatures,
        EclipseEnvironment enviroument) throws ArtifactNotFoundException, IOException {
        this.mode = mode;
        this.repositories = repositories.toArray(new EclipseUnitSource[0]);
        this.enviroument = enviroument.copy();
        if (includeFeatures) {
            this.enviroument.set("org.eclipse.update.install.features", "true");
        }
        MetaDataProperties cache = null;
        P2Resolver p2Resolver = null;
        if (mode == IncludeMode.SLICER && repositories.size() == 1) {
            EclipseUnitSource next = repositories.iterator().next();
            if (next instanceof P2Resolver) {
                p2Resolver = (P2Resolver) next;
                // we can do caching!
                URL url = p2Resolver.getUrl();
                cache = P2Cache.getMetaDataProperties(url);
            }
        }
        if (cache != null
            && cache.isModified(CACHE_KEY_LASTMODIFIED, p2Resolver.getLastModified())) {
            cache.clearObjects(CACHE_KEY_UNIT);
        }
        for (EclipseInstallableUnit unit : units) {
            String key = encode(CACHE_KEY_UNIT, unit);
            if (cache != null) {
                @SuppressWarnings("unchecked")
                Set<String> items = cache.getObjectProperty(key, Set.class);
                if (items != null) {
                    LOG.info("Use cached resolving result for unit {}...", unit);
                    for (String item : items) {
                        if (item.startsWith(KEY_BUNDLE)) {
                            ArtifactInfo<Void> decode = decode(item.substring(KEY_BUNDLE.length()));
                            bundleSource
                                .addBundle(p2Resolver.bundle(decode.getId(), decode.getVersion()));
                        }
                        else if (item.startsWith(KEY_FEATURE)) {
                            ArtifactInfo<Void> decode = decode(
                                item.substring(KEY_FEATURE.length()));
                            featureSource.addFeature(
                                p2Resolver.feature(decode.getId(), decode.getVersion()));
                        }
                        else if (item.startsWith(KEY_UNIT)) {
                            ArtifactInfo<Void> decode = decode(item.substring(KEY_UNIT.length()));
                            unitSource
                                .addUnit(p2Resolver.unit(decode.getId(), decode.getVersion()));
                        }
                    }
                    continue;
                }
            }
            Set<String> resolvedItems = resolveUnit(unit, unitToPath(unit));
            if (cache != null) {
                cache.setObjectProperty(key, resolvedItems);
            }
        }
        if (cache != null) {
            cache.setProperty(CACHE_KEY_LASTMODIFIED, p2Resolver.getLastModified());
            cache.store();
        }
    }

    private ArtifactInfo<Void> decode(String key) throws MalformedURLException {
        int index = key.lastIndexOf(':');
        String symbolicName = key.substring(0, index);
        Version version = AbstractParser.stringToVersion(key.substring(index + 1));
        return new ArtifactInfo<Void>(symbolicName, version, null);
    }

    private static String unitToPath(EclipseInstallableUnit unit) {
        EclipseUnitSource source = unit.getSource();
        if (source instanceof P2Resolver) {
            P2Resolver p2Resolver = (P2Resolver) source;
            return unit.getId() + "[" + p2Resolver.getName() + "@" + p2Resolver.getUrl() + "]";
        }
        else if (source instanceof EclipseRepository) {
            return unit.getId() + "[" + ((EclipseRepository) source).getName() + "]";
        }
        return unit.getId();
    }

    private Set<String> resolveUnit(EclipseInstallableUnit unit, String sourcePath)
        throws ArtifactNotFoundException, IOException {
        if (unitSource.containsUnit(unit) || resolvedRequirements.containsUnit(unit)) {
            LOG.debug("Skip resolving of {}, already resolved or resolving in progress...", unit);
            return Collections.emptySet();
        }
        resolvedRequirements.addUnit(unit);
        unitSource.addUnit(unit);
        LOG.info("Resolve {}...", unit);
        Set<String> result = new LinkedHashSet<>();
        result.add(encode(KEY_UNIT, unit));
        addArtifacts(unit, result);
        for (UnitRequirement requires : unit.getRequirements()) {
            if (!requires.matches(enviroument)) {
                LOG.info("Skip {} because it does not match enviroument...", requires);
                continue;
            }
            if (resolvedRequirements.isResolved(requires)) {
                LOG.debug("Skip {} because it is already resolved...", requires);
                continue;
            }
            if (resolvedRequirements.isFailed(requires)) {
                LOG.debug("Skip {} because it has already failed...", requires);
                continue;
            }
            LOG.debug("Try to resolve {}...", requires);
            EclipseInstallableUnit resolvedUnit = resolveRequirement(requires, unit, sourcePath);
            if (resolvedUnit != null) {
                result.addAll(resolveUnit(resolvedUnit,
                    sourcePath + " -> " + requires.getID() + " -> " + unitToPath(resolvedUnit)));
            }
        }
        return result;
    }

    private EclipseInstallableUnit resolveRequirement(UnitRequirement requires,
        EclipseInstallableUnit unit, String sourcePath) throws IOException {
        EclipseUnitSource primarySource = unit.getSource();
        try {
            Collection<EclipseInstallableUnit> allUnits = primarySource.getAllUnits();
            for (EclipseInstallableUnit reproUnit : allUnits) {
                for (UnitProviding providing : reproUnit.getProvided()) {
                    if (requires.matches(providing)) {
                        return reproUnit;
                    }
                }
            }
            throw failedRequirement(requires, sourcePath);
        }
        catch (ArtifactNotFoundException anfe) {
            if (mode == IncludeMode.SLICER) {
                LOG.debug("Failed to resolve {} ({}), ignore because of slicer mode...", requires,
                    anfe.getMessage());
                resolvedRequirements.addFailed(requires);
                return null;
            }
            else if (requires.isOptional() && !requires.isGreedy()) {
                LOG.debug("Failed to resolve optional {} ({}) ignore because of non greedy...",
                    requires, anfe.toString());
                return null;
            }
        }
        for (EclipseUnitSource unitSource : repositories) {
            if (unitSource.equals(primarySource)) {
                continue;
            }
            try {
                Collection<EclipseInstallableUnit> allUnits = unitSource.getAllUnits();
                for (EclipseInstallableUnit reproUnit : allUnits) {
                    Collection<UnitProviding> provided = reproUnit.getProvided();
                    for (UnitProviding providing : provided) {
                        if (requires.matches(providing)) {
                            return reproUnit;
                        }
                    }
                }
            }
            catch (ArtifactNotFoundException anfe) {
            }
        }
        if (requires.isOptional()) {
            LOG.debug("Ignore {} because it is optional and can't be resolved...", requires);
            return null;
        }
        throw failedRequirement(requires, sourcePath);

    }

    private ArtifactNotFoundException failedRequirement(UnitRequirement requires,
        String sourcePath) {
        return new ArtifactNotFoundException(
            "can't resolve " + sourcePath + " -> " + requires.getID() + " -> ???");
    }

    private void addArtifacts(EclipseInstallableUnit unit, Set<String> resolved)
        throws IOException, ArtifactNotFoundException {
        ResolvedArtifacts artifacts = unit.resolveArtifacts(enviroument);
        for (EclipseBundleOption bundle : artifacts.getBundles()) {
            if (bundleSource.addBundle(bundle)) {
                resolved.add(encode(KEY_BUNDLE, bundle));
                LOG.debug("Resolve bundle {}:{}...", bundle.getId(), bundle.getVersion());
            }
        }
        for (EclipseFeatureOption feature : artifacts.getFeatures()) {
            if (featureSource.addFeature(feature)) {
                resolved.add(encode(KEY_FEATURE, feature));
                LOG.debug("Resolve feature {}:{}...", feature.getId(), feature.getVersion());
            }
        }
    }

    public static String encode(String key, EclipseVersionedArtifact artifact) {
        return key + artifact.getId() + ":" + artifact.getVersion();
    }

    @Override
    public ContextEclipseUnitSource getUnitSource() {
        return unitSource;
    }

    @Override
    public ContextEclipseBundleSource getBundleSource() {
        return bundleSource;
    }

    @Override
    public ContextEclipseFeatureSource getFeatureSource() {
        return featureSource;
    }

}
