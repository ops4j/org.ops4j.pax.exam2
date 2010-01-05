/*
 * Copyright 2008,2009 Toni Menzel.
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
package org.ops4j.pax.exam.raw.internal;

import java.io.File;
import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.lang.NullArgumentException;

/**
 * Finds resources of the current module under test just by given top-level parent (whatever that is)
 * and name of the class under test using a narrowing approach.
 *
 * @author Toni Menzel (tonit)
 * @since May 30, 2008
 */
public class FileTailImpl implements FileTail

{

    public static final Log logger = LogFactory.getLog( FileTailImpl.class );

    private File m_topLevelDir;

    private String m_tail;
    private File m_parentOfTail;

    /**
     * @param topLevelDir an existing folder on the local filesystem. Must be readable.
     * @param tailExpr    a relative path to a file in any depth under topLevelDir.
     *
     * @throws java.io.IOException if a problem while crawling occurs.
     */
    public FileTailImpl( final File topLevelDir, final String tailExpr )
        throws IOException
    {
        NullArgumentException.validateNotNull( topLevelDir, "topLevelDir" );
        NullArgumentException.validateNotNull( tailExpr, "tailExpr" );
        m_topLevelDir = topLevelDir;
        m_tail = tailExpr;

        validateTopLevelDir();
        findParentOfTail();
    }

    private void validateTopLevelDir()
    {
        if( !m_topLevelDir.exists() || !m_topLevelDir.canRead() || !m_topLevelDir.isDirectory() )
        {
            throw new IllegalArgumentException(
                "topLevelDir " + m_topLevelDir.getAbsolutePath() + " is not a readable folder"
            );
        }
        logger.debug( "Top level dir " + m_topLevelDir + " has been verified." );
    }

    private void findParentOfTail()
        throws IOException
    {
        if( m_tail == null || m_tail.length() == 0 )
        {
            m_parentOfTail = m_topLevelDir;
        }
        else
        {

            m_parentOfTail = findParentOfTail( m_topLevelDir );
            if( m_parentOfTail == null )
            {
                throw new IllegalArgumentException(
                    "topLevelDir " + m_topLevelDir.getAbsolutePath() + "  does not contain a tail " + m_tail
                );
            }
            else
            {
                logger.debug( "Parent of tail is: " + m_parentOfTail.getAbsolutePath() );
            }
        }
    }

    /**
     * Tries to find the parent of tail in sub folders of folder parameter.
     * Tail is a relative path to a file in any depth of folder parameter.
     * It will recursively call this method trying to find the given tail's parent.
     *
     * Note: THIS is being called recursively !!
     *
     * @param folder local folder that is used as (current) root. Sub folders wll be crawled recursively.
     *
     * @return the parent folder of the given anchor
     *
     * @throws java.io.IOException if a problem occures while crawling subfolders of *folder*
     */
    protected File findParentOfTail( File folder )
        throws IOException
    {
        logger.debug( "findParentOfTail " + folder.getAbsolutePath() );
        for( File f : folder.listFiles() )
        {
            if( !f.isHidden() && f.isDirectory() )
            {
                File r = findParentOfTail( f );
                if( r != null )
                {
                    return r;
                }
            }
            else if( !f.isHidden() )
            {
                String p = f.getCanonicalPath().replaceAll( "\\\\", "/" );
                if( p.endsWith( m_tail ) )
                {
                    return new File(
                        f.getCanonicalPath().substring( 0, f.getCanonicalPath().length() - m_tail.length() )
                    );
                }
            }
        }
        return null;
    }

    /**
     * @return the found (by crawling) parent of tail.
     */
    public File getParentOfTail()
    {
        return m_parentOfTail;
    }

    @Override
    public String toString()
    {
        return "FileTailImpl{" +
               "m_topLevelDir=" + m_topLevelDir +
               ", m_tail=" + m_tail +
               '}';
    }

}