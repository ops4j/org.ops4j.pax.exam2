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

import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseProjectSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.builder.EclipseInstallationBuilder;
import org.ops4j.pax.exam.container.eclipse.builder.EclipseProductBuilder;
import org.ops4j.pax.exam.container.eclipse.impl.CombinedSource;
import org.ops4j.pax.exam.container.eclipse.impl.DefaultEclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.impl.DefaultEclipseLauncher;
import org.ops4j.pax.exam.container.eclipse.impl.DefaultEclipseProvision;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProductParser;
import org.ops4j.pax.exam.container.eclipse.impl.sources.directory.DirectoryResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.feature.FeatureResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository.P2Resolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.target.TargetResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.workspace.WorkspaceResolver;

/**
 * Static Options to configure the EclipseContainer
 * 
 * @author Christoph Läubrich
 *
 */
public class EclipseOptions {

    private static final EclipseEnvironment SYSTEM_ENVIRONMENT = new DefaultEclipseEnvironment();

    public static EclipseEnvironment getSystemEnvironment() {
        return SYSTEM_ENVIRONMENT;
    }

    public static EclipseLauncher launcher(EclipseProvision provision) {
        return launcher(provision, false);
    }

    public static EclipseLauncher launcher(EclipseProvision provision, final boolean forked) {
        return new DefaultEclipseLauncher(forked, provision);
    }

    public static EclipseProductBuilder buildFromProduct(InputStream productFile)
        throws IOException {
        try {
            return new EclipseProductBuilder(new ProductParser(productFile));
        }
        finally {
            productFile.close();
        }
    }

    public static EclipseInstallationBuilder buildFromEclipseInstallation(
        EclipseInstallation installation) throws IOException {
        return new EclipseInstallationBuilder(installation);
    }

    /**
     * Uses a Installation-Folder to provision bundles from
     * 
     * @param folder
     * @return
     * @throws IOException
     */
    public static EclipseInstallation fromInstallation(final File baseFolder) throws IOException {
        return new DirectoryResolver(baseFolder);
    }

    /**
     * Use an Eclipse-Workspace to provision bundles from
     * 
     * @param workspaceFolder
     * @return
     * @throws IOException
     */
    public static EclipseWorkspace fromWorkspace(final File workspaceFolder) throws IOException {
        return new WorkspaceResolver(workspaceFolder);
    }

    public static EclipseTargetPlatform fromTarget(InputStream targetDefinition)
        throws IOException {
        return fromTarget(targetDefinition, null);
    }

    /**
     * Use an Eclipse Target file to provision bundles from, the cache folder can be used to specify
     * a location where the resolve result of the target is stored (e.g. if the target is based on
     * software-sites) the cache will be refreshed if the sequenceNumber changes but in no other
     * circumstances so it might be needed to clear the cache if something changes on the remote
     * server but the target stays unmodified.
     * 
     * @param targetDefinition
     * @param cacheFolder
     * @return
     * @throws IOException
     */
    public static EclipseTargetPlatform fromTarget(InputStream targetDefinition, File cacheFolder)
        throws IOException {
        return new TargetResolver(targetDefinition, cacheFolder);
    }

    public static EclipseRepository createRepository(URL url, String name) throws IOException {
        return new P2Resolver(name, url);
    }

    public static <Source extends EclipseBundleSource & EclipseFeatureSource> EclipseBundleSource fromFeatures(
        Source source, EclipseEnvironment environment, EclipseFeature... features)
        throws ArtifactNotFoundException, IOException {
        return fromFeatures(source, source, environment, features);
    }

    public static EclipseBundleSource fromFeatures(EclipseBundleSource bundleSource,
        EclipseFeatureSource featureSource, EclipseEnvironment environment,
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
        return new FeatureResolver(bundleSource, featureSource, bootFeatures, environment);
    }

    public static CombinedEclipseArtifactSource combine(final EclipseArtifactSource... sources) {
        return new CombinedSource(Arrays.asList(sources));

    }

    public static EclipseProvision provision(final EclipseArtifactSource source,
        String... ignoreItems) {
        EclipseEnvironment env;
        if (source instanceof EclipseTargetPlatform) {
            env = ((EclipseTargetPlatform) source).getEclipseEnvironment();
        }
        else {
            env = SYSTEM_ENVIRONMENT;
        }
        return createDefaultProvision(source, env, ignoreItems);
    }

    public static EclipseProvision provision(final EclipseArtifactSource source,
        EclipseEnvironment environment, String... ignoreItems) {
        return createDefaultProvision(source, environment, ignoreItems);
    }

    private static DefaultEclipseProvision createDefaultProvision(
        final EclipseArtifactSource source, EclipseEnvironment environment, String... ignoreItems) {
        final Set<String> ignored = new HashSet<>();
        if (ignoreItems != null) {
            ignored.addAll(Arrays.asList(ignoreItems));
        }
        // We provide this by default
        ignored.add("org.eclipse.osgi");
        return new DefaultEclipseProvision(source, environment, ignored);
    }

    public static interface CombinedEclipseArtifactSource
        extends EclipseFeatureSource, EclipseProjectSource, EclipseUnitSource, EclipseBundleSource {

    }

}
