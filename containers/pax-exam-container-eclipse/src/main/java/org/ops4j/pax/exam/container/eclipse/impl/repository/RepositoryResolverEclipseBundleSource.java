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
package org.ops4j.pax.exam.container.eclipse.impl.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitProviding;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitRequirement;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision.IncludeMode;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact.EclipseClassifiedVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.AbstractEclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.FeatureEclipseBundleSource;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
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
public class RepositoryResolverEclipseBundleSource extends
    AbstractEclipseUnitSource<EclipseBundleOption, EclipseFeatureOption, EclipseInstallableUnit> {

    private static final Logger LOG = LoggerFactory
        .getLogger(RepositoryResolverEclipseBundleSource.class);

    private final EclipseUnitSource[] repositories;
    private final IncludeMode mode;
    private final ResolvedRequirements resolvedRequirements = new ResolvedRequirements();

    public RepositoryResolverEclipseBundleSource(
        Collection<? extends EclipseUnitSource> repositories, IncludeMode mode,
        Collection<EclipseInstallableUnit> units) throws ArtifactNotFoundException, IOException {
        this.mode = mode;
        this.repositories = repositories.toArray(new EclipseUnitSource[0]);
        for (EclipseInstallableUnit unit : units) {
            // TODO provide a "path" so error can give exact information from where an requirement
            // can't be reolved
            resolveUnit(unit);
        }
    }

    private void resolveUnit(EclipseInstallableUnit unit)
        throws ArtifactNotFoundException, IOException {
        if (getUnits().get(unit) != null) {
            LOG.debug("Skip resolving of {}, already resolved or resolving in progress...", unit);
            return;
        }
        getUnits().add(new ArtifactInfo<EclipseInstallableUnit>(unit, unit));
        LOG.info("Resolve {}...", unit);
        resolvedRequirements.addResolved(unit.getProvided());
        resolveArtifactFeatures(unit, addArtifacts(unit));
        for (UnitRequirement requires : unit.getRequirements()) {
            if (resolvedRequirements.isResolved(requires)) {
                LOG.debug("Skip requirement {} because it is already resolved...", requires);
                continue;
            }
            if (resolvedRequirements.isFailed(requires)) {
                LOG.debug("Skip requirement {} because it has already failed...", requires);
                continue;
            }
            if (EclipseInstallableUnit.NAMESPACE_IU.equals(requires.getNamespace())) {
                resolveRequiredUnit(requires, unit);
            }
            else if (EclipseInstallableUnit.NAMESPACE_BUNDLE.equals(requires.getNamespace())) {
                resolveRequiredBundle(requires, unit);
            }
            else {
                resolveGenericRequirement(requires, unit);
            }
        }
    }

    private void resolveGenericRequirement(UnitRequirement requires, EclipseInstallableUnit unit)
        throws IOException {
        EclipseUnitSource primarySource = unit.getSource();
        try {
            Collection<EclipseInstallableUnit> allUnits = primarySource.getAllUnits();
            for (EclipseInstallableUnit reproUnit : allUnits) {
                List<UnitProviding> provided = reproUnit.getProvided();
                for (UnitProviding providing : provided) {
                    if (fullfills(providing, requires)) {
                        resolveUnit(reproUnit);
                        return;
                    }
                }
            }
        }
        catch (ArtifactNotFoundException anfe) {
            if (mode == IncludeMode.SLICER) {
                resolvedRequirements.addFailed(requires);
                return;
            }
        }
        for (EclipseUnitSource unitSource : repositories) {
            if (unitSource.equals(primarySource)) {
                continue;
            }
            try {
                Collection<EclipseInstallableUnit> allUnits = unitSource.getAllUnits();
                for (EclipseInstallableUnit reproUnit : allUnits) {
                    List<UnitProviding> provided = reproUnit.getProvided();
                    for (UnitProviding providing : provided) {
                        if (fullfills(providing, requires)) {
                            resolveUnit(reproUnit);
                            return;
                        }
                    }
                }
            }
            catch (ArtifactNotFoundException anfe) {
            }
        }
        throw failedRequirement(requires);

    }

    private void resolveRequiredBundle(UnitRequirement requires, EclipseInstallableUnit unit)
        throws IOException {
        if (getBundles().get(requires.getName(), requires.getVersionRange()) != null) {
            return;
        }
        EclipseUnitSource primarySource = unit.getSource();
        try {
            EclipseBundleOption bundle = primarySource.bundle(requires.getName(),
                requires.getVersionRange());
            if (getBundles().get(bundle) == null) {
                getBundles().add(new ArtifactInfo<EclipseBundleOption>(bundle, bundle));
            }
            return;
        }
        catch (ArtifactNotFoundException anfe) {
            if (mode == IncludeMode.SLICER) {
                resolvedRequirements.addFailed(requires);
                return;
            }
        }
        for (EclipseUnitSource unitSource : repositories) {
            if (unitSource.equals(primarySource)) {
                continue;
            }
            try {
                EclipseBundleOption bundle = unitSource.bundle(requires.getName(),
                    requires.getVersionRange());
                if (getBundles().get(bundle) == null) {
                    getBundles().add(new ArtifactInfo<EclipseBundleOption>(bundle, bundle));
                }
                return;
            }
            catch (ArtifactNotFoundException anfe) {
            }
        }
        throw failedRequirement(requires);
    }

    private void resolveRequiredUnit(UnitRequirement requires, EclipseInstallableUnit unit)
        throws IOException {
        EclipseUnitSource primarySource = unit.getSource();
        try {
            resolveUnit(primarySource.unit(requires.getName(), requires.getVersionRange()));
            return;
        }
        catch (ArtifactNotFoundException anfe) {
            if (mode == IncludeMode.SLICER) {
                resolvedRequirements.addFailed(requires);
                return;
            }
        }
        for (EclipseUnitSource unitSource : repositories) {
            if (unitSource.equals(primarySource)) {
                continue;
            }
            try {
                resolveUnit(unitSource.unit(requires.getName(), requires.getVersionRange()));
                return;
            }
            catch (ArtifactNotFoundException anfe) {
            }
        }
        throw failedRequirement(requires);
    }

    private ArtifactNotFoundException failedRequirement(UnitRequirement requires) {
        return new ArtifactNotFoundException("can't resolve " + requires.getNamespace()
            + "-requirement " + requires.getName() + ":" + requires.getVersionRange());
    }

    private void resolveArtifactFeatures(EclipseInstallableUnit unit,
        List<EclipseFeatureOption> unitFeatures) throws ArtifactNotFoundException, IOException {
        if (!unitFeatures.isEmpty()) {
            FeatureEclipseBundleSource featureResolver = new FeatureEclipseBundleSource(
                unit.getSource(), unitFeatures);
            for (EclipseBundleOption bundle : featureResolver.getIncludedBundles()) {
                if (getBundles().get(bundle) == null) {
                    getBundles().add(new ArtifactInfo<EclipseBundleOption>(bundle, bundle));
                }
            }
            for (EclipseFeatureOption feature : featureResolver.getIncludedFeatures()) {
                if (getFeatures().get(feature) == null) {
                    getFeatures().add(new ArtifactInfo<EclipseFeatureOption>(feature, feature));
                }
            }
        }
    }

    private List<EclipseFeatureOption> addArtifacts(EclipseInstallableUnit unit)
        throws IOException, ArtifactNotFoundException {
        List<EclipseFeatureOption> unitFeatures = new ArrayList<>();
        for (EclipseClassifiedVersionedArtifact artifact : unit.getArtifacts()) {
            if (EclipseClassifiedVersionedArtifact.CLASSIFIER_BUNDLE
                .equals(artifact.getClassifier())) {
                if (getBundles().get(artifact) == null) {
                    EclipseBundleOption bundle = unit.getSource().bundle(artifact.getId(),
                        toVersionRange(artifact.getVersion()));
                    getBundles().add(new ArtifactInfo<EclipseBundleOption>(bundle, bundle));
                }
            }
            else if (EclipseClassifiedVersionedArtifact.CLASSIFIER_FEATURE
                .equals(artifact.getClassifier())) {
                if (getFeatures().get(artifact) == null) {
                    EclipseFeatureOption feature = unit.getSource().feature(artifact.getId(),
                        toVersionRange(artifact.getVersion()));
                    getFeatures().add(new ArtifactInfo<EclipseFeatureOption>(feature, feature));
                    unitFeatures.add(feature);
                }
            }
            else {
                LOG.warn("Ignore artifact {} with unkown classifier: {}",
                    new Object[] { artifact, artifact.getClassifier() });
            }
        }
        return unitFeatures;
    }

    @Override
    protected EclipseInstallableUnit getUnit(ArtifactInfo<EclipseInstallableUnit> unitInfo)
        throws IOException {
        if (unitInfo.getContext() == null) {
            // this is an (unresolved) requirement
            throw new ArtifactNotFoundException(unitInfo);
        }
        return unitInfo.getContext();
    }

    @Override
    protected EclipseFeatureOption getFeature(ArtifactInfo<EclipseFeatureOption> featureInfo)
        throws IOException {
        if (featureInfo.getContext() == null) {
            // this is an (unresolved) requirement
            throw new ArtifactNotFoundException(featureInfo);
        }
        return featureInfo.getContext();
    }

    @Override
    protected EclipseBundleOption getBundle(ArtifactInfo<EclipseBundleOption> bundleInfo)
        throws IOException {
        if (bundleInfo.getContext() == null) {
            // this is an (unresolved) requirement
            throw new ArtifactNotFoundException(bundleInfo);
        }
        return bundleInfo.getContext();
    }

    private static VersionRange toVersionRange(Version version) {
        // TODO check if this is more meant as an "at-least" version
        return new VersionRange(VersionRange.LEFT_CLOSED, version, version,
            VersionRange.RIGHT_CLOSED);
    }

    private static boolean fullfills(UnitProviding providing, UnitRequirement requires) {
        if (providing.getNamespace().equals(requires.getNamespace())) {
            if (providing.getName().equals(requires.getName())) {
                return requires.getVersionRange().includes(providing.getVersion());
            }
        }
        return false;
    }

}
