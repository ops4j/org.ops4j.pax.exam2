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
package org.ops4j.pax.exam.container.eclipse.impl;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.options.CompositeOption;
import org.osgi.framework.Version;

/**
 * Abstrect helper class to reduce the work to do when providing EclipseBundleOptions
 * 
 * @author Christoph Läubrich
 *
 * @param <T>
 */
public abstract class AbstractEclipseBundleOption<T>
    implements EclipseBundleOption, CompositeOption {

    private final BundleInfo<T> bundleInfo;

    public AbstractEclipseBundleOption(BundleInfo<T> bundleInfo) {
        this.bundleInfo = bundleInfo;
    }

    @Override
    public Version getVersion() {
        return bundleInfo.getVersion();
    }

    @Override
    public String getSymbolicName() {
        return bundleInfo.getSymbolicName();
    }

    @Override
    public Option[] getOptions() {
        return CoreOptions.options(toOption(bundleInfo));
    }

    protected abstract Option toOption(BundleInfo<T> bundleInfo);
}
