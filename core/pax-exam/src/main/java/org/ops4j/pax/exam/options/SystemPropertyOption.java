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
 * Option specifying a system property.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 10, 2008
 */
public class SystemPropertyOption implements ValueOption
{

    /**
     * System property key (cannot be null or empty).
     */
    private final String m_key;
    /**
     * System property value (can be null or empty).
     */
    private String m_value;

    /**
     * Constructor.
     *
     * @param key system property key (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If key is null or empty
     */
    public SystemPropertyOption( final String key )
    {
        m_key = key;
        m_value = "";
    }

    /**
     * Sets the system property value.
     *
     * @param value system property value (cannot be null, can be empty)
     *
     * @return itself, for fluent api usage
     *
     * @throws IllegalArgumentException - If value is null
     */
    public SystemPropertyOption value( final String value )
    {
        validateNotNull( value, "Value" );
        m_value = value;
        return this;
    }

    /**
     * Getter.
     *
     * @return system property key (cannot be null or empty)
     */
    public String getKey()
    {
        return m_key;
    }

    /**
     * Getter.
     *
     * @return system property value (cannot be null, can be empty)
     */
    public String getValue()
    {
        return m_value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "SystemPropertyOption" );
        sb.append( "{key='" ).append( m_key ).append( '\'' );
        sb.append( ", value='" ).append( m_value ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( m_key == null ) ? 0 : m_key.hashCode() );
        result = prime * result + ( ( m_value == null ) ? 0 : m_value.hashCode() );
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
        SystemPropertyOption other = (SystemPropertyOption) obj;
        if( m_key == null )
        {
            if( other.m_key != null )
                return false;
        }
        else if( !m_key.equals( other.m_key ) )
            return false;
        if( m_value == null )
        {
            if( other.m_value != null )
                return false;
        }
        else if( !m_value.equals( other.m_value ) )
            return false;
        return true;
    }

}
