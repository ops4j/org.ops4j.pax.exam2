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

import static org.ops4j.lang.NullArgumentException.*;

/**
 * Option specifying an url.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 26, 2009
 */
public class RawUrlReference
    implements UrlReference
{

    /**
     * Provision url (cannot be null or empty).
     */
    private final String m_url;

    /**
     * Constructor.
     *
     * @param url provision url (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public RawUrlReference( final String url )
    {
        validateNotEmpty( url, true, "URL" );
        m_url = url;
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        return m_url;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( this.getClass().getSimpleName() );
        sb.append( "{url='" ).append( m_url ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( m_url == null ) ? 0 : m_url.hashCode() );
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
        RawUrlReference other = (RawUrlReference) obj;
        if( m_url == null )
        {
            if( other.m_url != null )
                return false;
        }
        else if( !m_url.equals( other.m_url ) )
            return false;
        return true;
    }

}
