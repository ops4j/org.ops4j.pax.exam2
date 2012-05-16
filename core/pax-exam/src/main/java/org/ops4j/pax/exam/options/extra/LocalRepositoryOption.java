package org.ops4j.pax.exam.options.extra;

import org.ops4j.pax.exam.Option;

/**
 * @deprecated Only supported by Pax Runner Container which will be removed in Pax Exam 3.0.
 * @author Toni Menzel (tonit)
 * @since Apr 21, 2009
 */
@Deprecated
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
