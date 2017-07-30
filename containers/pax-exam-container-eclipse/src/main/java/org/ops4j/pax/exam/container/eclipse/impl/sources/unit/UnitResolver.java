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
import java.util.Collection;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.ResolvedArtifacts;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitProviding;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.UnitRequirement;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision.IncludeMode;
import org.ops4j.pax.exam.container.eclipse.impl.repository.ResolvedRequirements;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureAndUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseUnitSource;
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

    private static final Logger LOG = LoggerFactory.getLogger(UnitResolver.class);

    private final EclipseUnitSource[] repositories;
    private final IncludeMode mode;
    private final ResolvedRequirements resolvedRequirements = new ResolvedRequirements();
    private final ContextEclipseBundleSource bundleSource = new ContextEclipseBundleSource();
    private final ContextEclipseFeatureSource featureSource = new ContextEclipseFeatureSource();
    private final ContextEclipseUnitSource unitSource = new ContextEclipseUnitSource();

    public UnitResolver(Collection<? extends EclipseUnitSource> repositories, IncludeMode mode,
        Collection<EclipseInstallableUnit> units) throws ArtifactNotFoundException, IOException {
        this.mode = mode;
        this.repositories = repositories.toArray(new EclipseUnitSource[0]);
        for (EclipseInstallableUnit unit : units) {
            // TODO provide a "path" so error can give exact information from where an requirement
            // can't be resolved
            resolveUnit(unit);
        }
    }

    private void resolveUnit(EclipseInstallableUnit unit)
        throws ArtifactNotFoundException, IOException {
        if (unitSource.containsUnit(unit) || resolvedRequirements.containsUnit(unit)) {
            LOG.debug("Skip resolving of {}, already resolved or resolving in progress...", unit);
            return;
        }
        resolvedRequirements.addUnit(unit);
        unitSource.addUnit(unit);
        LOG.info("Resolve {}...", unit);
        addArtifacts(unit);
        resolvedRequirements.addResolved(unit.getProvided());
        for (UnitRequirement requires : unit.getRequirements()) {
            if (resolvedRequirements.isResolved(requires)) {
                LOG.info("Skip requirement {} because it is already resolved...", requires);
                continue;
            }
            if (resolvedRequirements.isFailed(requires)) {
                LOG.debug("Skip requirement {} because it has already failed...", requires);
                continue;
            }
            LOG.debug("Try to resolve {}...", requires);
            resolveRequirement(requires, unit);
        }
    }

    private void resolveRequirement(UnitRequirement requires, EclipseInstallableUnit unit)
        throws IOException {
        EclipseUnitSource primarySource = unit.getSource();
        try {
            Collection<EclipseInstallableUnit> allUnits = primarySource.getAllUnits();
            for (EclipseInstallableUnit reproUnit : allUnits) {
                Collection<UnitProviding> provided = reproUnit.getProvided();
                for (UnitProviding providing : provided) {
                    if (fullfills(providing, requires)) {
                        resolveUnit(reproUnit);
                        return;
                    }
                }
            }
            throw new ArtifactNotFoundException("no unit fullfills the requirement " + requires);
        }
        catch (ArtifactNotFoundException anfe) {
            if (mode == IncludeMode.SLICER) {
                LOG.info("Failed to resolve {} ({}), ignore because of slicer mode...", requires,
                    anfe.getMessage());
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
                    Collection<UnitProviding> provided = reproUnit.getProvided();
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

    private ArtifactNotFoundException failedRequirement(UnitRequirement requires) {
        return new ArtifactNotFoundException("can't resolve " + requires.getNamespace()
            + "-requirement " + requires.getName() + ":" + requires.getVersionRange());
    }

    private void addArtifacts(EclipseInstallableUnit unit)
        throws IOException, ArtifactNotFoundException {
        ResolvedArtifacts artifacts = unit.resolveArtifacts();
        for (EclipseBundleOption bundle : artifacts.getBundles()) {
            if (bundleSource.addBundle(bundle)) {
                LOG.info("Resolve bundle {}:{}...", bundle.getId(), bundle.getVersion());
            }
        }
        for (EclipseFeatureOption feature : artifacts.getFeatures()) {
            if (featureSource.addFeature(feature)) {
                LOG.info("Resolve feature {}:{}...", feature.getId(), feature.getVersion());
            }
        }
    }

    private static boolean fullfills(UnitProviding providing, UnitRequirement requires) {
        if (providing.getNamespace().equals(requires.getNamespace())) {
            if (providing.getName().equals(requires.getName())) {
                return requires.getVersionRange().includes(providing.getVersion());
            }
        }
        return false;
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
