package org.ops4j.pax.exam.quickbuild.internal;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import com.google.inject.Inject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.quickbuild.Quickbuild;
import org.ops4j.pax.exam.quickbuild.QType;
import org.ops4j.pax.exam.quickbuild.Snapshot;
import org.ops4j.pax.exam.quickbuild.SnapshotElement;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;

/**
 *
 */
public class DefaultQuickbuild implements Quickbuild
{

    public static final Log LOGGER = LogFactory.getLog( DefaultQuickbuild.class );

    final private Store<InputStream> m_store;

    @Inject
    public DefaultQuickbuild( Store<InputStream> store )
    {
        m_store = store;
    }

    public InputStream update( Snapshot referenceSnapshot, File changedContentFolder )
        throws IOException
    {
        // keep it very simple for now
        final Map<String, Handle> folderContent = new HashMap<String, Handle>();

        deflateFolder( folderContent, changedContentFolder.getCanonicalPath(), changedContentFolder );

        // pack jar from work into anchor
        final PipedInputStream pin = new PipedInputStream();
        final PipedOutputStream pout = new PipedOutputStream( pin );

        final Map<String, URI> contentMap = calculateNewJarContent( referenceSnapshot, folderContent );
        new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    pack( contentMap, pout );
                } catch( IOException e )
                {
                    e.printStackTrace();
                } finally
                {
                    try
                    {
                        pout.close();
                    } catch( IOException e )
                    {
                        //
                    }
                }
            }
        }.start();

        // TODO replace with IOPipes after this somehow works.
        return pin;
        //return new FileInputStream( new File( changedContentFolder, "build_by_quickbuild.jar" ) );

    }

    private void deflateFolder( Map<String, Handle> contentMap, String base, File folder )
        throws IOException
    {

        for( File f : folder.listFiles() )
        {
            if( !f.isHidden() && f.isDirectory() )
            {
                deflateFolder( contentMap, base, f );

            }
            else if( !f.isHidden() && f.getName().endsWith( ".class" ) )
            {

                String p = f.getCanonicalPath().replaceAll( "\\\\", "/" );
                // cut prefix
                p = p.substring( base.length() + 1 );
                contentMap.put( p, m_store.store( new FileInputStream( f ) ) );
            }
        }
    }

    /**
     * Just pack fully resolved resources into an outputstream.
     *
     * @param agg
     * @param out
     */
    public void pack( Map<String, URI> agg, OutputStream out )
        throws IOException
    {
        // calculate the final map

        JarOutputStream jout = new JarOutputStream( out );
        try
        {
            // first set manifest if available:
            URI manifest = agg.get( "META-INF/MANIFEST.MF" );
            if( manifest != null )
            {
                JarEntry entry = new JarEntry( "META-INF/MANIFEST.MF" );
                jout.putNextEntry( entry );
                StreamUtils.copyStream( manifest.toURL().openStream(), jout, false );
                jout.closeEntry();
            }
            for( String name : agg.keySet() )
            {
                if( name.equals( "META-INF/MANIFEST.MF" ) )
                {
                    continue;
                }

                JarEntry entry = new JarEntry( name );
                jout.putNextEntry( entry );
                StreamUtils.copyStream( agg.get( name ).toURL().openStream(), jout, false );
                jout.closeEntry();
            }
        } finally
        {
            jout.close();
        }

    }

    private Map<String, URI> calculateNewJarContent( Snapshot snapshot, Map<String, Handle> contentMap )
        throws IOException
    {
        Map<String, URI> agg = new HashMap<String, URI>();
        int files_new = 0;
        int files_changed = 0;
        int files_removed = 0;

        for( SnapshotElement snapshotElement : snapshot )
        {
            if( !contentMap.containsKey( snapshotElement.name() ) )
            {
                if( snapshotElement.type() == QType.OWN )
                {
                    // removed !
                    files_removed++;

                }
                else
                {
                    // external, still include
                    agg.put( snapshotElement.name(), snapshotElement.reference() );
                }
            }
            else
            {
                // exlude the ones we do not had initially, too. Add the rest of cause.
                if( snapshotElement.type() != QType.EXCLUDED )
                {
                    // if wanted, add this
                    if( snapshotElement.type() == QType.OWN && !checkSumsEquals( snapshotElement, contentMap.get( snapshotElement.name() ) ) )
                    {
                        files_changed++;
                    }
                    agg.put( snapshotElement.name(), m_store.getLocation( contentMap.get( snapshotElement.name() ) ) );
                }

                // remove from content so we do not pick it up again
                contentMap.remove( snapshotElement.name() );

            }
        }
        for( String name : contentMap.keySet() )
        {
            // just new stuff in here
            agg.put( name, m_store.getLocation( contentMap.get( name ) ) );
            files_new++;
        }
        if( files_changed + files_new + files_removed > 0 )
        {
            LOGGER.info( "QUICKBUILD CHANGESET: " + files_new + " new, " + files_removed + " removed, " + files_changed + " changed." );
        }
        else
        {
            LOGGER.info( "QUICKBUILD CHANGESET: no changes detected." );

        }
        return agg;
    }

    private boolean checkSumsEquals( SnapshotElement snapshotElement, Handle handle )
    {
        return snapshotElement.checksum().equals( handle.getIdentification() );
    }

}
