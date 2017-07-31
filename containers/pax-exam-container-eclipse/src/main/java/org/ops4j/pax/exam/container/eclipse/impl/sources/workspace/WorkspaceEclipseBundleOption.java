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

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.options.AbstractEclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProjectParser;

/**
 * Option for Workspace Bundles
 * 
 * @author Christoph Läubrich
 *
 */
public final class WorkspaceEclipseBundleOption extends AbstractEclipseBundleOption<ProjectParser> {

    public WorkspaceEclipseBundleOption(ArtifactInfo<ProjectParser> bundleInfo) {
        super(bundleInfo);
    }

    @Override
    protected Option toOption(ArtifactInfo<ProjectParser> bundleInfo) {
        try {
            return WorkspaceResolver.projectToOption(bundleInfo.getContext(), this);
        }
        catch (IOException e) {
            throw new TestContainerException("option creation failed", e);
        }
    }
}
