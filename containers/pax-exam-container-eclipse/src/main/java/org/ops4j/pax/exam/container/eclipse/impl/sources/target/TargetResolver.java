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
package org.ops4j.pax.exam.container.eclipse.impl.sources.target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.ops4j.pax.exam.container.eclipse.EclipseArtifactSource;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.EclipseEnvironment.ModifiableEclipseEnvironment;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseInstallableUnit;
import org.ops4j.pax.exam.container.eclipse.EclipseOptions;
import org.ops4j.pax.exam.container.eclipse.EclipseProvision.IncludeMode;
import org.ops4j.pax.exam.container.eclipse.EclipseTargetPlatform;
import org.ops4j.pax.exam.container.eclipse.impl.ArtifactInfo;
import org.ops4j.pax.exam.container.eclipse.impl.CombinedSource;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProjectParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.DirectoryTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.FeatureTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.InstallableUnitTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.PathTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.ProfileTargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.parser.TargetPlatformParser.TargetPlatformLocation;
import org.ops4j.pax.exam.container.eclipse.impl.sources.BundleAndFeatureAndUnitSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.CacheableSource;
import org.ops4j.pax.exam.container.eclipse.impl.sources.directory.DirectoryResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.feature.FeatureResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.p2repository.P2Resolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.unit.UnitResolver;
import org.ops4j.pax.exam.container.eclipse.impl.sources.workspace.ProjectFileInputStream;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A source of bundles based on an eclipse target
 * 
 * @author Christoph Läubrich
 *
 */
public class TargetResolver extends BundleAndFeatureAndUnitSource implements EclipseTargetPlatform {

    private static final String CACHE_METADATA_NAME = "cache.metadata";
    private static final String CACHE_FOLDER_NAME = "target";
    public static final Logger LOG = LoggerFactory.getLogger(TargetResolver.class);
    private final CombinedSource combinedSource;
    private final ModifiableEclipseEnvironment eclipseEnvironment;
    private static final Pattern SYSTEM_PROPERTY_PATTERN = Pattern
        .compile("\\$\\{system_property:([^}]+)\\}", Pattern.CASE_INSENSITIVE);

    public TargetResolver(InputStream targetDefinition, File cacheFolder) throws IOException {
        eclipseEnvironment = EclipseOptions.getSystemEnvironment().copy();
        List<EclipseArtifactSource> bundleSources = new ArrayList<>();
        TargetPlatformParser target = new TargetPlatformParser(targetDefinition);
        if (isSet(target.getArch())) {
            eclipseEnvironment.set(EclipseStarter.PROP_ARCH, target.getArch());
        }
        if (isSet(target.getNl())) {
            eclipseEnvironment.set(EclipseStarter.PROP_NL, target.getNl());
        }
        if (isSet(target.getOs())) {
            eclipseEnvironment.set(EclipseStarter.PROP_OS, target.getOs());
        }
        if (isSet(target.getWs())) {
            eclipseEnvironment.set(EclipseStarter.PROP_WS, target.getWs());
        }
        Map<String, DirectoryResolver> directoryResolverCache = new HashMap<>();
        if (isValid(cacheFolder, target.getSequenceNumber())) {
            LOG.info("Reading cached state from folder {}...", cacheFolder);
            combinedSource = CacheableSource.load(new File(cacheFolder, CACHE_FOLDER_NAME));
            LOG.info("done.");
        }
        else {
            List<TargetPlatformLocation> locations = target.getLocations();
            Map<String, P2Resolver> repositories = new LinkedHashMap<>();
            List<EclipseInstallableUnit> installunits = new ArrayList<>();
            int cnt = 0;
            for (TargetPlatformLocation location : locations) {
                cnt++;
                if (location instanceof DirectoryTargetPlatformLocation
                    || location instanceof ProfileTargetPlatformLocation) {
                    File folder = resolveFolder((PathTargetPlatformLocation) location,
                        targetDefinition);
                    DirectoryResolver resolver = getResolver(folder, directoryResolverCache);
                    bundleSources.add(resolver);
                }
                else if (location instanceof FeatureTargetPlatformLocation) {
                    FeatureTargetPlatformLocation featureLocation = (FeatureTargetPlatformLocation) location;
                    File folder = resolveFolder((PathTargetPlatformLocation) location,
                        targetDefinition);
                    DirectoryResolver source = getResolver(folder, directoryResolverCache);
                    EclipseFeatureOption feature = source.feature(featureLocation.id,
                        featureLocation.version);
                    FeatureResolver featureResolver = new FeatureResolver(source, source,
                        Collections.singleton(feature), eclipseEnvironment);
                    bundleSources.add(featureResolver);
                }
                else if (location instanceof InstallableUnitTargetPlatformLocation) {
                    InstallableUnitTargetPlatformLocation iuLocation = (InstallableUnitTargetPlatformLocation) location;
                    P2Resolver repository;
                    try {
                        URL url = new URL(iuLocation.repository);
                        repository = repositories.get(url.toExternalForm());
                        if (repository == null) {
                            repository = new P2Resolver("target-platform-" + cnt, url);
                            repositories.put(url.toExternalForm(), repository);
                        }
                    }
                    catch (MalformedURLException e) {
                        throw new IOException("can't create location " + iuLocation.repository, e);
                    }
                    List<EclipseInstallableUnit> local = new ArrayList<>();
                    for (ArtifactInfo<?> unit : iuLocation.units) {
                        Version version = unit.getVersion();
                        EclipseInstallableUnit iu = repository.unit(unit.getId(), version);
                        local.add(iu);
                    }
                    if (iuLocation.mode == IncludeMode.SLICER) {
                        LOG.info("Resolve {} units with slicer mode...", local.size());
                        UnitResolver source = new UnitResolver(Collections.singleton(repository),
                            IncludeMode.SLICER, local, true, eclipseEnvironment);
                        bundleSources.add(source);
                    }
                    else {
                        installunits.addAll(local);
                    }
                }
                else {
                    LOG.warn("location of type {} is currently not supported!", location.type);
                }
            }
            if (!installunits.isEmpty()) {
                LOG.info("Resolve {} units with planner mode...", installunits.size());
                // now resolve the big thing then...
                UnitResolver source = new UnitResolver(repositories.values(), IncludeMode.PLANNER,
                    installunits, true, eclipseEnvironment);
                bundleSources.add(source);
            }
            combinedSource = new CombinedSource(bundleSources);
        }
        if (cacheFolder != null) {
            if (cacheFolder.exists() || cacheFolder.mkdirs()) {
                CacheableSource.store(combinedSource, new File(cacheFolder, CACHE_FOLDER_NAME));
                Properties properties = new Properties();
                properties.setProperty("sequenceNumber", target.getSequenceNumber());
                try (FileOutputStream fout = new FileOutputStream(
                    new File(cacheFolder, CACHE_METADATA_NAME))) {
                    properties.store(fout, null);
                }
            }
        }
    }

