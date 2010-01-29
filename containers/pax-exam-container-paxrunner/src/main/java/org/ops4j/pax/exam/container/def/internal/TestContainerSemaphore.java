/*
 * Copyright 2009 Toni Menzel
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
package org.ops4j.pax.exam.container.def.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Simple file system lock.
 * Will check for existing lock file in given folder (constructor) and report on acquire()
 * On release it will remove the lock.
 *
 * Any failing acquire is "logged" into the lock file.
 *
 */
public class TestContainerSemaphore
{

    /**
     * JCL logger.
     */
    private static final Log LOG = LogFactory.getLog( PaxRunnerTestContainer.class );

    private File m_workingFolder;

    public TestContainerSemaphore( File workingFolder )
    {
        m_workingFolder = workingFolder;
    }

    public boolean acquire()
    {
        if( lockExists() )
        {
            // blame !
            LOG.error( "Blame ! Acquire lock for new Pax Runner instance failed at " + getLockFile().getAbsolutePath() );

            appendToFile( "! Tried to acquire this on " + new Date().toString() );
            return false;
        }
        else
        {
            // create
            LOG.info( "Acquire lock for new Pax Runner instance on " + getLockFile().getAbsolutePath() );

            appendToFile( "Created on " + new Date().toString() );
            return true;
        }
    }

    public void release()
    {
        // delete file
        getLockFile().delete();
    }

    public File getLockFile()
    {
        return new File( m_workingFolder, "paxexam.lock" );
    }

    private boolean lockExists()
    {
        return getLockFile().exists();
    }

    private void appendToFile( String s )
    {
        FileWriter fw = null;
        try
        {
            fw = new FileWriter( getLockFile(), true );
            fw.write( s );
        } catch( IOException e )
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                fw.close();
            } catch( IOException e )
            {
                e.printStackTrace();
            }
        }


    }
}
