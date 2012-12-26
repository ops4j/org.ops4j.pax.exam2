/*
 * Copyright 2008 Alin Dreghiciu.
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

/**
 * Option specifying provisioning from an maven url (Pax URL mvn: handler).
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 08, 2008
 */
public class MavenArtifactProvisionOption extends
    AbstractProvisionOption<MavenArtifactProvisionOption> implements MavenUrlReference {

    /**
     * Maven artifact.
     */
    private final MavenUrlReference artifact;
    /**
     * True if the user used update method.
     */
    private boolean updateUsed;

    /**
     * Constructor.
     */
    public MavenArtifactProvisionOption() {
        artifact = new MavenArtifactUrlReference();
    }

    /**
     * Constructor based on a mevn artifact option.
     * 
     * @param artifact
     *            maven artifact (cannot be null)
     */
    public MavenArtifactProvisionOption(final MavenUrlReference artifact) {
        validateNotNull(artifact, "Maven artifact");
        this.artifact = artifact;
    }

    public MavenArtifactProvisionOption groupId(final String groupId) {
        artifact.groupId(groupId);
        updateUsed = false;
        return itself();
    }

    public MavenArtifactProvisionOption artifactId(final String artifactId) {
        artifact.artifactId(artifactId);
        return itself();
    }

    public MavenArtifactProvisionOption type(final String type) {
        artifact.type(type);
        return itself();
    }

    public MavenArtifactProvisionOption classifier(String classifier) {
        artifact.classifier(classifier);
        return itself();
    }

    public MavenArtifactProvisionOption version(final String version) {
        artifact.version(version);
        return itself();
    }

    public MavenArtifactProvisionOption version(
        final MavenArtifactUrlReference.VersionResolver resolver) {
        artifact.version(resolver);
        return itself();
    }

    public MavenArtifactProvisionOption versionAsInProject() {
        artifact.versionAsInProject();
        return itself();
    }

    public Boolean isSnapshot() {
        return artifact.isSnapshot();
    }

    public String getURL() {
        return artifact.getURL();
    }

    /**
     * Keep track if the user used the update method, so we do not override the value
     * when handling automatic update on SNAPSHOT versions.
     */
    @Override
    public MavenArtifactProvisionOption update(final Boolean shouldUpdate) {
        updateUsed = true;
        return super.update(shouldUpdate);
    }

    @Override
    public boolean shouldUpdate() {
        if (!updateUsed) {
            super.update(isSnapshot() != null && isSnapshot());
        }
        return super.shouldUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return artifact.toString();
    }

    protected MavenArtifactProvisionOption itself() {
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((artifact == null) ? 0 : artifact.hashCode());
        result = prime * result + (updateUsed ? 1231 : 1237);
        return result;
    }

    // CHECKSTYLE:OFF : generated code
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        MavenArtifactProvisionOption other = (MavenArtifactProvisionOption) obj;
        if (artifact == null) {
            if (other.artifact != null)
                return false;
        }
        else if (!artifact.equals(other.artifact))
            return false;
        if (updateUsed != other.updateUsed)
            return false;
        return true;
    }
    // CHECKSTYLE:ON
}
