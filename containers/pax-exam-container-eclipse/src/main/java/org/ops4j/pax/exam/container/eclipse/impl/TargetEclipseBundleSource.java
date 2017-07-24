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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision.IncludeMode;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProjectParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.DirectoryTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.FeatureTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.InstallableUnitTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.PathTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.ProfileTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.TargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.repository.P2EclipseRepositorySource;
import org.ops4j.pax.exam.container.eclipse.impl.repository.RepositoryResolverEclipseBundleSource;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A source of bundles based on an eclipse target
 * 
 * @author Christoph Läubrich
 *
 */
public class TargetEclipseBundleSource implements EclipseFeatureSource {

    public static final Logger LOG = LoggerFactory.getLogger(TargetEclipseBundleSource.class);
    private final CombinedSource bundleSource;

    public TargetEclipseBundleSource(InputStream targetDefinition) throws IOException {
        List<EclipseArtifactSource> bundleSources = new ArrayList<>();
        TargetPlatformParser target = new TargetPlatformParser(targetDefinition);
        List<TargetPlatformLocation> locations = target.getLocations();
        List<P2EclipseRepositorySource> repositories = new ArrayList<>();
        List<EclipseInstallableUnit> installunits = new ArrayList<>();
        for (TargetPlatformLocation location : locations) {
            if (location instanceof DirectoryTargetPlatformLocation
                || location instanceof ProfileTargetPlatformLocation) {
                File folder = resolveFolder((PathTargetPlatformLocation) location,
                    targetDefinition);
                bundleSources.add(DirectoryEclipseBundleSource.create(folder));
            }
            else if (location instanceof FeatureTargetPlatformLocation) {
                FeatureTargetPlatformLocation featureLocation = (FeatureTargetPlatformLocation) location;
                File folder = resolveFolder((PathTargetPlatformLocation) location,
                    targetDefinition);
                EclipseFeatureSource source = DirectoryEclipseBundleSource.create(folder);
                EclipseFeatureOption feature = source.feature(featureLocation.id,
                    createVersionRange(featureLocation.version));
                bundleSources
                    .add(new FeatureEclipseBundleSource(source, Collections.singleton(feature)));
            }
            else if (location instanceof InstallableUnitTargetPlatformLocation) {
                InstallableUnitTargetPlatformLocation iuLocation = (InstallableUnitTargetPlatformLocation) location;
                P2EclipseRepositorySource repository = new P2EclipseRepositorySource(
                    new URL(iuLocation.repository), "target-platform");
                repositories.add(repository);
                List<EclipseInstallableUnit> local = new ArrayList<>();
                for (ArtifactInfo<?> unit : iuLocation.units) {
                    Version version = unit.getVersion();
                    VersionRange versionRange = createVersionRange(version);
                    EclipseInstallableUnit iu = repository.unit(unit.getId(), versionRange);
                    local.add(iu);
                }
                if (iuLocation.mode == IncludeMode.SLICER) {
                    RepositoryResolverEclipseBundleSource source = new RepositoryResolverEclipseBundleSource(
                        Collections.singleton(repository), IncludeMode.SLICER, local);
                    bundleSources.add(source);
                }
                else {
                    installunits.addAll(local);
                }
            }
            else {
                LOG.warn("location of type {} is currently not supported!", location.type);
            }
        }
        if (!installunits.isEmpty()) {
            // now resolve the big thing then...
            RepositoryResolverEclipseBundleSource source = new RepositoryResolverEclipseBundleSource(
                repositories, IncludeMode.PLANNER, installunits);
            bundleSources.add(source);
        }
        bundleSource = new CombinedSource(bundleSources);
    }

    private VersionRange createVersionRange(Version version) {
        VersionRange versionRange = new VersionRange(VersionRange.LEFT_CLOSED, version, version,
            VersionRange.RIGHT_CLOSED);
        return versionRange;
    }

    private File resolveFolder(PathTargetPlatformLocation locations, InputStream stream)
        throws IOException {
        String path = locations.path;
        if (stream instanceof ProjectFileInputStream) {
            ProjectFileInputStream prjStream = (ProjectFileInputStream) stream;
            ProjectParser project = prjStream.getProject();
            String projectFolder = project.getProjectFolder().getCanonicalPath();
            path = path.replace("${project_loc}", projectFolder);
            path = path.replace("${project_name}", project.getName());
            path = path.replace(" ${project_path}", projectFolder
                .substring(prjStream.getWorkspaceFolder().getCanonicalPath().length()));
            // TODO ${system_property:xxx}
        }
        return new File(path);
    }

    @Override
    public EclipseBundleOption bundle(String bundleName)
        throws IOException, ArtifactNotFoundException {
        return bundleSource.bundle(bundleName);
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, VersionRange bundleVersionRange)
        throws IOException, ArtifactNotFoundException {
        return bundleSource.bundle(bundleName, bundleVersionRange);
    }

    @Override
    public EclipseFeatureOption feature(String featureName)
        throws IOException, ArtifactNotFoundException {
        return bundleSource.feature(featureName);
    }

    @Override
    public EclipseFeatureOption feature(String featureName, VersionRange featureVersionRange)
        throws IOException, ArtifactNotFoundException {
        return bundleSource.feature(featureName, featureVersionRange);
    }

}
