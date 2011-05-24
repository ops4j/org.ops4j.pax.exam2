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
package org.ops4j.pax.exam.options.extra;

import static org.ops4j.lang.NullArgumentException.*;
import org.ops4j.pax.exam.MavenUtils;
import org.ops4j.pax.exam.Option;

/**
 * Option specifying a Pax runner provisioning profile.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 08, 2008
 */
public class ProfileOption
    implements Option
{

    /**
     * Profile name. Cannot be null or empty.
     */
    private final String m_name;

    /**
     * Profile version. Cannot be empty.
     */
    private String m_version;

    /**
     * Constructor.
     *
     * @param name profile name (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If name is null or empty
     */
    public ProfileOption( final String name )
    {
        this( name, null );
    }

    /**
     * Constructor.
     *
     * @param name    profile name (cannot be null or empty)
     * @param version profile version (cannot be empty)
     *
     * @throws IllegalArgumentException - If name is null or empty
     */
    public ProfileOption( final String name,
                          final String version )
    {
        validateNotEmpty( name, true, "Profile name" );
        if( version != null )
        {
            validateNotEmpty( version, true, "Profile version" );
        }
        m_name = name;
        m_version = version;
    }

    /**
     * Getter.
     *
     * @return profile (cannot be null or empty)
     */
    public String getProfile()
    {
        return m_name + ( m_version == null ? "" : "/" + m_version );
    }

    /**
     * Sets the profile version or version range. Do not set (use this method) if the latest version should be
     * discovered and used)
     *
     * @param version artifact version / version range (cannot be empty)
     *
     * @return itself, for fluent api usage
     *
     * @throws IllegalArgumentException - If version is empty
     */
    public ProfileOption version( final String version )
    {
        if( version != null )
        {
            validateNotEmpty( version, true, "Version" );
        }
        m_version = version;
        return this;
    }

    /**
     * Discovers the profile version out of the project.
     * To do so, the maven project should have a dependency on the profile and Pax Exam maven plugin configured to run
     * the "generate-depends-file" goal.
     *
     * @return itself, for fluent api usage
     */
    public ProfileOption versionAsInProject()
    {
        return version( MavenUtils.asInProject().getVersion( "org.ops4j.pax.runner.profiles", getProfile() ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "ProfileOption" );
        sb.append( "{name='" ).append( m_name ).append( '\'' );
        if( m_version != null )
        {
            sb.append( ",version='" ).append( m_version ).append( '\'' );
        }
        sb.append( '}' );
        return sb.toString();
    }

}
