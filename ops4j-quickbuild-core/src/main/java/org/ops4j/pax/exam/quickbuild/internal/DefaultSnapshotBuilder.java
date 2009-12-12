package org.ops4j.pax.exam.quickbuild.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.ops4j.pax.exam.quickbuild.QType;
import org.ops4j.pax.exam.quickbuild.Snapshot;
import org.ops4j.pax.exam.quickbuild.SnapshotBuilder;
import org.ops4j.pax.exam.quickbuild.SnapshotElement;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;

/**
 * @author Toni Menzel
 */
@Singleton
public class DefaultSnapshotBuilder implements SnapshotBuilder
{

    final private Store<InputStream> m_store;
    public static final String LINE_DELIM = "|";

    @Inject
    DefaultSnapshotBuilder( Store<InputStream> store )
    {
        m_store = store;
    }

    public Snapshot take( InputStream referenceBuild, File workFolders )
    {
        try
        {
            return new DefaultSnapshot( merge( deflate( referenceBuild ), deflate( workFolders ) ) );

        } catch( IOException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    private List<SnapshotElement> merge( Map<String, Handle> reference, Map<String, Handle> dynamic )
        throws IOException
    {
        List<SnapshotElement> res = new ArrayList<SnapshotElement>();
        for( String name : reference.keySet() )
        {
            if( dynamic.containsKey( name ) )
            {
                res.add( newSnapshotElement( name, reference.get( name ), QType.OWN ) );
                dynamic.remove( name );
            }
            else
            {
                res.add( newSnapshotElement( name, reference.get( name ), QType.IMPORTED ) );
            }
        }

        // content of dynamic that does not end up in artifact (directly
        for( String name : dynamic.keySet() )
        {
            res.add( newSnapshotElement( name, dynamic.get( name ), QType.EXCLUDED ) );
        }

        return res;
    }

    /**
     * Create a new SnapshotElement instance of name, handle and type.
     * Helper method.
     *
     * @param name   name identifier of a snapshot entry
     * @param handle store handle
     * @param type   the type
     *
     * @return new instance of type {@link SnapshotElement}
     *
     * @throws IOException in case something goes wrong when dealing with you supplied handle
     */
    private SnapshotElement newSnapshotElement( String name, Handle handle, QType type )
        throws IOException
    {
        return new DefaultSnapshotElement( name, m_store.getLocation( handle ), type, handle.getIdentification() );
    }

    private Map<String, Handle> deflate( File folder )
        throws IOException
    {
        Map<String, Handle> content = new HashMap<String, Handle>();
        defl( content, folder.getCanonicalPath(), folder );
        return content;
    }

    private void defl( Map<String, Handle> content, String basePath, File folder )
        throws IOException
    {
        for( File f : folder.listFiles() )
        {
            if( !f.isHidden() && f.isDirectory() )
            {
                defl( content, basePath, f );

            }
            else if( !f.isHidden() && f.getName().endsWith( ".class" ) )
            {

                String p = f.getCanonicalPath().replaceAll( "\\\\", "/" );
                // cut prefix
                p = p.substring( basePath.length() + 1 );
                content.put( p, m_store.store( new FileInputStream( f ) ) );
            }
        }
    }

    /**
     * Load Snapshot from disk.
     * Using format produced by {@link org.ops4j.pax.exam.quickbuild.internal.DefaultSnapshot#write}
     *
     * @param load reference to what has been produced by {@link Snapshot#write}
     *
     * @return a snapshot representation of argument load
     *
     * @throws IOException problems with reading and transforming to Snapshot.
     */
    public Snapshot load( InputStream load )
        throws IOException
    {
        final BufferedReader reader = new BufferedReader( new InputStreamReader( load ) );
        final List<SnapshotElement> elements = new ArrayList<SnapshotElement>();
        try
        {
            String line = null;
            while( ( line = reader.readLine() ) != null )
            {
                String[] parts = line.split( "\\" + LINE_DELIM );
                String name = parts[ 0 ];
                String checksum = parts[ 1 ];
                String uri = parts[ 2 ];
                String type = parts[ 3 ];
                elements.add( new DefaultSnapshotElement( name, new URI( uri ), QType.valueOf( type ), checksum ) );
            }
        } catch( URISyntaxException e )
        {
            throw new IOException( "At least one URI is not valid. Check snapshot export", e );
        } finally
        {
            reader.close();
        }
        return new DefaultSnapshot( elements );
    }

    public Map<String, Handle> deflate( InputStream anchor )
        throws IOException
    {
        Map<String, Handle> content = new HashMap<String, Handle>();
        ZipInputStream jin = new ZipInputStream( anchor );
        //while ()

        ZipEntry entry = null;
        while( ( entry = jin.getNextEntry() ) != null )
        {
            Handle handle = m_store.store( jin );
            content.put( entry.getName(), handle );
        }
        return content;
    }
}
