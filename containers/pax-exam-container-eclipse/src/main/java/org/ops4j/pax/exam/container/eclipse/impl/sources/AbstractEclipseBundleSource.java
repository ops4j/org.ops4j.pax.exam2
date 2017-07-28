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

import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfoMap;
import org.osgi.framework.Version;
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
    extends AbstractEclipseArtifactSource<BundleInfoContext, EclipseBundleOption>
    implements EclipseBundleSource {

    @Override
    public final EclipseBundleOption bundle(String bundleSymbolicName)
        throws IOException, ArtifactNotFoundException {
        return bundle(bundleSymbolicName, Version.emptyVersion);
    }

    @Override
    public final EclipseBundleOption bundle(String bundleSymbolicName, Version bundleVersion)
        throws IOException, ArtifactNotFoundException {
        ArtifactInfo<BundleInfoContext> info = getArtifactsMap().get(bundleSymbolicName,
            bundleVersion);
        if (info == null) {
            throw new ArtifactNotFoundException("bundle", bundleSymbolicName, bundleVersion);
        }
        return getArtifact(info);
    }

    @Override
    public final EclipseBundleOption bundle(String bundleSymbolicName,
        VersionRange bundleVersionRange) throws IOException, ArtifactNotFoundException {
        ArtifactInfo<BundleInfoContext> info = getArtifactsMap().get(bundleSymbolicName,
            bundleVersionRange);
        if (info == null) {
            throw new ArtifactNotFoundException("bundle", bundleSymbolicName, bundleVersionRange);
        }
        return getArtifact(info);
    }

}
