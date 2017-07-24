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
package org.ops4j.pax.exam.container.eclipse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseProjectSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.CombinedSource;
import org.ops4j.pax.exam.container.eclipse.impl.DefaultEclipseProvision;
import org.ops4j.pax.exam.container.eclipse.impl.DirectoryEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.EclipseApplicationImpl;
import org.ops4j.pax.exam.container.eclipse.impl.FeatureEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.TargetEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.WorkspaceEclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.impl.repository.P2EclipseRepositorySource;

/**
 * Static Options to configure the EclipseContainer
 * 
 * @author Christoph Läubrich
 *
 */
public class EclipseOptions {

    private static final class EclipseLauncherImpl implements EclipseLauncher {

        private final boolean forked;

        private EclipseLauncherImpl(boolean forked) {
            this.forked = forked;
        }

        @Override
        public boolean isForked() {
            return forked;
        }

        @Override
        public EclipseApplication ignoreApp() {
            return new EclipseApplicationImpl(this, true,
                CoreOptions.frameworkProperty("eclipse.ignoreApp").value(true));
        }

        @Override
        public EclipseApplication application(String applicationID) {
            return new EclipseApplicationImpl(this, false,
                CoreOptions.frameworkProperty("eclipse.application").value(applicationID));
        }

        @Override
        public EclipseProduct product(InputStream productFile) {
            throw new UnsupportedOperationException("not implmented yet, sorry :-(");
        }

        @Override
        public EclipseProduct product(final String productID) {
            return new EclipseProduct() {

                @Override
                public EclipseApplication application(String applicationID) {
                    return new EclipseApplicationImpl(EclipseLauncherImpl.this, false,
                        CoreOptions.frameworkProperty("eclipse.application").value(applicationID),
                        CoreOptions.frameworkProperty("eclipse.product").value(productID));
                }
            };
        }

    }

    public static EclipseLauncher launcher(final boolean forked) {
        return new EclipseLauncherImpl(forked);
    }

    /**
     * Uses a Installation-Folder to provision bundles from
     * 
     * @param folder
     * @return
     * @throws IOException
     */
    public static EclipseFeatureSource fromInstallation(final File baseFolder) throws IOException {
        return DirectoryEclipseBundleSource.create(baseFolder);
    }

    /**
     * Use an Eclipse-WOrkspace to provision bundles from
     * 
     * @param workspaceFolder
     * @return
     * @throws IOException
     */
    public static EclipseProjectSource fromWorkspace(final File workspaceFolder)
        throws IOException {
        return new WorkspaceEclipseBundleSource(workspaceFolder);
    }

    public static EclipseFeatureSource fromTarget(InputStream targetDefinition) throws IOException {
        return new TargetEclipseBundleSource(targetDefinition);
    }

    public static EclipseUnitSource createRepository(URL url, String name) throws IOException {
        return new P2EclipseRepositorySource(url, name);
    }

    public static EclipseBundleSource fromFeatures(EclipseFeatureSource featureSource,
        EclipseFeature... features) throws ArtifactNotFoundException, IOException {
        List<EclipseFeatureOption> bootFeatures = new ArrayList<>();
        for (EclipseFeature feature : features) {
            if (feature instanceof EclipseFeatureOption) {
                bootFeatures.add((EclipseFeatureOption) feature);
            }
            else {
                bootFeatures.add(featureSource.feature(feature.getId()));
            }
        }
        return new FeatureEclipseBundleSource(featureSource, bootFeatures);
    }

    public static CombinedEclipseArtifactSource combine(final EclipseArtifactSource... sources) {
        return new CombinedSource(Arrays.asList(sources));

    }

    public static EclipseProvision provision(final EclipseArtifactSource source,
        String... ignoreItems) {
        final Set<String> ignored = new HashSet<>();
        if (ignoreItems != null) {
            ignored.addAll(Arrays.asList(ignoreItems));
        }
        // We provide this by default
        ignored.add("org.eclipse.osgi");
        // We do the job of the configurator
        ignored.add("org.eclipse.equinox.simpleconfigurator");
        return new DefaultEclipseProvision(source, ignored);
    }

    public static interface CombinedEclipseArtifactSource
        extends EclipseFeatureSource, EclipseProjectSource, EclipseUnitSource {

    }

}
