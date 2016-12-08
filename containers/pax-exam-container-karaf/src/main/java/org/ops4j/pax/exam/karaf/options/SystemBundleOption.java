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
package org.ops4j.pax.exam.karaf.options;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.RawUrlReference;
import org.ops4j.pax.exam.options.UrlReference;

import java.io.File;
import java.net.MalformedURLException;

/**
 * Copies the specified file into the Karaf system folder. (A Karaf folder that follows the m2 directory structure)
 */
public class SystemBundleOption implements Option {

    private String group;
    private String artifact;
    private String version;
    private File file;


    public SystemBundleOption(String aGroup, String artifact, String version, File file) {
        addSystemBundle(aGroup, artifact, version, file);
    }

    public SystemBundleOption(String aGroup, String artifact, String version) {
        addSystemBundle(aGroup, artifact, version);
    }

    public String getGroup() {
        return group;
    }

    public String getArtifact() {
        return artifact;
    }

    public String getVersion() {
        return version;
    }

    public SystemBundleOption addSystemBundle(String aGroup, String artifact, String version) {
        return addSystemBundle(aGroup, artifact, version, null);
    }

    public SystemBundleOption addSystemBundle(String aGroup, String artifact, String version, File file) {
        this.group = aGroup;
        this.artifact = artifact;
        this.version = version;
        this.file = file;
        return this;
    }

    /**
     *
     * @return The URL to fetch the artifact
     */
    public UrlReference getLibraryUrl() {
        if (file != null) {
            try {
                return new RawUrlReference(file.toURI().toURL().toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return new MavenArtifactUrlReference().groupId(group).artifactId(artifact).version(version);
        }
    }

    /**
     *
     * @return The maven repository path to the artifact.  Must start with <strong>/</strong>.
     * This path should not include the /system prefix.
     */
    public String getRepositoryPath() {
        String path = "/" + getGroup().replace(".", "/");
        path += "/" + getArtifact();
        path += "/" + getVersion();
        if (file != null) {
            path += "/" + file.getName();
        } else {
            path += "/" + getArtifact() + "-" + getVersion() + ".jar";
        }
        return path;
    }
}
