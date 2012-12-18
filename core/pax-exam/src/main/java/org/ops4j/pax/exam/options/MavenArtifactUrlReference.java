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

import static org.ops4j.lang.NullArgumentException.*;
import org.ops4j.pax.exam.MavenUtils;

/**
 * Option specifying a maven url (Pax URL mvn: handler).
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 25, 2008
 */
public class MavenArtifactUrlReference implements MavenUrlReference {

    /**
     * Artifact group id (cannot be null or empty).
     */
    private String groupId;
    /**
     * Artifact id (cannot be null or empty).
     */
    private String artifactId;
    /**
     * Artifact type (can be null case when the default type is used = jar).
     */
    private String type;
    /**
     * Artifact version/version range (can be null case when latest version will be used).
     */
    private String version;
    /**
     * Artifact clasifier. Can be null.
     */
    private String classifier;

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference groupId(final String groupId) {
        validateNotEmpty(groupId, true, "Group");
        this.groupId = groupId;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference artifactId(final String artifactId) {
        validateNotEmpty(artifactId, true, "Artifact");
        this.artifactId = artifactId;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference type(final String type) {
        validateNotEmpty(type, true, "Type");
        this.type = type;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public MavenUrlReference classifier(String classifier) {
        validateNotEmpty(classifier, true, "Classifier");
        this.classifier = classifier;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference version(final String version) {
        validateNotEmpty(version, true, "Version");
        this.version = version;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference version(final VersionResolver resolver) {
        validateNotNull(resolver, "Version resolver");
        return version(resolver.getVersion(groupId, artifactId));
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference versionAsInProject() {
        return version(MavenUtils.asInProject());
    }

    /**
     * {@inheritDoc}
     */
    public Boolean isSnapshot() {
        return version == null ? null : version.endsWith("SNAPSHOT");
    }

    /**
     * {@inheritDoc}
     * 
     * @throws IllegalArgumentException
     *             - If group id is null or empty - If artifact id is null or empty
     */
    public String getURL() {
        validateNotEmpty(groupId, true, "Group");
        validateNotEmpty(artifactId, true, "Artifact");
        final StringBuilder url = new StringBuilder();
        url.append("mvn:").append(groupId).append("/").append(artifactId);
        if (version != null || type != null || classifier != null) {
            url.append("/");
        }
        if (version != null) {
            url.append(version);
        }
        if (type != null || classifier != null) {
            url.append("/");
        }
        if (type != null) {
            url.append(type);
        }
        if (classifier != null) {
            url.append("/").append(classifier);
        }
        return url.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName());
        sb.append("{groupId='").append(groupId).append('\'');
        sb.append(", artifactId='").append(artifactId).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
        result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MavenArtifactUrlReference other = (MavenArtifactUrlReference) obj;
        if (artifactId == null) {
            if (other.artifactId != null)
                return false;
        }
        else if (!artifactId.equals(other.artifactId))
            return false;
        if (classifier == null) {
            if (other.classifier != null)
                return false;
        }
        else if (!classifier.equals(other.classifier))
            return false;
        if (groupId == null) {
            if (other.groupId != null)
                return false;
        }
        else if (!groupId.equals(other.groupId))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        }
        else if (!type.equals(other.type))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        }
        else if (!version.equals(other.version))
            return false;
        return true;
    }

}
