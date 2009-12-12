package org.ops4j.pax.exam.container.def.options;

import org.ops4j.pax.exam.Option;

/**
 * Option to overwrite --workingDirectory default.
 *
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0 December 10, 2008
 */
public class WorkingDirectoryOption implements Option
{

    final private String m_workingDirectory;

    public WorkingDirectoryOption( String directory )
    {
        m_workingDirectory = directory;
    }

    public String getWorkingDirectory()
    {
        return m_workingDirectory;
    }
}
