package org.ops4j.pax.exam.options;

import java.net.URL;
import static org.ops4j.lang.NullArgumentException.*;
import org.ops4j.pax.exam.Option;

/**
 * @author Toni Menzel (tonit)
 * @since Mar 18, 2009
 */
public class MavenPluginGeneratedConfigOption implements Option
{

    private URL m_url;

    public MavenPluginGeneratedConfigOption( URL url )

    {
        validateNotNull( url, "url" );
        m_url = url;
    }

    public URL getURL()
    {
        return m_url;
    }

}
