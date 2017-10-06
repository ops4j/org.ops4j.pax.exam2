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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseBundleSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseFeatureSource;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource.EclipseUnitSource;
import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision;
import org.ops4j.pax.exam.container.eclipse.EclipseVersionedArtifact;
import org.ops4j.pax.exam.container.eclipse.impl.sources.feature.FeatureResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.unit.UnitResolver;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

/**
 * Default implementation of the {@link EclipseProvision} interface used in the eclipse options
 * 
 * @author Christoph Läubrich
 *
 */
public class DefaultEclipseProvision implements EclipseProvision {

    private final Set<String> ignoredBundles;
    private final List<EclipseBundleOption> bundles = new ArrayList<>();
    private final Map<String, EclipseBundleOption> singletonBundles = new HashMap<>();
    private final EclipseArtifactSource source;
    private final EclipseEnvironment environment;
    private SingletonConflictResolution conflictResolution = SingletonConflictResolution.FAIL;

    public DefaultEclipseProvision(EclipseArtifactSource source, EclipseEnvironment environment,
        final Set<String> ignoredBundles) {
        this.environment = environment;
        this.source = source == null ? new CombinedSource(Collections.emptyList()) : source;
        this.ignoredBundles = ignoredBundles;
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
                "An EclipseUnitSource is required to use this provision option");
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
    public List<EclipseBundleOption> feature(EclipseFeature feature) throws IOException {
        EclipseFeatureOption eclipseFeatureOption;
        if (feature instanceof EclipseFeatureOption) {
            eclipseFeatureOption = (EclipseFeatureOption) feature;
        }
        else {
            eclipseFeatureOption = getFeatureSource().feature(feature.getId(), new VersionRange(
                VersionRange.LEFT_CLOSED, feature.getVersion(), null, VersionRange.RIGHT_CLOSED));
        }
        FeatureResolver featureSource = new FeatureResolver(getBundleSource(), getFeatureSource(),
            Collections.singleton(eclipseFeatureOption), environment);
        return addAll(featureSource.getBundleSource().getIncludedArtifacts());
    }

    @Override
    public Option asOption() {
        ArrayList<Option> list = new ArrayList<>();
        list.addAll(bundles);
        list.addAll(singletonBundles.values());
        return CoreOptions.composite(list);
    }

    private String getKey(EclipseVersionedArtifact bundle) {
        return bundle.getId() + ":" + bundle.getVersion();
    }

    @Override
    public List<EclipseBundleOption> bundle(EclipseBundle bundle) throws IOException {
        if (bundle instanceof EclipseBundleOption) {
            return addAll(Collections.singleton((EclipseBundleOption) bundle));
        }
        else {
            return addAll(Collections
                .singleton(getBundleSource().bundle(bundle.getId(), bundle.getVersion())));
        }
    }

    private List<EclipseBundleOption> addAll(Collection<EclipseBundleOption> list) {
        ArrayList<EclipseBundleOption> added = new ArrayList<>();
        for (EclipseBundleOption bundle : list) {
            if (isIgnored(bundle.getId(), bundle.getVersion())) {
                continue;
            }
            String key = getKey(bundle);
            if (bundle.isSingleton()) {
                String stkey = bundle.getId();
                if (singletonBundles.containsKey(stkey)) {
                    switch (conflictResolution) {
                        case FAIL:
                            throw new IllegalStateException(
                                "conflicting singleton bundle for " + stkey + " found");
                        case KEEP_CURRENT:
                            continue;
                        case REPLACE_CURRENT:
                            break;
                        case USE_HIGHEST_VERSION: {
                            EclipseBundleOption existing = singletonBundles.get(stkey);
                            if (existing.getVersion().compareTo(bundle.getVersion()) > 0) {
                                continue;
                            }
                            else {
                                break;
                            }
                        }
                    }
                }
                singletonBundles.put(stkey, bundle);
                added.add(bundle);
            }
            else {
                bundles.add(bundle);
                added.add(bundle);
            }
            ignoredBundles.add(key);
        }
        return added;
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
    public List<EclipseBundleOption> units(IncludeMode mode, EclipseUnitSource unitSource,
        EclipseInstallableUnit... units) throws IOException {
        Set<EclipseUnitSource> repositories = new LinkedHashSet<>();
        for (EclipseInstallableUnit unit : units) {
            repositories.add(unit.getSource());
        }
        if (unitSource != null) {
            repositories.add(unitSource);
        }
        UnitResolver resolver = new UnitResolver(repositories, mode, Arrays.asList(units), false,
            environment);
        return addAll(resolver.getBundleSource().getIncludedArtifacts());
    }

    @Override
    public List<EclipseBundleOption> units(IncludeMode mode, EclipseInstallableUnit... units)
        throws IOException {
        if (source instanceof EclipseUnitSource) {
            return units(mode, getUnitSource(), units);
        }
        return units(mode, null, units);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public EclipseProvision singletonConflictResolution(
        SingletonConflictResolution singletonConflictResolution) {
        conflictResolution = singletonConflictResolution;
        return this;
    }

}
