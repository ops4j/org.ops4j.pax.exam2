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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.ArtifactNotFoundException;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProductParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProductParser.PluginConfiguration;
import org.ops4j.pax.exam.container.eclipse.impl.repository.RepositoryResolverEclipseBundleSource;
import org.ops4j.pax.exam.options.ProvisionControl;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * Default implementation of the {@link EclipseProvision} interface used in the eclipse options
 * 
 * @author Christoph Läubrich
 *
 */
public class DefaultEclipseProvision implements EclipseProvision, EclipseUnitSource {

    private final Set<String> ignoredBundles;
    private final List<Option> bundles = new ArrayList<>();
    private final EclipseArtifactSource source;

    public DefaultEclipseProvision(EclipseArtifactSource source, final Set<String> ignoredBundles) {
        this.source = source == null ? new CombinedSource(Collections.emptyList()) : source;
        this.ignoredBundles = ignoredBundles;
    }

    @Override
    public EclipseProvision simpleconfigurator(InputStream bundleFile) throws IOException {
        EclipseBundleSource bundleSource = getBundleSource();
        // We parse the file and add bundle(...) Options, later we might just provision the
        // simple configurator, but then we must find a way to fetch/install it ...
        List<EclipseBundleOption> local = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(bundleFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                String[] bundleInfo = line.split(",");
                String bsn = bundleInfo[0];
                Version version = version(bundleInfo[1]);
                if (isIgnored(bsn, version)) {
                    continue;
                }
                EclipseBundleOption bundle = bundleSource.bundle(bsn);
                if (bundle instanceof ProvisionControl<?>) {
                    ProvisionControl<?> control = (ProvisionControl<?>) bundle;
                    String sl = bundleInfo[3];
                    String start = bundleInfo[4];
                    control.startLevel(Integer.parseInt(sl));
                    control.start(Boolean.valueOf(start));
                }
                local.add(bundle);
            }
        }
        addAll(local);
        return this;
    }

    private EclipseBundleSource getBundleSource() {
        if (source instanceof EclipseBundleSource) {
            return (EclipseBundleSource) source;
        }
        else {
            throw new IllegalArgumentException(
                "An EclipseBundleSource is required to use this provision option");
        }
    }

    private EclipseUnitSource getUnitSource() {
        if (source instanceof EclipseUnitSource) {
            return (EclipseUnitSource) source;
        }
        else {
            throw new IllegalArgumentException(
                "An EclipseBundleSource is required to use this provision option");
        }
    }

    private EclipseFeatureSource getFeatureSource() {
        if (source instanceof EclipseBundleSource) {
            return (EclipseFeatureSource) source;
        }
        else {
            throw new IllegalArgumentException(
                "An EclipseBundleSource is required to use this provision option");
        }
    }

    @Override
    public EclipseProvision product(InputStream productDefinition) throws IOException {
        EclipseBundleSource bundleSource = getBundleSource();
        List<EclipseBundleOption> local = new ArrayList<>();
        ProductParser parser = new ProductParser(productDefinition);
        for (ArtifactInfo<PluginConfiguration> bundleInfo : parser.getPlugins()) {
            if (isIgnored(bundleInfo.getId(), bundleInfo.getVersion())) {
                continue;
            }
            EclipseBundleOption bundle = bundleSource.bundle(bundleInfo.getId());
            if (bundle instanceof ProvisionControl<?>) {
                ProvisionControl<?> control = (ProvisionControl<?>) bundle;
                PluginConfiguration context = bundleInfo.getContext();
                if (context == null) {
                    control.start(false);
                }
                else {
                    control.start(context.autoStart);
                    if (context.startLevel > 0) {
                        control.startLevel(context.startLevel);
                    }
                }
            }
            local.add(bundle);
        }
        addAll(local);
        return this;
    }

    @Override
    public EclipseProvision feature(EclipseFeature feature) throws IOException {
        EclipseFeatureOption eclipseFeatureOption;
        if (feature instanceof EclipseFeatureOption) {
            eclipseFeatureOption = (EclipseFeatureOption) feature;
        }
        else {
            eclipseFeatureOption = getFeatureSource().feature(feature.getId(),
                new VersionRange(VersionRange.LEFT_CLOSED, feature.getVersion(),
                    feature.getVersion(), VersionRange.RIGHT_CLOSED));
        }
        FeatureEclipseBundleSource featureSource = new FeatureEclipseBundleSource(getBundleSource(),
            Collections.singleton(eclipseFeatureOption));
        addAll(featureSource.getIncludedBundles());
        return this;
    }

    @Override
    public Option[] getOptions() {
        return bundles.toArray(new Option[0]);
    }

    private String getKey(EclipseVersionedArtifact bundle) {
        return bundle.getId() + ":" + bundle.getVersion();
    }

    private void addAll(Collection<EclipseBundleOption> list) {
        for (EclipseBundleOption bundle : list) {
            String key = getKey(bundle);
            if (ignoredBundles.add(key)) {
                bundles.add(bundle);
            }
        }
    }

    private Version version(String version) {
        return (version == null || version.isEmpty()) ? Version.emptyVersion
            : Version.parseVersion(version);
    }

    private boolean isIgnored(String bsn, Version version) {
        if (ignoredBundles.contains(bsn)) {
            return true;
        }
        StringBuilder sb = new StringBuilder(bsn);
        sb.append(':');
        sb.append(version.getMajor());
        if (ignoredBundles.contains(sb.toString())) {
            return true;
        }
        sb.append('.');
        sb.append(version.getMinor());
        if (ignoredBundles.contains(sb.toString())) {
            return true;
        }
        sb.append('.');
        sb.append(version.getMicro());
        if (ignoredBundles.contains(sb.toString())) {
            return true;
        }
        else {
            sb.append('.');
            sb.append(version.getQualifier());
            return ignoredBundles.contains(sb.toString());
        }
    }

    @Override
    public EclipseProvision units(IncludeMode mode, EclipseInstallableUnit... units)
        throws IOException {
        Set<EclipseUnitSource> repositories = new LinkedHashSet<>();
        for (EclipseInstallableUnit unit : units) {
            repositories.add(unit.getSource());
        }
        repositories.add(this);
        RepositoryResolverEclipseBundleSource resolver = new RepositoryResolverEclipseBundleSource(
            repositories, mode, Arrays.asList(units));
        addAll(resolver.getIncludedBundles());
        return this;
    }

    @Override
    public EclipseFeatureOption feature(String featureId, VersionRange featureVersionRange)
        throws IOException, ArtifactNotFoundException {
        if (source instanceof EclipseFeatureSource) {
            return getFeatureSource().feature(featureId, featureVersionRange);
        }
        throw new ArtifactNotFoundException("feature", featureId, featureVersionRange);
    }

    @Override
    public EclipseFeatureOption feature(String featureId)
        throws IOException, ArtifactNotFoundException {
        if (source instanceof EclipseFeatureSource) {
            return getFeatureSource().feature(featureId);
        }
        throw new ArtifactNotFoundException("feature", featureId, HIGHEST_VERSION);
    }

    @Override
    public EclipseBundleOption bundle(String bundleSymbolicName, VersionRange bundleVersionRange)
        throws IOException, ArtifactNotFoundException {
        if (source instanceof EclipseBundleSource) {
            return getBundleSource().bundle(bundleSymbolicName, bundleVersionRange);
        }
        throw new ArtifactNotFoundException("bunde", bundleSymbolicName, bundleVersionRange);
    }

    @Override
    public EclipseBundleOption bundle(String bundleSymbolicName)
        throws IOException, ArtifactNotFoundException {
        if (source instanceof EclipseBundleSource) {
            return getBundleSource().bundle(bundleSymbolicName);
        }
        throw new ArtifactNotFoundException("bunde", bundleSymbolicName, HIGHEST_VERSION);
    }

    @Override
    public EclipseInstallableUnit unit(String id, VersionRange versionRange)
        throws IOException, ArtifactNotFoundException {
        if (source instanceof EclipseUnitSource) {
            return getUnitSource().unit(id, versionRange);
        }
        throw new ArtifactNotFoundException("unit", id, versionRange);
    }

    @Override
    public EclipseInstallableUnit unit(String id) throws IOException, ArtifactNotFoundException {
        if (source instanceof EclipseUnitSource) {
            return ((EclipseUnitSource) source).unit(id);
        }
        return unit(id, HIGHEST_VERSION);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public Collection<EclipseInstallableUnit> getAllUnits() throws IOException {
        if (source instanceof EclipseUnitSource) {
            return ((EclipseUnitSource) source).getAllUnits();
        }
        return Collections.emptyList();
    }

}
