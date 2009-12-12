package org.ops4j.pax.exam.quickbuild.internal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import org.ops4j.pax.exam.quickbuild.Snapshot;
import static org.ops4j.pax.exam.quickbuild.internal.DefaultSnapshotBuilder.LINE_DELIM;

import org.ops4j.pax.exam.quickbuild.SnapshotElement;


/**
 * @author Toni Menzel
 */
public class DefaultSnapshot implements Snapshot
{

    final private List<SnapshotElement> m_elements;
    final private long m_timestamp;

    public DefaultSnapshot( List<SnapshotElement> elements )
    {
        m_elements = elements;
        m_timestamp = System.currentTimeMillis();
    }

    public long timestamp()
    {
        return m_timestamp;
    }

    public void write( OutputStream out )
        throws IOException
    {
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( out ) );
        try
        {
            for( SnapshotElement element : m_elements )
            {
                writer.write( element.name() + LINE_DELIM + element.checksum() + LINE_DELIM + element.reference() + LINE_DELIM + element.type().name() );
                writer.newLine();
            }
        } finally
        {
            writer.close();
        }
    }

    public Iterator<SnapshotElement> iterator()
    {
        return m_elements.iterator();
    }
}
