package org.ops4j.pax.exam.options.extra;

import org.ops4j.pax.exam.options.ValueOption;

/**
 * @author Toni Menzel (tonit)
 * @since Mar 7, 2009
 */
public class CleanCachesOption implements ValueOption<Boolean>
{

    private static final Boolean DEFAULT_VALUE = Boolean.TRUE;
    private Boolean m_value;

    public CleanCachesOption() {
         m_value = DEFAULT_VALUE;
    }
    
    public CleanCachesOption(Boolean value) {
        m_value = value;
   }
    
    public CleanCachesOption setValue(Boolean value) {
        m_value = value;
        return this;
    }
    
    public Boolean getValue()
    {
         return m_value;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
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
        CleanCachesOption other = (CleanCachesOption) obj;
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
