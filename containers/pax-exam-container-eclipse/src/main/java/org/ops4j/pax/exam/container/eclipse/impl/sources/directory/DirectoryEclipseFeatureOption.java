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
package org.ops4j.pax.exam.container.eclipse.impl.sources.directory;

import java.io.File;
import java.util.List;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.options.AbstractEclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser.PluginInfo;

/**
 * FeatureOption Option for {@link DirectoryResolver}
 * 
 * @author Christoph Läubrich
 *
 */
public final class DirectoryEclipseFeatureOption
    extends AbstractEclipseFeatureOption<DirectoryFeatureFile> {

    public DirectoryEclipseFeatureOption(ArtifactInfo<DirectoryFeatureFile> bundleInfo)
        throws ArtifactNotFoundException {
        super(bundleInfo);
        File file = bundleInfo.getContext().getFile();
        if (!file.exists()) {
            throw new ArtifactNotFoundException(
                "Can't resolve bundle '" + file.getAbsolutePath() + "' does not exists");
        }
    }

    @Override
    protected List<? extends EclipseFeature> getIncluded(
        ArtifactInfo<DirectoryFeatureFile> bundleInfo) {
        return bundleInfo.getContext().getFeature().getIncluded();
    }

    @Override
    protected List<PluginInfo> getBundles(ArtifactInfo<DirectoryFeatureFile> bundleInfo) {
        return bundleInfo.getContext().getFeature().getPlugins();
    }

    @Override
    protected boolean isOptional(ArtifactInfo<DirectoryFeatureFile> bundleInfo) {
        return false;
    }

    @Override
    protected Option toOption(ArtifactInfo<DirectoryFeatureFile> bundleInfo) {
        return CoreOptions.bundle(bundleInfo.getContext().getFile().getAbsolutePath());
    }
}
