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
package org.ops4j.pax.exam.container.eclipse.impl.sources.target;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision.IncludeMode;
import org.ops4j.pax.exam.container.eclipse.EclipseTargetPlatform;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.CombinedSource;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProjectParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.DirectoryTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.FeatureTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.InstallableUnitTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.PathTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.ProfileTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.TargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureAndUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.directory.DirectoryResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.feature.FeatureResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository.P2Resolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.unit.UnitResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.workspace.ProjectFileInputStream;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A source of bundles based on an eclipse target
 * 
 * @author Christoph Läubrich
 *
 */
public class TargetResolver extends BundleAndFeatureAndUnitSource implements EclipseTargetPlatform {

    public static final Logger LOG = LoggerFactory.getLogger(TargetResolver.class);
    private final CombinedSource combinedSource;

    public TargetResolver(InputStream targetDefinition) throws IOException {
        List<EclipseArtifactSource> bundleSources = new ArrayList<>();
        TargetPlatformParser target = new TargetPlatformParser(targetDefinition);
        List<TargetPlatformLocation> locations = target.getLocations();
        List<P2Resolver> repositories = new ArrayList<>();
        List<EclipseInstallableUnit> installunits = new ArrayList<>();
        Map<String, DirectoryResolver> directoryResolverCache = new HashMap<>();
        int cnt = 0;
        for (TargetPlatformLocation location : locations) {
            cnt++;
            if (location instanceof DirectoryTargetPlatformLocation
                || location instanceof ProfileTargetPlatformLocation) {
                File folder = resolveFolder((PathTargetPlatformLocation) location,
                    targetDefinition);
                DirectoryResolver resolver = getResolver(folder, directoryResolverCache);
                bundleSources.add(resolver);
            }
            else if (location instanceof FeatureTargetPlatformLocation) {
                FeatureTargetPlatformLocation featureLocation = (FeatureTargetPlatformLocation) location;
                File folder = resolveFolder((PathTargetPlatformLocation) location,
                    targetDefinition);
                DirectoryResolver source = getResolver(folder, directoryResolverCache);
                EclipseFeatureOption feature = source.feature(featureLocation.id,
                    featureLocation.version);
                FeatureResolver featureResolver = new FeatureResolver(source, source,
                    Collections.singleton(feature));
                bundleSources.add(featureResolver);
            }
            else if (location instanceof InstallableUnitTargetPlatformLocation) {
                InstallableUnitTargetPlatformLocation iuLocation = (InstallableUnitTargetPlatformLocation) location;
                P2Resolver repository = new P2Resolver("target-platform-" + cnt,
                    new URL(iuLocation.repository));
                repositories.add(repository);
                List<EclipseInstallableUnit> local = new ArrayList<>();
                for (ArtifactInfo<?> unit : iuLocation.units) {
                    Version version = unit.getVersion();
                    EclipseInstallableUnit iu = repository.unit(unit.getId(), version);
                    local.add(iu);
                }
                if (iuLocation.mode == IncludeMode.SLICER) {
                    UnitResolver source = new UnitResolver(
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
            UnitResolver source = new UnitResolver(
                repositories, IncludeMode.PLANNER, installunits);
            bundleSources.add(source);
        }
        combinedSource = new CombinedSource(bundleSources);
    }

    private DirectoryResolver getResolver(File folder, Map<String, DirectoryResolver> cache)
        throws IOException {
        String key = folder.getCanonicalPath();
        DirectoryResolver resolver = cache.get(key);
        if (resolver == null) {
            resolver = new DirectoryResolver(folder);
            cache.put(key, resolver);
        }
        return resolver;
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
    protected EclipseBundleSource getBundleSource() {
        return combinedSource;
    }

    @Override
    protected EclipseFeatureSource getFeatureSource() {
        return combinedSource;
    }

    @Override
    protected EclipseUnitSource getUnitSource() {
        return combinedSource;
    }

}