    private static boolean isValid(File cacheFolder, String sequenceNumber) throws IOException {
        if (cacheFolder == null) {
            return false;
        }
        if (!cacheFolder.exists() && !cacheFolder.mkdirs()) {
            return false;
        }
        if (!cacheFolder.isDirectory()) {
            return false;
        }
        File metaDataFile = new File(cacheFolder, CACHE_METADATA_NAME);
        if (metaDataFile.exists()) {
            Properties properties = new Properties();
            try (FileInputStream stream = new FileInputStream(metaDataFile)) {
                properties.load(stream);
            }
            if (properties.getProperty("sequenceNumber", "").equals(sequenceNumber)) {
                // TODO also check ENV against cache!?
                return true;
            }
            // clear metadata then...
            metaDataFile.delete();
        }
        // delete cache...
        FileUtils.deleteDirectory(new File(cacheFolder, CACHE_FOLDER_NAME));
        return false;
    }

    private static DirectoryResolver getResolver(File folder, Map<String, DirectoryResolver> cache)
        throws IOException {
        String key = folder.getCanonicalPath();
        DirectoryResolver resolver = cache.get(key);
        if (resolver == null) {
            resolver = new DirectoryResolver(folder);
            cache.put(key, resolver);
        }
        return resolver;
    }

    private static File resolveFolder(PathTargetPlatformLocation locations, InputStream stream)
        throws IOException {
        String path = locations.path;
        if (stream instanceof ProjectFileInputStream) {
            ProjectFileInputStream prjStream = (ProjectFileInputStream) stream;
            ProjectParser project = prjStream.getProject();
            String projectFolder = project.getProjectFolder().getCanonicalPath();
            path = path.replace("${project_loc}", projectFolder);
            path = path.replace("${project_name}", project.getName());
            path = path.replace("${project_path}", projectFolder
                .substring(prjStream.getWorkspaceFolder().getCanonicalPath().length()));
        }
        Matcher matcher = SYSTEM_PROPERTY_PATTERN.matcher(path);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(sb, System.getProperty(group, ""));
        }
        matcher.appendTail(sb);
        path = sb.toString();
        return new File(path);
    }

    @Override
    protected EclipseBundleSource getBundleSource() {
        return combinedSource;
    }

    @Override
    protected EclipseFeatureSource getFeatureSource() {
        return combinedSource;
    }

    @Override
    protected EclipseUnitSource getUnitSource() {
        return combinedSource;
    }

    @Override
    public EclipseEnvironment getEclipseEnvironment() {
        return eclipseEnvironment;
    }

    private static boolean isSet(String attr) {
        return attr != null && !attr.trim().isEmpty();
    }

}
