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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.eclipse.core.internal.localstore.SafeChunkyInputStream;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.container.eclipse.EclipseBundle;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleOption;
import org.ops4j.pax.exam.container.eclipse.EclipseBundleSource.EclipseProjectSource;
import org.ops4j.pax.exam.container.eclipse.EclipseFeature;
import org.ops4j.pax.exam.container.eclipse.EclipseFeatureOption;
import org.ops4j.pax.exam.container.eclipse.EclipseProject;
import org.ops4j.pax.exam.container.eclipse.impl.parser.FeatureParser;
import org.ops4j.pax.exam.container.eclipse.impl.parser.ProjectParser;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scanns a Workspace folder and the conaining project to make them useable for pax-exam tests
 * 
 * @author Christoph Läubrich
 *
 */
public class WorkspaceEclipseBundleSource implements EclipseProjectSource {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceEclipseBundleSource.class);

    private static final String PREFIX_URI = "URI//";

    private final BundleInfoMap<ProjectParser> bundleMap = new BundleInfoMap<>();
    private final BundleInfoMap<FeatureProject> featureMap = new BundleInfoMap<>();

    private final Map<String, ProjectParser> projectMap = new HashMap<>();

    private File workspaceFolder;

    public WorkspaceEclipseBundleSource(File workspaceFolder) throws IOException {
        this.workspaceFolder = workspaceFolder;
        List<File> projectLocations = new ArrayList<>();

        File projectsFolder = new File(workspaceFolder,
            ".metadata/.plugins/org.eclipse.core.resources/.projects");
        if (projectsFolder.exists()) {
            readProjectsFromEclipseWorkspace(workspaceFolder, projectLocations, projectsFolder);
        }
        else if (workspaceFolder.exists()) {
            readProjectsFromFolder(workspaceFolder);
        }
        else {
            throw new FileNotFoundException(
                "Folder " + workspaceFolder.getAbsolutePath() + " does not exists");
        }
        for (File projectFolder : projectLocations) {
            ProjectParser project = new ProjectParser(projectFolder);
            if (project.isValid()) {
                projectMap.put(project.getName(), project);
                if (project.hasNature(ProjectParser.JAVA_NATURE)) {
                    if (project.hasNature(ProjectParser.PLUGIN_NATURE)) {
                        // the project must have a META-INF at the root folder like an exploded
                        // bundle...
                        try {
                            BundleInfo<ProjectParser> bundle = BundleInfo
                                .readExplodedBundle(projectFolder, project);
                            LOG.info("Add bundle {} ...", bundle);
                            bundleMap.add(bundle);
                        }
                        catch (Exception e) {
                            LOG.warn("can't read plugin-project {} ({})", project, e.toString());
                        }
                    }
                    else if (project.hasNature(ProjectParser.MAVEN2_NATURE)) {
                        // TODO
                    }
                    else {
                        LOG.info("Skipping java-project {} without plugin/maven2 nature...",
                            project.getName());
                    }
                }
                else if (project.hasNature(ProjectParser.FEATURE_NATURE)) {
                    try {
                        FeatureParser feature = new FeatureParser(projectFolder);
                        FeatureProject featureProject = new FeatureProject(project, feature);
                        BundleInfo<FeatureProject> featureInfo = new BundleInfo<>(feature.getId(),
                            feature.getVersion(), featureProject);
                        LOG.info("Add feature {} ...", featureInfo);
                        featureMap.add(featureInfo);
                    }
                    catch (Exception e) {
                        LOG.warn("can't read feature-project {} ({})", project, e.toString());
                    }
                }
                else {
                    LOG.info("Skipping project {} without supported project-natures...",
                        project.getName());
                }
            }
            else {
                LOG.info("Skipping location {} without valid project...", projectFolder);
            }
        }
    }

    private void readProjectsFromFolder(File workspaceFolder) {
        throw new UnsupportedOperationException("not implemented yet :-(");

    }

    private void readProjectsFromEclipseWorkspace(File workspaceFolder, List<File> projectLocations,
        File projectsFolder) throws IOException {
        for (File projectFolder : projectsFolder.listFiles()) {
            File location = new File(projectFolder, ".location");
            if (location.exists()) {
                // A project that is not located in the workspace...
                try (
                    DataInputStream in = new DataInputStream(new SafeChunkyInputStream(location))) {
                    String path = in.readUTF();
                    if (path.startsWith(PREFIX_URI)) {
                        try {
                            projectLocations
                                .add(new File(new URI(path.substring(PREFIX_URI.length()))));
                        }
                        catch (URISyntaxException e) {
                            throw new IOException("can't parse project location!", e);
                        }
                    }
                }
            }
            else {
                projectLocations.add(new File(workspaceFolder, projectFolder.getName()));
            }
        }
    }

    @Override
    public EclipseBundleOption bundle(String bundleName, Version bundleVersion)
        throws IOException, FileNotFoundException {
        BundleInfo<ProjectParser> bundleInfo = bundleMap.get(bundleName, bundleVersion);
        if (bundleInfo == null || !bundleInfo.getContext().isValid()) {
            throw new BundleNotFoundException("can't find bundle " + bundleName + ":"
                + bundleVersion + " in workspace " + workspaceFolder.getAbsolutePath());
        }
        return new AbstractEclipseBundleOption<ProjectParser>(bundleInfo) {

            @Override
            protected Option toOption(BundleInfo<ProjectParser> bundleInfo) {
                try {
                    return projectToOption(bundleInfo.getContext());
                }
                catch (IOException e) {
                    throw new TestContainerException("option creation failed", e);
                }
            }
        };
    }

    @Override
    public EclipseFeatureOption feature(String featureName, Version featureVersion)
        throws IOException, BundleNotFoundException {
        BundleInfo<FeatureProject> bundleInfo = featureMap.get(featureName, featureVersion);
        if (bundleInfo == null) {
            throw new BundleNotFoundException("can't find feature " + featureName + ":"
                + featureVersion + " in workspace " + workspaceFolder.getAbsolutePath());
        }
        return new AbstractEclipseFeatureOption<FeatureProject>(bundleInfo) {

            @Override
            protected List<? extends EclipseFeature> getIncluded(
                BundleInfo<FeatureProject> bundleInfo) {
                return bundleInfo.getContext().feature.getIncluded();
            }

            @Override
            protected List<? extends EclipseBundle> getBundles(
                BundleInfo<FeatureProject> bundleInfo) {
                return bundleInfo.getContext().feature.getPlugins();
            }

            @Override
            protected boolean isOptional(BundleInfo<FeatureProject> bundleInfo) {
                return false;
            }

            @Override
            protected Option toOption(BundleInfo<FeatureProject> bundleInfo) {
                try {
                    return projectToOption(bundleInfo.getContext().project);
                }
                catch (IOException e) {
                    throw new TestContainerException("creation of option failed", e);
                }
            }

        };
    }

    @Override
    public EclipseProject project(String projectName) throws BundleNotFoundException {
        ProjectParser context = projectMap.get(projectName);
        if (context == null || !context.isValid()) {
            throw new BundleNotFoundException("can't find project " + projectName + " in workspace "
                + workspaceFolder.getAbsolutePath());
        }
        return new EclipseProject() {

            @Override
            public Option toOption() throws IOException {
                return projectToOption(context);
            }

            @Override
            public InputStream getResource(String name) throws FileNotFoundException {
                return new ProjectFileInputStream(name, context, workspaceFolder);
            }
        };
    }

    private static Option projectToOption(ProjectParser context) throws IOException {
        ByteArrayOutputStream projectStream = new ByteArrayOutputStream();
        Manifest manifest = null;
        if (context.hasNature(ProjectParser.JAVA_NATURE)
            && BundleInfo.isBundle(context.getProjectFolder())) {
            manifest = BundleInfo.readManifest(context.getProjectFolder());
            try (JarOutputStream jar = new JarOutputStream(projectStream, manifest)) {
                writeProjectTo(context, jar);
            }
        }
        else {
            try (JarOutputStream jar = new JarOutputStream(projectStream)) {
                writeProjectTo(context, jar);
            }
        }
        UrlProvisionOption bundle = CoreOptions
            .streamBundle(new ByteArrayInputStream(projectStream.toByteArray()));
        if (BundleInfo.isBundle(manifest) || context.hasNature(ProjectParser.FEATURE_NATURE)) {
            return bundle;
        }
        else {
            return CoreOptions.wrappedBundle(bundle);
        }
    }

    private static void writeProjectTo(ProjectParser project, JarOutputStream jar)
        throws IOException {
        Set<String> copied = new HashSet<>();
        copied.add(BundleInfo.MANIFEST_LOCATION);
        copyFolder(project.getOutputFolder(), "", jar, copied);
        if (project.hasNature(ProjectParser.PLUGIN_NATURE)
            || project.hasNature(ProjectParser.FEATURE_NATURE)) {
            addBuildProperties(project, jar, copied);
        }
    }

    private static void addBuildProperties(ProjectParser project, JarOutputStream jar,
        Set<String> copied) throws IOException {
        Properties properties = project.getBuildProperties();
        String includes = properties.getProperty("bin.includes");
        if (includes != null) {
            String[] split = includes.split(",");
            for (String include : split) {
                File includeFile = new File(project.getProjectFolder(), include);
                if (includeFile.isDirectory()) {
                    copyFolder(includeFile, includeFile.getName() + "/", jar, copied);
                }
                else if (includeFile.isFile()) {
                    copyFile(includeFile, include, jar, copied);
                }
            }
        }
    }

    private static void copyFolder(File folder, String path, JarOutputStream jar,
        Set<String> copied) throws IOException {
        if (folder == null || !folder.exists()) {
            return;
        }
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                copyFolder(file, path + file.getName() + "/", jar, copied);
            }
            else {
                String name = path + file.getName();
                copyFile(file, name, jar, copied);
            }
        }

    }

    private static void copyFile(File file, String name, JarOutputStream jar, Set<String> copied)
        throws IOException, FileNotFoundException {
        if (copied.contains(name)) {
            return;
        }
        copied.add(name);
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(file.lastModified());
        entry.setSize(file.length());
        jar.putNextEntry(entry);
        try (FileInputStream input = new FileInputStream(file)) {
            IOUtils.copy(input, jar);
        }
        jar.closeEntry();
    }

    private static final class FeatureProject {

        private final ProjectParser project;
        private final FeatureParser feature;

        public FeatureProject(ProjectParser project, FeatureParser feature) {
            this.project = project;
            this.feature = feature;
        }

        @Override
        public String toString() {
            return project.toString();
        }

    }

}
