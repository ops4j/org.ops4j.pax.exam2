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
package org.ops4j.pax.exam.container.eclipse.impl.options;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.options.CompositeOption;
import org.osgi.framework.Version;

/**
 * Abstract helper class to reduce the work to be done when implementing a
 * {@link EclipseFeatureOption}
 * 
 * @author Christoph Läubrich
 *
 * @param <T>
 */
public abstract class AbstractEclipseFeatureOption<T>
    implements EclipseFeatureOption, CompositeOption {

    private final ArtifactInfo<T> bundleInfo;

    public AbstractEclipseFeatureOption(ArtifactInfo<T> bundleInfo) {
        this.bundleInfo = bundleInfo;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getArtifactInfo();
    }

    @Override
    public Version getVersion() {
        return getArtifactInfo().getVersion();
    }

    @Override
    public String getId() {
        return getArtifactInfo().getId();
    }

    @Override
    public Option[] getOptions() {
        return CoreOptions.options(toOption());
    }

    public ArtifactInfo<T> getArtifactInfo() {
        return bundleInfo;
    }

    protected abstract Option toOption();
}
