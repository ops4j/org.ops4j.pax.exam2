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
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * A compound source that is based on an {@link EclipseBundleSource} and an
 * {@link EclipseFeatureSource}
 * 
 * @author Christoph Läubrich
 *
 */
public abstract class BundleAndFeatureSource implements EclipseBundleSource, EclipseFeatureSource {

    @Override
    public final EclipseFeatureOption feature(String featureId)
        throws IOException, ArtifactNotFoundException {
        return getFeatureSource().feature(featureId);
    }

    @Override
    public final EclipseFeatureOption feature(String featureId, VersionRange featureVersionRange)
        throws IOException, ArtifactNotFoundException {
        return getFeatureSource().feature(featureId, featureVersionRange);
    }

    @Override
    public EclipseFeatureOption feature(String featureId, Version featureVersion)
        throws IOException, ArtifactNotFoundException {
        return getFeatureSource().feature(featureId, featureVersion);
    }

    @Override
    public final EclipseBundleOption bundle(String bundleSymbolicName)
        throws IOException, ArtifactNotFoundException {
        return getBundleSource().bundle(bundleSymbolicName);
    }

    @Override
    public final EclipseBundleOption bundle(String bundleSymbolicName,
        VersionRange bundleVersionRange) throws IOException, ArtifactNotFoundException {
        return getBundleSource().bundle(bundleSymbolicName, bundleVersionRange);
    }

    @Override
    public final EclipseBundleOption bundle(String bundleSymbolicName, Version bundleVersion)
        throws IOException, ArtifactNotFoundException {
        return getBundleSource().bundle(bundleSymbolicName, bundleVersion);
    }

    protected abstract EclipseBundleSource getBundleSource();

    protected abstract EclipseFeatureSource getFeatureSource();

}
