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

/**
 * Option specifying provisioning from an maven url (Pax URL mvn: handler).
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 08, 2008
 */
public class MavenArtifactProvisionOption
    extends AbstractProvisionOption<MavenArtifactProvisionOption>
    implements MavenUrlReference
{

    /**
     * Maven artifact.
     */
    private final MavenUrlReference m_artifact;
    /**
     * True if the user used update method.
     */
    private boolean m_updateUsed;

    /**
     * Constructor.
     */
    public MavenArtifactProvisionOption()
    {
        m_artifact = new MavenArtifactUrlReference();
    }

    /**
     * Constructor based on a mevn artifact option.
     *
     * @param artifact maven artifact (cannot be null)
     */
    public MavenArtifactProvisionOption( final MavenUrlReference artifact )
    {
        validateNotNull( artifact, "Maven artifact" );
        m_artifact = artifact;
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactProvisionOption groupId( final String groupId )
    {
        m_artifact.groupId( groupId );
        m_updateUsed = false;
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactProvisionOption artifactId( final String artifactId )
    {
        m_artifact.artifactId( artifactId );
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactProvisionOption type( final String type )
    {
        m_artifact.type( type );
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactProvisionOption classifier( String classifier )
    {
        m_artifact.classifier( classifier );
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactProvisionOption version( final String version )
    {
        m_artifact.version( version );
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactProvisionOption version( final MavenArtifactUrlReference.VersionResolver resolver )
    {
        m_artifact.version( resolver );
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public MavenArtifactProvisionOption versionAsInProject()
    {
        m_artifact.versionAsInProject();
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    public Boolean isSnapshot()
    {
        return m_artifact.isSnapshot();
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        return m_artifact.getURL();
    }

    /**
     * {@inheritDoc}
     * Keep track if the user used the update method, so we do not override the value when handling automatic update on
     * SNAPSHOT versions.
     */
    @Override
    public MavenArtifactProvisionOption update( final Boolean shouldUpdate )
    {
        m_updateUsed = true;
        return super.update( shouldUpdate );
    }

    @Override
    public boolean shouldUpdate()
    {
        if( !m_updateUsed )
        {
            super.update( isSnapshot() != null && isSnapshot() );
        }
        return super.shouldUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return m_artifact.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected MavenArtifactProvisionOption itself()
    {
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( ( m_artifact == null ) ? 0 : m_artifact.hashCode() );
        result = prime * result + ( m_updateUsed ? 1231 : 1237 );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( this == obj )
            return true;
        if( !super.equals( obj ) )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        MavenArtifactProvisionOption other = (MavenArtifactProvisionOption) obj;
        if( m_artifact == null )
        {
            if( other.m_artifact != null )
                return false;
        }
        else if( !m_artifact.equals( other.m_artifact ) )
            return false;
        if( m_updateUsed != other.m_updateUsed )
            return false;
        return true;
    }

}