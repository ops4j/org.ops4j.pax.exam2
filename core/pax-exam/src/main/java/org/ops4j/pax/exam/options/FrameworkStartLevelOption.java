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

import org.ops4j.pax.exam.Option;

/**
 * Option specifying framework start level.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 23, 2009
 */
public class FrameworkStartLevelOption
    implements Option
{

    /**
     * Start level.
     */
    private final int m_startLevel;

    /**
     * Constructor.
     *
     * @param startLevel framework start level (must be bigger then zero)
     *
     * @throws IllegalArgumentException - If start level is <= 0
     */
    public FrameworkStartLevelOption( final int startLevel )
    {
        if( startLevel <= 0 )
        {
            throw new IllegalArgumentException( "Start level must be bigger then zero" );
        }
        m_startLevel = startLevel;
    }

    /**
     * Getter.
     *
     * @return startlevel (bigger then zero)
     */
    public int getStartLevel()
    {
        return m_startLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return new StringBuilder()
            .append( FrameworkStartLevelOption.class.getSimpleName() )
            .append( "{startlevel='" ).append( m_startLevel ).append( "\'}" )
            .toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_startLevel;
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if( this == obj )
            return true;
        if( obj == null )
            return false;
        if( getClass() != obj.getClass() )
            return false;
        FrameworkStartLevelOption other = (FrameworkStartLevelOption) obj;
        if( m_startLevel != other.m_startLevel )
            return false;
        return true;
    }

}