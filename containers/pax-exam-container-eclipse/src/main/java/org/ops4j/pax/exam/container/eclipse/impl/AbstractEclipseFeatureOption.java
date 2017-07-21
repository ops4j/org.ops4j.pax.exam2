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

import java.util.Collections;
import java.util.List;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.options.CompositeOption;

/**
 * Abstract helper class to reduce the work to be done when implementing a
 * {@link EclipseFeatureOption}
 * 
 * @author Christoph Läubrich
 *
 * @param <T>
 */
public abstract class AbstractEclipseFeatureOption<T> extends AbstractEclipseBundleOption<T>
    implements EclipseFeatureOption, CompositeOption {

    private BundleInfo<T> bundleInfo;

    public AbstractEclipseFeatureOption(BundleInfo<T> bundleInfo) {
        super(bundleInfo);
        this.bundleInfo = bundleInfo;
    }

    @Override
    public final List<EclipseBundle> getBundles() {
        return Collections.unmodifiableList(getBundles(bundleInfo));
    }

    @Override
    public final List<EclipseFeature> getIncluded() {
        return Collections.unmodifiableList(getIncluded(bundleInfo));
    }

    @Override
    public final boolean isOptional() {
        return isOptional(bundleInfo);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + bundleInfo;
    }

    protected abstract List<? extends EclipseFeature> getIncluded(BundleInfo<T> bundleInfo);

    protected abstract List<? extends EclipseBundle> getBundles(BundleInfo<T> bundleInfo);

    protected abstract boolean isOptional(BundleInfo<T> bundleInfo);

    protected abstract Option toOption(BundleInfo<T> bundleInfo);
}
