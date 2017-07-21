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
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseOptions;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProjectParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.DirectoryTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.FeatureTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.PathTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.ProfileTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.TargetPlatformLocation;
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
public class TargetEclipseBundleSource implements EclipseBundleSource {

    public static final Logger LOG = LoggerFactory.getLogger(TargetEclipseBundleSource.class);
    private EclipseBundleSource bundleSource;

    public TargetEclipseBundleSource(InputStream targetDefinition) throws IOException {
        List<EclipseBundleSource> bundleSources = new ArrayList<>();
        TargetPlatformParser target = new TargetPlatformParser(targetDefinition);
        List<TargetPlatformLocation> locations = target.getLocations();
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
                EclipseBundleSource source = DirectoryEclipseBundleSource.create(folder);
                EclipseFeatureOption feature = source.feature(featureLocation.id,
                    featureLocation.version);
                bundleSources.add(
                    new FeatureEclipseBundleSource(source, new EclipseFeatureOption[] { feature }));
            }
            else {
                LOG.warn("location of type {} is currently not supported!", location.type);
            }
        }
        bundleSource = EclipseOptions.combine(bundleSources.toArray(new EclipseBundleSource[0]));
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
    public EclipseBundleOption bundle(String bundleName, Version bundleVersion)
        throws IOException, BundleNotFoundException {
        return bundleSource.bundle(bundleName, bundleVersion);
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, VersionRange bundleVersionRange)
        throws IOException, BundleNotFoundException {
        return bundleSource.bundle(bundleName, bundleVersionRange);
    }

    @Override
    public EclipseFeatureOption feature(String featureName, Version featureVersion)
        throws IOException, BundleNotFoundException {
        return bundleSource.feature(featureName, featureVersion);
    }

    @Override
    public EclipseFeatureOption feature(String featureName, VersionRange featureVersionRange)
        throws IOException, BundleNotFoundException {
        return bundleSource.feature(featureName, featureVersionRange);
    }

}
