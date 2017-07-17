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

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseOptions;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProjectParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.DirectoryTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.TargetPlatformLocation;
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
            if (location instanceof DirectoryTargetPlatformLocation) {
                File folder = resolveFolder((DirectoryTargetPlatformLocation) location,
                    targetDefinition);
                bundleSources.add(new InstallationEclipseBundleSource(folder));
            }
            else {
                LOG.warn("location of type {} is currently not supported!", location.type);
            }
        }
        if (bundleSources.size() > 0) {
            EclipseBundleSource primary = bundleSources.remove(0);
            bundleSource = EclipseOptions.withFallback(primary,
                bundleSources.toArray(new EclipseBundleSource[0]));
        }
    }

    private File resolveFolder(DirectoryTargetPlatformLocation locations, InputStream stream)
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
    public Option resolve(String bundleName, String bundleVersion)
        throws IOException, BundleNotFoundException {
        return bundleSource.resolve(bundleName, bundleVersion);
    }

}
