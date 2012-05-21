package org.ops4j.pax.exam.spi.intern;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.ops4j.pax.exam.spi.ContentCollector;

/**
 *
 */
public class CollectFromItems implements ContentCollector {

    final private List<Class> m_items;

    public CollectFromItems( List<Class> items )
    {
        m_items = items;
    }

    public void collect( Map<String, URL> map )
        throws IOException
    {
        for( Class s : m_items ) {
            String name = convert( s );
            map.put( name, s.getResource( "/" + name ) );
        }
    }

    private String convert( Class c )
    {
        return c.getName().replace( ".", "/" ) + ".class";

    }

}
