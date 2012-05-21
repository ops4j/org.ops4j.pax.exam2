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
import org.ops4j.pax.exam.Option;

/**
 * Option specifing a framework.
 *
 * @deprecated Only supported by Pax Runner Container which will be removed in Pax Exam 3.0.
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 08, 2008
 */
@Deprecated
public class FrameworkOption
    implements Option
{

    /**
     * Framework name (cannot be null or empty).
     */
    private final String m_name;
    /**
     * Framework version (can be null, cese when the default framework version shall be used)
     */
    private String m_version;

    /**
     * Constructor.
     *
     * @param name framework name (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If name is null or empty
     */
    public FrameworkOption( final String name )
    {
        validateNotEmpty( name, true, "Name" );
        m_name = name;
    }

    /**
     * Sets the framework version.
     *
     * @param version framework version (cannot be null or empty)
     *
     * @return itself, for fluent api usage
     *
     * @throws IllegalArgumentException - If version is null or empty
     */
    public FrameworkOption version( final String version )
    {
        validateNotEmpty( version, true, "version" );
        m_version = version;
        return this;
    }

    /**
     * Sets the framework version to snapshot.
     *
     * @return itself, for fluent api usage
     */
    public FrameworkOption snapshotVersion()
    {
        m_version = "snapshot";
        return this;
    }

    /**
     * Getter.
     *
     * @return framework name (cannot be null or empty)
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Getter.
     *
     * @return framework version (can be null, case when default version should be used)
     */
    public String getVersion()
    {
        return m_version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "FrameworkOption" );
        sb.append( "{name='" ).append( m_name ).append( '\'' );
        if( m_version != null )
        {
            sb.append( ", version='" ).append( m_version ).append( '\'' );
        }
        sb.append( '}' );
        return sb.toString();
    }

}
