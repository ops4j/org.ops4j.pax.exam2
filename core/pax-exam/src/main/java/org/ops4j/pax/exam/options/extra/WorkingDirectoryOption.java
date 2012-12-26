package org.ops4j.pax.exam.options.extra;

import org.ops4j.pax.exam.Option;

/**
 * Option to overwrite --workingDirectory default.
 * 
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0 December 10, 2008
 */
public class WorkingDirectoryOption implements Option {

    private final String workingDirectory;

    public WorkingDirectoryOption(String directory) {
        workingDirectory = directory;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }
}
