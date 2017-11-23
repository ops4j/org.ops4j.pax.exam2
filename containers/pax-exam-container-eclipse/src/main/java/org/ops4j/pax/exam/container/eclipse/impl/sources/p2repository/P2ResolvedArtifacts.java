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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit.ResolvedArtifacts;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.ContextEclipseFeatureSource;

/**
 * A class that holds artifacts of a resolving process of a unit
 * 
 * @author Christoph Läubrich
 *
 */
public class P2ResolvedArtifacts extends BundleAndFeatureSource implements ResolvedArtifacts {

    private final ContextEclipseBundleSource bundles = new ContextEclipseBundleSource();
    private final ContextEclipseFeatureSource features = new ContextEclipseFeatureSource();

    @Override
    public ContextEclipseBundleSource getBundleSource() {
        return bundles;
    }

    @Override
    public ContextEclipseFeatureSource getFeatureSource() {
        return features;
    }

    @Override
    public Collection<EclipseBundleOption> getBundles() throws IOException {
        return Collections.unmodifiableCollection(bundles.getIncludedArtifacts());
    }

    @Override
    public Collection<EclipseFeatureOption> getFeatures() throws IOException {
        return Collections.unmodifiableCollection(features.getIncludedArtifacts());
    }

}
