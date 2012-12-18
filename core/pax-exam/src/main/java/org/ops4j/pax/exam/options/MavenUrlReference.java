/*
 * Copyright 2009 Alin Dreghiciu.
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

/**
 * Option specifying a maven url (Pax URL mvn: handler).
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 26, 2009
 */
public interface MavenUrlReference extends UrlReference {

    /**
     * Sets the artifact group id.
     * 
     * @param groupId
     *            artifact group id (cannot be null or empty)
     * 
     * @return itself, for fluent api usage
     * 
     * @throws IllegalArgumentException
     *             - If group id is null or empty
     */
    MavenUrlReference groupId(String groupId);

    /**
     * Sets the artifact id.
     * 
     * @param artifactId
     *            artifact id (cannot be null or empty)
     * 
     * @return itself, for fluent api usage
     * 
     * @throws IllegalArgumentException
     *             - If artifact id is null or empty
     */
    MavenUrlReference artifactId(String artifactId);

    /**
     * Sets the artifact type. Do not set the value (use this method) if default artifact type
     * should be used.
     * 
     * @param type
     *            artifact type (cannot be null or empty)
     * 
     * @return itself, for fluent api usage
     * 
     * @throws IllegalArgumentException
     *             - If type is null or empty
     */
    MavenUrlReference type(String type);

    /**
     * Sets the artifact classifier.
     * 
     * @param classifier
     *            artifact classifier (cannot be null or empty)
     * 
     * @return itself, for fluent api usage
     * 
     * @throws IllegalArgumentException
     *             - If classifier is null or empty
     */
    MavenUrlReference classifier(String classifier);

    /**
     * Sets the artifact version or version range. If version is a SNAPSHOT version the bundle will
     * be set to updatable, otherwise the bundle will not be updated. This handling happens only if
     * the user dows not use the update() by itself (see
     * {@link org.ops4j.pax.exam.options.ProvisionOption#update(boolean)}).
     * 
     * @param version
     *            artifact version / version range (cannot be null or empty)
     * 
     * @return itself, for fluent api usage
     * 
     * @throws IllegalArgumentException
     *             - If version is null or empty
     */
    MavenUrlReference version(String version);

    /**
     * Determines the artifact version using an {@link MavenUrlReference.VersionResolver}.
     * 
     * @param resolver
     *            a {@link MavenUrlReference.VersionResolver} (cannot be null)
     * 
     * @return itself, for fluent api usage
     * 
     * @throws IllegalArgumentException
     *             - If version is null
     */
    MavenUrlReference version(VersionResolver resolver);

    /**
     * Version will be discovered from the dependencies of Maven project that includes the
     * regression.
     * 
     * @return itself, for fluent api usage
     */
    MavenUrlReference versionAsInProject();

    /**
     * Returns true if the specified version is a snapshot version, false if not and null if the
     * version is not yet specified.
     * 
     * @return true if the specified version is a snapshot version, false if not and null if the
     *         version is not yet specified.
     */
    Boolean isSnapshot();

    /**
     * Resolves versions based on maven artifact groupId / atifactid.
     */
    public static interface VersionResolver {

        /**
         * Getter.
         * 
         * @param groupId
         *            groupd id
         * @param artifactId
         *            artifact id
         * 
         * @return discovered version
         */
        String getVersion(String groupId, String artifactId);

    }

}
