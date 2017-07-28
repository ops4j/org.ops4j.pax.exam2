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
package org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.impl.repository.EclipseClassifiedVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.repository.Unit;
import org.ops4j.pax.exam.container.eclipse.impl.sources.feature.FeatureResolver;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Christoph Läubrich
 *
 */
public class P2EclipseInstallableUnit implements EclipseInstallableUnit {

    private static final Logger LOG = LoggerFactory.getLogger(P2EclipseInstallableUnit.class);
    private final String repository;
    private final Unit unit;
    private final EclipseUnitSource source;
    private final P2ArtifactRepository artifactRepository;

    public P2EclipseInstallableUnit(P2Unit artifact, P2ArtifactRepository artifactRepository,
        EclipseUnitSource source) {
        this.artifactRepository = artifactRepository;
        this.source = source;
        this.unit = artifact.getUnit();
        this.repository = artifact.getReproName();
    }

    @Override
    public String getId() {
        return unit.getId();
    }

    @Override
    public Version getVersion() {
        return unit.getVersion();
    }

    @Override
    public List<UnitRequirement> getRequirements() {
        return unit.getRequires();
    }

    @Override
    public EclipseUnitSource getSource() {
        return source;
    }

    @Override
    public String toString() {
        return unit + ":" + repository;
    }

    @Override
    public P2ResolvedArtifacts resolveArtifacts() throws ArtifactNotFoundException, IOException {
        P2ResolvedArtifacts artifacts = new P2ResolvedArtifacts();
        List<EclipseFeatureOption> unitFeatures = new ArrayList<>();
        for (EclipseClassifiedVersionedArtifact artifact : unit.getArtifacts()) {
            if (EclipseClassifiedVersionedArtifact.CLASSIFIER_BUNDLE
                .equals(artifact.getClassifier())) {
                EclipseBundleOption bundle = artifactRepository.bundle(artifact.getId(),
                    artifact.getVersion());
                if (artifacts.getBundleSource().addBundle(bundle)) {
                    LOG.info("resolve artifact-bundle {}:{}", bundle.getId(), bundle.getVersion());
                }
            }
            else if (EclipseClassifiedVersionedArtifact.CLASSIFIER_FEATURE
                .equals(artifact.getClassifier())) {
                EclipseFeatureOption feature = artifactRepository.feature(artifact.getId(),
                    artifact.getVersion());
                if (artifacts.getFeatureSource().addFeature(feature)) {
                    LOG.info("resolve artifact-feature {}:{}", feature.getId(),
                        feature.getVersion());
                }
            }
        }
        if (!unitFeatures.isEmpty()) {
            FeatureResolver featureResolver = new FeatureResolver(artifactRepository,
                artifactRepository, unitFeatures);
            for (EclipseBundleOption bundle : featureResolver.getBundleSource()
                .getIncludedArtifacts()) {
                if (artifacts.getBundleSource().addBundle(bundle)) {
                    LOG.info("resolve artifact-bundle {}:{}", bundle.getId(), bundle.getVersion());
                }
            }
            for (EclipseFeatureOption feature : featureResolver.getFeatureSource()
                .getIncludedArtifacts()) {
                LOG.info("resolve artifact-feature {}:{}", feature.getId(), feature.getVersion());
            }
        }

        return artifacts;
    }

    @Override
    public List<UnitProviding> getProvided() {
        return unit.getProvides();
    }

}
