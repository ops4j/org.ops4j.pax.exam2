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
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.options.CompositeOption;
import org.osgi.framework.Version;

/**
 * Abstract helper class to reduce the work to do when providing EclipseBundleOptions
 * 
 * @author Christoph Läubrich
 *
 * @param <T>
 */
public abstract class AbstractEclipseBundleOption<T>
    implements EclipseBundleOption, CompositeOption {

    private final ArtifactInfo<T> bundleInfo;

    public AbstractEclipseBundleOption(ArtifactInfo<T> bundleInfo) {
        this.bundleInfo = bundleInfo;
    }

    @Override
    public Version getVersion() {
        return bundleInfo.getVersion();
    }

    @Override
    public String getId() {
        return bundleInfo.getId();
    }

    @Override
    public Option[] getOptions() {
        return CoreOptions.options(toOption(bundleInfo));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + bundleInfo.toString();
    }

    protected abstract Option toOption(ArtifactInfo<T> bundleInfo);
}
