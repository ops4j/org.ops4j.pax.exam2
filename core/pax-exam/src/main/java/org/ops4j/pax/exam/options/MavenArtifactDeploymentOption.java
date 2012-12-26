/*
 * Copyright 2012 Harald Wellmann
 *
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
package org.ops4j.pax.exam.options;

import static org.ops4j.lang.NullArgumentException.validateNotNull;

public class MavenArtifactDeploymentOption extends UrlDeploymentOption implements MavenUrlReference {

    private MavenUrlReference artifact;

    public MavenArtifactDeploymentOption() {
        // FIXME
        super("file:/dummy");
        this.artifact = new MavenArtifactUrlReference();
    }

    public MavenArtifactDeploymentOption(final MavenUrlReference artifact) {
        super(artifact);
        validateNotNull(artifact, "Maven artifact");
        this.artifact = artifact;
    }

    public String getURL() {
        return artifact.getURL();
    }

    public MavenArtifactDeploymentOption groupId(String groupId) {
        artifact.groupId(groupId);
        return this;
    }

    public MavenArtifactDeploymentOption artifactId(String artifactId) {
        artifact.artifactId(artifactId);
        return this;
    }

    public MavenArtifactDeploymentOption type(String type) {
        artifact.type(type);
        return this;
    }

    public MavenArtifactDeploymentOption classifier(String classifier) {
        artifact.classifier(classifier);
        return this;
    }

    public MavenArtifactDeploymentOption version(String version) {
        artifact.version(version);
        return this;
    }

    public MavenArtifactDeploymentOption version(VersionResolver resolver) {
        artifact.version(resolver);
        return this;
    }

    public MavenArtifactDeploymentOption versionAsInProject() {
        artifact.versionAsInProject();
        return this;
    }

    public Boolean isSnapshot() {
        return artifact.isSnapshot();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
        return result;
    }

    // CHECKSTYLE:OFF : generated code
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MavenArtifactDeploymentOption other = (MavenArtifactDeploymentOption) obj;
        if (artifact == null) {
            if (other.artifact != null)
                return false;
        }
        else if (!artifact.equals(other.artifact))
            return false;
        return true;
    }
    // CHECKSTYLE:ON
}
