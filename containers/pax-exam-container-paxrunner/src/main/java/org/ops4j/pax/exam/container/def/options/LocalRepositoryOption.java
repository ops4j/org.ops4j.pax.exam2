package org.ops4j.pax.exam.container.def.options;

import org.ops4j.pax.exam.Option;

/**
 * @author Toni Menzel (tonit)
 * @since Apr 21, 2009
 */
public class LocalRepositoryOption implements Option
{

    private String m_path;

    public LocalRepositoryOption( String path )
    {
        m_path = path;
    }

    public String getLocalRepositoryPath() {
        return m_path;
    }
}
