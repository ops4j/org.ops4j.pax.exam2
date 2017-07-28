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

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.options.AbstractEclipseBundleOption;

/**
 * 
 * @author Christoph Läubrich
 *
 */
public final class P2EclipseBundleOption extends AbstractEclipseBundleOption<P2Bundle> {

    private final String reproName;

    public P2EclipseBundleOption(ArtifactInfo<P2Bundle> bundleInfo) {
        super(bundleInfo);
        this.reproName = bundleInfo.getContext().getReproName();
    }

    @Override
    protected Option toOption(ArtifactInfo<P2Bundle> bundleInfo) {
        return CoreOptions.bundle(bundleInfo.getContext().getUrl().toExternalForm());
    }

    @Override
    public String toString() {
        if (reproName != null) {
            return super.toString() + ":" + reproName;
        }
        return super.toString();
    }
}
