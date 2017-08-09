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
import org.ops4j.pax.exam.container.eclipse.impl.BundleArtifactInfo;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.ProvisionControl;
import org.osgi.framework.Version;

/**
 * Abstract helper class to reduce the work to do when providing EclipseBundleOptions
 * 
 * @author Christoph Läubrich
 *
 * @param <T>
 */
public abstract class AbstractEclipseBundleOption<T>
    implements EclipseBundleOption, CompositeOption, ProvisionControl<ProvisionControl<?>> {

    private final BundleArtifactInfo<T> bundleInfo;
    private Integer startlevel;
    private boolean shouldStart;
    private boolean shouldUpdate;

    public AbstractEclipseBundleOption(BundleArtifactInfo<T> bundleInfo) {
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

    @Override
    public Integer getStartLevel() {
        return startlevel;
    }

    @Override
    public ProvisionControl<?> start(Boolean shouldStart) {
        this.shouldStart = shouldStart;
        return this;
    }

    @Override
    public ProvisionControl<?> startLevel(Integer startLevel) {
        startlevel = startLevel;
        return this;
    }

    @Override
    public ProvisionControl<?> update(Boolean shouldUpdate) {
        this.shouldUpdate = shouldUpdate;
        return this;
    }

    @Override
    public ProvisionControl<?> noStart() {
        return start(false);
    }

    @Override
    public ProvisionControl<?> noUpdate() {
        return update(false);
    }

    @Override
    public boolean shouldStart() {
        if (isFragment()) {
            return false;
        }
        return shouldStart;
    }

    @Override
    public boolean shouldUpdate() {
        return shouldUpdate;
    }

    @Override
    public ProvisionControl<?> start() {
        return start(true);
    }

    @Override
    public ProvisionControl<?> update() {
        return update(true);
    }

    @Override
    public boolean isFragment() {
        return bundleInfo.isFragment();
    }

    @Override
    public boolean isSingleton() {
        return bundleInfo.isSingleton();
    }

    protected abstract Option toOption(BundleArtifactInfo<T> bundleInfo);
}
