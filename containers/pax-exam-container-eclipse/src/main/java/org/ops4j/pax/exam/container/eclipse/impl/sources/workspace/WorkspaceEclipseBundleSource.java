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
package org.ops4j.pax.exam.container.eclipse.impl.sources.workspace;

import java.io.IOException;

import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProjectParser;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseBundleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Source of WOrkspace bundles
 * 
 * @author Christoph Läubrich
 *
 */
public class WorkspaceEclipseBundleSource extends AbstractEclipseBundleSource<ProjectParser> {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceEclipseBundleSource.class);

    @Override
    protected EclipseBundleOption getArtifact(ArtifactInfo<ProjectParser> bundleInfo)
        throws IOException {
        return new WorkspaceEclipseBundleOption(bundleInfo);
    }

    public void addProject(ProjectParser project) {
        if (project.hasNature(ProjectParser.PLUGIN_NATURE)) {
            // the project must have a META-INF at the root folder like an exploded
            // bundle...
            try {
                ArtifactInfo<ProjectParser> bundle = ArtifactInfo
                    .readExplodedBundle(project.getProjectFolder(), project);
                if (add(bundle)) {
                    LOG.info("Add bundle {} ...", bundle);
                }
            }
            catch (Exception e) {
                LOG.warn("can't read plugin-project {} ({})", project, e.toString());
            }
        }
        else if (project.hasNature(ProjectParser.MAVEN2_NATURE)) {
            // TODO convert to maven url option or something??
        }
        else {
            LOG.info("Skipping java-project {} without plugin/maven2 nature...", project.getName());
        }

    }

}
