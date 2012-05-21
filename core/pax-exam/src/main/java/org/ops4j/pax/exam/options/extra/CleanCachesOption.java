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
    
}
