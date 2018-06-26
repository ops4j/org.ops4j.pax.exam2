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

import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProjectParser;
import org.ops4j.pax.exam.container.eclipse.impl.sources.AbstractEclipseFeatureSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Source for workspace features
 * 
 * @author Christoph Läubrich
 *
 */
public class WorkspaceEclipseFeatureSource
    extends AbstractEclipseFeatureSource<WorkspaceFeatureProject> {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceEclipseFeatureSource.class);

    @Override
    protected EclipseFeatureOption getArtifact(ArtifactInfo<WorkspaceFeatureProject> featureInfo)
        throws IOException {
        return new WorkspaceEclipseFeatureOption(featureInfo);
    }

    public void addFeature(ProjectParser project) {
        try {
            FeatureParser feature = new FeatureParser(project.getProjectFolder());
            WorkspaceFeatureProject featureProject = new WorkspaceFeatureProject(project, feature);
            ArtifactInfo<WorkspaceFeatureProject> featureInfo = new ArtifactInfo<>(feature.getId(),
                feature.getVersion(), featureProject);
            if (add(featureInfo)) {
                LOG.debug("Add feature {} ...", featureInfo);
            }
        }
        catch (Exception e) {
            LOG.warn("can't read feature-project {} ({})", project, e.toString());
        }

    }

}
