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
package org.ops4j.pax.exam.container.eclipse.impl.repository;

import java.net.URL;
import java.util.List;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.impl.AbstractEclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;

/**
 * 
 * @author Christoph Läubrich
 *
 */
public final class RepositoryEclipseFeatureOption extends AbstractEclipseFeatureOption<URL> {

    private final FeatureParser featureParser;
    private final EclipseUnitSource location;

    public RepositoryEclipseFeatureOption(ArtifactInfo<URL> bundleInfo, FeatureParser featureParser,
        EclipseUnitSource location) {
        super(bundleInfo);
        this.featureParser = featureParser;
        this.location = location;
    }

    @Override
    protected List<? extends EclipseFeature> getIncluded(ArtifactInfo<URL> bundleInfo) {
        return featureParser.getIncluded();
    }

    @Override
    protected List<? extends EclipseBundle> getBundles(ArtifactInfo<URL> bundleInfo) {
        return featureParser.getPlugins();
    }

    @Override
    protected boolean isOptional(ArtifactInfo<URL> bundleInfo) {
        return false;
    }

    @Override
    protected Option toOption(ArtifactInfo<URL> bundleInfo) {
        return CoreOptions.bundle(bundleInfo.getContext().toExternalForm());
    }

    @Override
    public String toString() {
        return super.toString() + ":" + location;
    }
}
