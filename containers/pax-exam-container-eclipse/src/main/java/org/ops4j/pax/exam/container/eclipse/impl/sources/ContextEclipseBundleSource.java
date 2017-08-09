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
package org.ops4j.pax.exam.container.eclipse.impl.sources;

import java.io.IOException;

import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.impl.BundleArtifactInfo;

/**
 * A {@link EclipseBundleSource} that simply use the context itself as the option
 * 
 * @author Christoph Läubrich
 *
 */
public class ContextEclipseBundleSource extends AbstractEclipseBundleSource<EclipseBundleOption> {

    @Override
    protected EclipseBundleOption getArtifact(BundleArtifactInfo<EclipseBundleOption> info)
        throws IOException {
        return info.getContext();
    }

    public boolean addBundle(EclipseBundleOption bundle) {
        return add(new BundleArtifactInfo<EclipseBundleOption>(bundle, bundle));
    }

    public boolean containsBundle(EclipseBundle bundle) {
        return contains(bundle);
    }

}
