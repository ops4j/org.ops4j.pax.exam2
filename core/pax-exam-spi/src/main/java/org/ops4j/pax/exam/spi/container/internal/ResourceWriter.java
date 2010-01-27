package org.ops4j.pax.exam.spi.container.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.ops4j.io.StreamUtils;
import org.ops4j.lang.NullArgumentException;

/**
 * @author Toni Menzel (tonit)
 * @since Jan 14, 2009
 */
public class ResourceWriter implements ResourceLocator
{

    private final File m_base;
    private final FilenameFilter m_filter;

    public ResourceWriter( FilenameFilter filter, File base )
    {
        m_filter = filter;
        m_base = base;
    }

    public ResourceWriter( File base )
    {
        m_filter = new FilenameFilter()
        {

            public boolean accept( File file, String s )
            {
                return true;
            }
        };
        m_base = base;
    }

    /**
     * This locates the top level resource folders for the current component
     *
     * @param target to write to
     */
    public void write( JarOutputStream target )
        throws IOException
    {
        NullArgumentException.validateNotNull( target, "target" );

        if( m_base != null )
        {
            findAndWriteResources( target, m_base );
        }
        else
        {
            throw new IllegalArgumentException(
                "-- has not been found!"
            );
        }
    }

    /**
     * @param target the JarOutputStream to write to.
     * @param dir    the current folder to list and write (recursive call!)
     */
    private void findAndWriteResources( final JarOutputStream target, final File dir )
        throws IOException
    {
        if( dir != null && dir.canRead() && dir.isDirectory() )
        {
            for( File f : dir.listFiles( m_filter ) )
            {
                if( f.isDirectory() )
                {
                    findAndWriteResources( target, f );
                }
                else if( !f.isHidden() )
                {
                    writeToTarget( target, f );
                }
            }
        }
    }

    private void writeToTarget( JarOutputStream target, File f )
        throws IOException
    {

        String name =
            f.getCanonicalPath()
                .substring( m_base.getCanonicalPath().length() + 1 )
                .replace( File.separatorChar, '/' );
        if( name.equals( "META-INF/MANIFEST.MF" ) )
        {
            throw new RuntimeException( "You have specified a " + name
                                        + " in your probe bundle. Please make sure that you don't have it in your project's target folder. Otherwise it would lead to false assumptions and unexpected results."
            );
        }
        FileInputStream fis = new FileInputStream( f );
        try
        {
            write( name, fis, target );

        } finally
        {
            fis.close();
        }
    }

    void write( String name, InputStream fileIn, JarOutputStream target )
        throws IOException
    {
        target.putNextEntry( new JarEntry( name ) );
        StreamUtils.copyStream( fileIn, target, false );
    }
}