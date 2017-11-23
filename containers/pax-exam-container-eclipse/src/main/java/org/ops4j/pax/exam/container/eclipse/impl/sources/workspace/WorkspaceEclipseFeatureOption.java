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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.options.AbstractEclipseFeatureOption;
import org.ops4j.pax.exam.options.StreamReference;

/**
 * Option for workspace features
 * 
 * @author Christoph Läubrich
 *
 */
public final class WorkspaceEclipseFeatureOption
    extends AbstractEclipseFeatureOption<WorkspaceFeatureProject> implements StreamReference {

    public WorkspaceEclipseFeatureOption(ArtifactInfo<WorkspaceFeatureProject> bundleInfo) {
        super(bundleInfo);
    }

    @Override
    public List<EclipseFeature> getIncluded() {
        return Collections
            .unmodifiableList(getArtifactInfo().getContext().getFeature().getIncluded());
    }

    @Override
    public List<EclipseFeatureBundle> getBundles() {
        return Collections
            .unmodifiableList(getArtifactInfo().getContext().getFeature().getPlugins());
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    protected Option toOption() {
        try {
            return WorkspaceResolver.projectToOption(getArtifactInfo().getContext().getProject(),
                null);
        }
        catch (IOException e) {
            throw new TestContainerException("creation of option failed", e);
        }
    }

    @Override
    public InputStream createStream() throws IOException {
        return new ByteArrayInputStream(
            WorkspaceResolver.projectToByteArray(getArtifactInfo().getContext().getProject()));
    }
}
