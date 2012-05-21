package org.ops4j.pax.exam.spi.intern;

import java.io.File;
import java.io.IOException;

/**
 * Recursively tries to find the exact "target/classes" folder of a given clazz.
 * Pax Exam uses it to calculate
 */
public class ClassSourceFolder {

    final private File m_base;

    ClassSourceFolder( File base )
    {
        m_base = base;
    }

    /**
     * @param clazz to find the root classes folder for.
     *
     * @return A File instance being the exact folder on disk or null, if it hasn't been found under m_base. (see constructor)
     *
     * @throws java.io.IOException if a problem ocures (method crawls folders on disk..)
     */
    public File find( Class clazz )
        throws IOException
    {
        return findParentOfTail( m_base, convert( clazz ) );
    }

    private String convert( Class c )
    {
        return c.getName().replace( ".", "/" ) + ".class";

    }

    protected File findParentOfTail( final File folder, final String tail )
        throws IOException
    {
        for( File f : folder.listFiles() ) {
            if( !f.isHidden() && f.isDirectory() ) {
                File r = findParentOfTail( f, tail );
                if( r != null ) {
                    return r;
                }
            }
            else if( !f.isHidden() ) {
                String p = f.getCanonicalPath().replaceAll( "\\\\", "/" );
                if( p.endsWith( tail ) ) {
                    return new File( f.getCanonicalPath().substring( 0, f.getCanonicalPath().length() - tail.length() ) );
                }
            }
        }
        return null;
    }
}
