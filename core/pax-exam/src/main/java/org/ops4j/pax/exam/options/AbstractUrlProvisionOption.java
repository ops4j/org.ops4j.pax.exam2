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
 * Option specifying a provision url.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 26, 2009
 */
public abstract class AbstractUrlProvisionOption<T extends AbstractUrlProvisionOption<T>>
    extends AbstractProvisionOption<T>
{

    /**
     * Provision url (cannot be null).
     */
    private final UrlReference m_urlReference;

    /**
     * Constructor.
     *
     * @param url provision url (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public AbstractUrlProvisionOption( final String url )
    {
        this( new RawUrlReference( url ) );
    }

    /**
     * Constructor.
     *
     * @param url provision url (cannot be null)
     *
     * @throws IllegalArgumentException - If url is null
     */
    public AbstractUrlProvisionOption( final UrlReference url )
    {
        validateNotNull( url, "URL" );
        m_urlReference = url;
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        return m_urlReference.getURL();
    }

    /**
     * Getter.
     *
     * @return url reference
     */
    public UrlReference getUrlReference()
    {
        return m_urlReference;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( this.getClass().getSimpleName() );
        sb.append( "{url='" ).append( m_urlReference ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ( ( m_urlReference == null ) ? 0 : m_urlReference.hashCode() );
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
        @SuppressWarnings( "unchecked" )
        AbstractUrlProvisionOption<T> other = (AbstractUrlProvisionOption<T>) obj;
        if( m_urlReference == null )
        {
            if( other.m_urlReference != null )
                return false;
        }
        else if( !m_urlReference.equals( other.m_urlReference ) )
            return false;
        return true;
    }

}