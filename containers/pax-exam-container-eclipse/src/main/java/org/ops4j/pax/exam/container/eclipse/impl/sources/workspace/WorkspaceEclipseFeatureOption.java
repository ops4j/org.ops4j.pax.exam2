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
import java.util.List;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.options.AbstractEclipseFeatureOption;

/**
 * Option for workspace features
 * 
 * @author Christoph Läubrich
 *
 */
public final class WorkspaceEclipseFeatureOption
    extends AbstractEclipseFeatureOption<WorkspaceFeatureProject> {

    public WorkspaceEclipseFeatureOption(ArtifactInfo<WorkspaceFeatureProject> bundleInfo) {
        super(bundleInfo);
    }

    @Override
    protected List<? extends EclipseFeature> getIncluded(
        ArtifactInfo<WorkspaceFeatureProject> bundleInfo) {
        return bundleInfo.getContext().getFeature().getIncluded();
    }

    @Override
    protected List<? extends EclipseBundle> getBundles(
        ArtifactInfo<WorkspaceFeatureProject> bundleInfo) {
        return bundleInfo.getContext().getFeature().getPlugins();
    }

    @Override
    protected boolean isOptional(ArtifactInfo<WorkspaceFeatureProject> bundleInfo) {
        return false;
    }

    @Override
    protected Option toOption(ArtifactInfo<WorkspaceFeatureProject> bundleInfo) {
        try {
            return WorkspaceResolver.projectToOption(bundleInfo.getContext().getProject());
        }
        catch (IOException e) {
            throw new TestContainerException("creation of option failed", e);
        }
    }
}
