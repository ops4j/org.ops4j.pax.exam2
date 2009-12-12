package org.ops4j.pax.exam.quickbuild.internal;

import java.net.URI;
import org.ops4j.pax.exam.quickbuild.QType;
import org.ops4j.pax.exam.quickbuild.SnapshotElement;

/**
 *
 */
public class DefaultSnapshotElement implements SnapshotElement
{

    final private String m_name;
    final private URI m_reference;
    final private QType m_type;
    final private String m_checksum;

    public DefaultSnapshotElement( String name, URI reference, QType type, String checksum )
    {
        m_name = name;
        m_reference = reference;
        m_type = type;
        m_checksum = checksum;
    }

    public String name()
    {
        return m_name;
    }

    public URI reference()
    {

        return m_reference;
    }

    public String checksum()
    {
        return m_checksum;
    }

    public QType type()
    {
        return m_type;
    }

    
}
