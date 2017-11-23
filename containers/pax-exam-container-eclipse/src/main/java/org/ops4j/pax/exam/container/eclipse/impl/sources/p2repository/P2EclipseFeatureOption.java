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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.options.AbstractEclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;
import org.ops4j.pax.exam.options.StreamReference;

/**
 * 
 * @author Christoph Läubrich
 *
 */
public final class P2EclipseFeatureOption extends AbstractEclipseFeatureOption<P2Feature>
    implements StreamReference {

    private final FeatureParser featureParser;
    private final String location;

    public P2EclipseFeatureOption(ArtifactInfo<P2Feature> info, FeatureParser featureParser) {
        super(info);
        this.featureParser = featureParser;
        this.location = info.getContext().getReproName();
    }

    @Override
    public List<EclipseFeature> getIncluded() {
        return Collections.unmodifiableList(featureParser.getIncluded());
    }

    @Override
    public List<EclipseFeatureBundle> getBundles() {
        return Collections.unmodifiableList(featureParser.getPlugins());
    }

    @Override
    public boolean isOptional() {
        return false;
    }

    @Override
    protected Option toOption() {
        return CoreOptions.bundle(getArtifactInfo().getContext().getUrl().toExternalForm());
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

    @Override
    public InputStream createStream() throws IOException {
        return getArtifactInfo().getContext().getUrl().openStream();
    }
}
