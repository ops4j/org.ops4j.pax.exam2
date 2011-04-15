/*
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.container.externalframework.internal.javarunner;

/**
 * Provides easy building of command line for a java runtime.
 * TODO add unit tests
 *
 * @author Alin Dreghiciu
 * @since August 25, 2007
 */
public class CommandLineBuilder
{

    /**
     * The command line array.
     */
    private String[] m_commandLine;

    /**
     * Creates a new command line builder.
     */
    public CommandLineBuilder()
    {
        m_commandLine = new String[0];
    }

    /**
     * Appends an array of strings to command line.
     *
     * @param segments array to append
     *
     * @return CommandLineBuilder for fluent api
     */
    public CommandLineBuilder append( final String[] segments )
    {
        if( segments != null && segments.length > 0 )
        {
            final String[] command = new String[m_commandLine.length + segments.length];
            System.arraycopy( m_commandLine, 0, command, 0, m_commandLine.length );
            System.arraycopy( segments, 0, command, m_commandLine.length, segments.length );
            m_commandLine = command;
        }
        return this;
    }

    /**
     * Appends a string to command line.
     *
     * @param segment string to append
     *
     * @return CommandLineBuilder for fluent api
     */
    public CommandLineBuilder append( final String segment )
    {
        if( segment != null )
        {
            return append( new String[]{ segment } );
        }
        return this;
    }

    /**
     * Returns the command line.
     *
     * @return command line
     */
    public String[] toArray()
    {
        return m_commandLine;
    }

}
