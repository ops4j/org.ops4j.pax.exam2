/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.karaf.container.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.UUID;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;
import org.ops4j.pax.exam.karaf.options.SystemBundleOption;
import org.ops4j.pax.exam.options.*;

/**
 * Deploys exam and the user specified dependencies and creates the 
 * KarafFeatureOption for the exam feature.
 */
public class DependenciesDeployer {
    
    private static final String KARAF_FEATURE_NS = "http://karaf.apache.org/xmlns/features/v1.0.0";
    private ExamSystem subsystem;
    private File karafBase;
    private File karafHome;

    public DependenciesDeployer(ExamSystem subsystem, File karafBase, File karafHome) {
        this.subsystem = subsystem;
        this.karafBase = karafBase;
        this.karafHome = karafHome;
    }

    /**
     * Copy jars specified as BootClasspathLibraryOption in system
     * to the karaf lib path to make them available in the boot classpath
     * 
     * @throws IOException if copy fails
     */
    public void copyBootClasspathLibraries() throws IOException {
        BootClasspathLibraryOption[] bootClasspathLibraryOptions = subsystem
            .getOptions(BootClasspathLibraryOption.class);
        for (BootClasspathLibraryOption bootClasspathLibraryOption : bootClasspathLibraryOptions) {
            UrlReference libraryUrl = bootClasspathLibraryOption.getLibraryUrl();
            FileUtils.copyURLToFile(
                new URL(libraryUrl.getURL()),
                createUnique(libraryUrl.getURL(), new File(
                    karafHome + "/lib"), new String[] { "jar" }));
        }
    }

    /**
     *
     * Copies SystemBundleOption entries into the Karaf system folder using the Maven repository folder convention.
     *
     * @throws IOException if copy fails
     */
    public void copySystemLibraries() throws IOException {
        SystemBundleOption[] systemBundleOptions = subsystem
                .getOptions(SystemBundleOption.class);
        for (SystemBundleOption systemBundleOption : systemBundleOptions) {
            UrlReference libraryUrl = systemBundleOption.getLibraryUrl();
            String destPath = karafHome + "/system" + systemBundleOption.getRepositoryPath();
            FileUtils.copyURLToFile(
                    new URL(libraryUrl.getURL()),
                    new File(destPath));
        }
    }

    /**
     * Copy dependencies specified as ProvisionOption in system to the deploy folder
     */
    public void copyReferencedArtifactsToDeployFolder() {
        File deploy = new File(karafBase, "deploy");
        String[] fileEndings = new String[] { "jar", "war", "zip", "kar", "xml" };
        ProvisionOption<?>[] options = subsystem.getOptions(ProvisionOption.class);
        for (ProvisionOption<?> option : options) {
            try {
                File target = createUnique(option.getURL(), deploy, fileEndings);
                FileUtils.copyURLToFile(new URL(option.getURL()), target);
            }
            // CHECKSTYLE:SKIP
            catch (Exception e) {
                // well, this can happen...
            }
        }
    }

    private File createUnique(String url, File deploy, String[] fileEndings) {
        String prefix = UUID.randomUUID().toString();
        String realEnding = extractPossibleFileEndingIfMavenArtifact(url, fileEndings);
        String fileName = new File(url).getName();
        return new File(deploy, prefix + "_" + fileName + "." + realEnding);
    }

    private String extractPossibleFileEndingIfMavenArtifact(String url, String[] fileEndings) {
        String realEnding = "jar";
        for (String ending : fileEndings) {
            if (url.indexOf("/" + ending + "/") > 0) {
                realEnding = ending;
                break;
            }
        }
        return realEnding;
    }
    
    /**
     * Create a feature for the test dependencies
     * specified as ProvisionOption in the system
     * 
     * @return feature option for dependencies
     */
    public KarafFeaturesOption getDependenciesFeature() {
        if (subsystem == null) {
            return null;
        }

        try {
            File featuresXmlFile = new File(karafBase, "test-dependencies.xml");
            Writer wr = new OutputStreamWriter(new FileOutputStream(featuresXmlFile), "UTF-8");
            writeDependenciesFeature(wr, subsystem.getOptions(ProvisionOption.class));
            wr.close();
            String repoUrl = "file:"
                + featuresXmlFile.toString().replaceAll("\\\\", "/").replaceAll(" ", "%20");
            return new KarafFeaturesOption(repoUrl, "test-dependencies");
        }
        catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * Write a feature xml structure for test dependencies specified as ProvisionOption
     * in system to the given writer
     * 
     * @param writer where to write the feature xml
     * @param provisionOptions dependencies
     */
    static void writeDependenciesFeature(Writer writer, ProvisionOption<?>... provisionOptions) {
        XMLOutputFactory xof =  XMLOutputFactory.newInstance();
        xof.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        XMLStreamWriter sw = null;
        try {
            sw = xof.createXMLStreamWriter(writer);
            sw.writeStartDocument("UTF-8", "1.0");
            sw.setDefaultNamespace(KARAF_FEATURE_NS);
            sw.writeCharacters("\n");
            sw.writeStartElement("features");
            sw.writeAttribute("name", "test-dependencies");
            sw.writeCharacters("\n");
            sw.writeStartElement("feature");
            sw.writeAttribute("name", "test-dependencies");
            sw.writeCharacters("\n");
            for (ProvisionOption<?> provisionOption : provisionOptions) {
                if (provisionOption.getURL().startsWith("link")
                    || provisionOption.getURL().startsWith("scan-features")) {
                    // well those we've already handled at another location...
                    continue;
                }
                sw.writeStartElement("bundle");
                if (provisionOption.getStartLevel() != null) {
                    sw.writeAttribute("start-level", provisionOption.getStartLevel().toString());
                }
                sw.writeCharacters(provisionOption.getURL());
                endElement(sw);
            }
            endElement(sw);
            endElement(sw);
            sw.writeEndDocument();
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Error writing feature " + e.getMessage(), e);
        } 
        finally {
            close(sw);
        }
    }

    private static void endElement(XMLStreamWriter sw) throws XMLStreamException {
        sw.writeEndElement();
        sw.writeCharacters("\n");
    }

    private static void close(XMLStreamWriter sw) {
        if (sw != null) {
            try {
                sw.close();
            }
            catch (XMLStreamException e) {
            }
        }
    }

}
