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
package org.ops4j.pax.exam.container.eclipse.impl.parser;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * parses the product file of an EclipseProject and the corresponding files, this class throws no
 * error in case of failure, so you should check the {@link #isValid()} method before use it. Even
 * though a project file itself might be invalid you can still use things like classpath,
 * build.properties or the project folder
 * 
 * @author Christoph Läubrich
 *
 */
public class ProjectParser extends AbstractParser {

    private static final String PROJECT_FILE = ".project";

    private static final Logger LOG = LoggerFactory.getLogger(ProjectParser.class);

    public static final String PLUGIN_NATURE = "org.eclipse.pde.PluginNature";
    public static final String JAVA_NATURE = "org.eclipse.jdt.core.javanature";
    public static final String MAVEN2_NATURE = "org.eclipse.m2e.core.maven2Nature";
    public static final String FEATURE_NATURE = "org.eclipse.pde.FeatureNature";

    private boolean valid;
    private String projectName;
    private final Set<String> natures = new LinkedHashSet<>();
    private final File projectFolder;
    private final ClasspathParser classpath;

    private final Properties buildProperties = new Properties();

    public ProjectParser(File projectFolder) {
        this.projectFolder = projectFolder;
        this.classpath = new ClasspathParser(projectFolder);
        File file = new File(projectFolder, PROJECT_FILE);
        if (file.exists()) {
            try {
                Element document = parse(new FileInputStream(file));
                XPath xPath = getXPath();
                projectName = (String) xPath.evaluate("/projectDescription/name[text()]", document,
                    XPathConstants.STRING);
                for (Node node : evaluate(document, "/projectDescription/natures/nature")) {
                    natures.add(node.getTextContent());
                }
                valid = projectName != null;
            }
            catch (Exception e) {
                LOG.warn("can't parse {}", file.getAbsolutePath(), e);
            }
        }
        File buildPropertiesFile = new File(projectFolder, "build.properties");
        if (buildPropertiesFile.exists()) {
            try {
                try (FileInputStream stream = new FileInputStream(buildPropertiesFile)) {
                    buildProperties.load(stream);
                }
            }
            catch (Exception e) {
                LOG.warn("can't parse {}", buildPropertiesFile.getAbsolutePath(), e);
            }
        }
    }

    public static boolean isProjectFolder(File folder) {
        File file = new File(folder, PROJECT_FILE);
        return file.exists();
    }

    public boolean hasNature(String name) {
        return natures.contains(name);
    }

    public String getName() {
        return projectName;
    }

    public boolean isValid() {
        return valid;
    }

    public File getProjectFolder() {
        return projectFolder;
    }

    public File getOutputFolder() {
        if (classpath.isValid()) {
            String output = classpath.getOutput();
            if (output != null) {
                return new File(getProjectFolder(), output);
            }
        }

        return null;
    }

    public Properties getBuildProperties() {
        return buildProperties;
    }

    @Override
    public String toString() {
        return getProjectFolder().getAbsolutePath();
    }

}
