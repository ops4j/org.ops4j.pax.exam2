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
package org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository;

import java.util.List;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.options.AbstractEclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser.PluginInfo;

/**
 * 
 * @author Christoph Läubrich
 *
 */
public final class P2EclipseFeatureOption extends AbstractEclipseFeatureOption<P2Feature> {

    private final FeatureParser featureParser;
    private final String location;

    public P2EclipseFeatureOption(ArtifactInfo<P2Feature> info, FeatureParser featureParser) {
        super(info);
        this.featureParser = featureParser;
        this.location = info.getContext().getReproName();
    }

    @Override
    protected List<? extends EclipseFeature> getIncluded(ArtifactInfo<P2Feature> bundleInfo) {
        return featureParser.getIncluded();
    }

    @Override
    protected List<PluginInfo> getBundles(ArtifactInfo<P2Feature> bundleInfo) {
        return featureParser.getPlugins();
    }

    @Override
    protected boolean isOptional(ArtifactInfo<P2Feature> bundleInfo) {
        return false;
    }

    @Override
    protected Option toOption(ArtifactInfo<P2Feature> bundleInfo) {
        return CoreOptions.bundle(bundleInfo.getContext().getUrl().toExternalForm());
    }

    @Override
    public String toString() {
        if (location != null) {
            return super.toString() + ":" + location;
        }
        else {
            return super.toString();
        }
    }
}
