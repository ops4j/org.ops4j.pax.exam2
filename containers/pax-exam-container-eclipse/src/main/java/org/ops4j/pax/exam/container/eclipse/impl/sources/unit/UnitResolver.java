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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment.ModifiableEclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.ResolvedArtifacts;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitProviding;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitRequirement;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision.IncludeMode;
import org.ops4j.pax.exam.container.eclipse.EclipseRepository;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.repository.ResolvedRequirements;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.CacheableSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.directory.DirectoryResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository.P2Resolver;
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
public class UnitResolver extends BundleAndFeatureSource implements CacheableSource {

    private static final String KEY_BUNDLE = "bundle:";
    private static final String KEY_FEATURE = "feature:";
    private static final String KEY_UNIT = "unit:";

    private static final Logger LOG = LoggerFactory.getLogger(UnitResolver.class);

    private final AbstractEclipseBundleSource<?> bundleSource;
    private final AbstractEclipseFeatureSource<?> featureSource;

    public UnitResolver(Collection<? extends EclipseUnitSource> repositories, IncludeMode mode,
        Collection<EclipseInstallableUnit> units, boolean includeFeatures,
        EclipseEnvironment enviroument) throws ArtifactNotFoundException, IOException {
        ContextEclipseBundleSource bundleSource = new ContextEclipseBundleSource();
        ContextEclipseFeatureSource featureSource = new ContextEclipseFeatureSource();
        ContextEclipseUnitSource unitSource = new ContextEclipseUnitSource();
        ModifiableEclipseEnvironment localEnviroument = enviroument.copy();
        if (includeFeatures) {
            localEnviroument.set("org.eclipse.update.install.features", "true");
        }
        ResolvedRequirements resolvedRequirements = new ResolvedRequirements();
        for (EclipseInstallableUnit unit : units) {
            resolveUnit(unit, unitToPath(unit), mode, resolvedRequirements, enviroument,
                bundleSource, featureSource, unitSource, repositories);
        }
        this.featureSource = featureSource;
        this.bundleSource = bundleSource;
    }

    public UnitResolver(DirectoryResolver directoryResolver) {
        this.featureSource = directoryResolver.getFeatureSource();
        this.bundleSource = directoryResolver.getBundleSource();
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

    private static Set<String> resolveUnit(EclipseInstallableUnit unit, String sourcePath,
        IncludeMode mode, ResolvedRequirements resolvedRequirements, EclipseEnvironment enviroument,
        ContextEclipseBundleSource bundleSource, ContextEclipseFeatureSource featureSource,
        ContextEclipseUnitSource unitSource, Collection<? extends EclipseUnitSource> repositories)
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
        addArtifacts(unit, featureSource, bundleSource, enviroument, result);
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
            EclipseInstallableUnit resolvedUnit = resolveRequirement(requires, unit,
                resolvedRequirements, sourcePath, mode, repositories);
            if (resolvedUnit != null) {
                result.addAll(resolveUnit(resolvedUnit,
                    sourcePath + " -> " + requires.getID() + " -> " + unitToPath(resolvedUnit),
                    mode, resolvedRequirements, enviroument, bundleSource, featureSource,
                    unitSource, repositories));
            }
        }
        return result;
    }

    private static EclipseInstallableUnit resolveRequirement(UnitRequirement requires,
        EclipseInstallableUnit unit, ResolvedRequirements resolvedRequirements, String sourcePath,
        IncludeMode mode, Collection<? extends EclipseUnitSource> repositories) throws IOException {
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

    private static ArtifactNotFoundException failedRequirement(UnitRequirement requires,
        String sourcePath) {
        return new ArtifactNotFoundException(
            "can't resolve " + sourcePath + " -> " + requires.getID() + " -> ???");
    }

    private static void addArtifacts(EclipseInstallableUnit unit,
        ContextEclipseFeatureSource featureSource, ContextEclipseBundleSource bundleSource,
        EclipseEnvironment enviroument, Set<String> resolved)
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

    @Override
    public AbstractEclipseBundleSource<?> getBundleSource() {
        return bundleSource;
    }

    @Override
    public AbstractEclipseFeatureSource<?> getFeatureSource() {
        return featureSource;
    }

    private static String encode(String prefix, EclipseVersionedArtifact artifact) {
        return prefix + artifact.getId() + ":" + artifact.getVersion();
    }

    @Override
    public void writeToFolder(Properties metadata, File cacheFolder) throws IOException {
        DirectoryResolver.storeToFolder(cacheFolder, getBundleSource().getIncludedArtifacts(),
            getFeatureSource().getIncludedArtifacts());
    }

    public static UnitResolver restoreFromCache(Properties metadata, File cacheFolder)
        throws IOException {
        DirectoryResolver directoryResolver = new DirectoryResolver(cacheFolder);
        return new UnitResolver(directoryResolver);
    }

}
