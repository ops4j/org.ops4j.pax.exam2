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
public class MavenArtifactUrlReference
    implements MavenUrlReference
{

    /**
     * Artifact group id (cannot be null or empty).
     */
    private String m_groupId;
    /**
     * Artifact id  (cannot be null or empty).
     */
    private String m_artifactId;
    /**
     * Artifact type (can be null case when the default type is used = jar).
     */
    private String m_type;
    /**
     * Artifact version/version range (can be null case when latest version will be used).
     */
    private String m_version;
    /**
     * Artifact clasifier. Can be null.
     */
    private String m_classifier;

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference groupId( final String groupId )
    {
        validateNotEmpty( groupId, true, "Group" );
        m_groupId = groupId;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference artifactId( final String artifactId )
    {
        validateNotEmpty( artifactId, true, "Artifact" );
        m_artifactId = artifactId;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference type( final String type )
    {
        validateNotEmpty( type, true, "Type" );
        m_type = type;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public MavenUrlReference classifier( String classifier )
    {
        validateNotEmpty( classifier, true, "Classifier" );
        m_classifier = classifier;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference version( final String version )
    {
        validateNotEmpty( version, true, "Version" );
        m_version = version;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference version( final VersionResolver resolver )
    {
        validateNotNull( resolver, "Version resolver" );
        return version( resolver.getVersion( m_groupId, m_artifactId ) );
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactUrlReference versionAsInProject()
    {
        return version( MavenUtils.asInProject() );
    }

    /**
     * {@inheritDoc}
     */
    public Boolean isSnapshot()
    {
        return m_version == null ? null : m_version.endsWith( "SNAPSHOT" );
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException - If group id is null or empty
     *                                  - If artifact id is null or empty
     */
    public String getURL()
    {
        validateNotEmpty( m_groupId, true, "Group" );
        validateNotEmpty( m_artifactId, true, "Artifact" );
        final StringBuilder url = new StringBuilder();
        url.append( "mvn:" ).append( m_groupId ).append( "/" ).append( m_artifactId );
        if( m_version != null || m_type != null || m_classifier != null )
        {
            url.append( "/" );
        }
        if( m_version != null )
        {
            url.append( m_version );
        }
        if( m_type != null || m_classifier != null )
        {
            url.append( "/" );
        }
        if( m_type != null )
        {
            url.append( m_type );
        }
        if( m_classifier != null )
        {
            url.append( "/" ).append( m_classifier );
        }
        return url.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( this.getClass().getSimpleName() );
        sb.append( "{groupId='" ).append( m_groupId ).append( '\'' );
        sb.append( ", artifactId='" ).append( m_artifactId ).append( '\'' );
        sb.append( ", version='" ).append( m_version ).append( '\'' );
        sb.append( ", type='" ).append( m_type ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

}