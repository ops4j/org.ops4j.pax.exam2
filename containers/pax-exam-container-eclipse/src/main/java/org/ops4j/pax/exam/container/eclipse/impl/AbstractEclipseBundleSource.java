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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.osgi.framework.VersionRange;

/**
 * Abstract class for implementations based on an {@link ArtifactInfoMap} that want to provide
 * {@link EclipseBundleSource}s
 * 
 * @author Christoph Läubrich
 *
 * @param <BundleInfoContext>
 */
public abstract class AbstractEclipseBundleSource<BundleInfoContext>
    implements EclipseBundleSource {

    private ArtifactInfoMap<BundleInfoContext> bundles;

    @Override
    public final EclipseBundleOption bundle(String bundleSymbolicName)
        throws IOException, ArtifactNotFoundException {
        return bundle(bundleSymbolicName, HIGHEST_VERSION);
    }

    @Override
    public final EclipseBundleOption bundle(String bundleSymbolicName,
        VersionRange bundleVersionRange) throws IOException, ArtifactNotFoundException {
        ArtifactInfo<BundleInfoContext> info = getBundles().get(bundleSymbolicName,
            bundleVersionRange);
        if (info == null) {
            throw new ArtifactNotFoundException("bundle", bundleSymbolicName, bundleVersionRange);
        }
        return getBundle(info);
    }

    /**
     * 
     * @return all bundles that are resolved by the feature set of this source
     * @throws IOException
     */
    public final List<EclipseBundleOption> getIncludedBundles() throws IOException {
        ArrayList<EclipseBundleOption> list = new ArrayList<>();
        for (ArtifactInfo<BundleInfoContext> bundleInfo : getBundles().getArtifacts()) {
            try {
                list.add(getBundle(bundleInfo));
            }
            catch (ArtifactNotFoundException e) {
                // just in case ... ignore it
            }
        }
        return list;
    }

    protected abstract EclipseBundleOption getBundle(ArtifactInfo<BundleInfoContext> bundleInfo)
        throws IOException;

    protected ArtifactInfoMap<BundleInfoContext> getBundles() {
        if (bundles == null) {
            bundles = new ArtifactInfoMap<>();
        }
        return bundles;
    }

}
