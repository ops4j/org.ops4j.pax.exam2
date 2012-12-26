package org.ops4j.pax.exam.options;

import static org.ops4j.lang.NullArgumentException.validateNotNull;

import java.net.URL;

import org.ops4j.pax.exam.Option;

/**
 * @author Toni Menzel (tonit)
 * @since Mar 18, 2009
 */
public class MavenPluginGeneratedConfigOption implements Option {

    private URL url;

    public MavenPluginGeneratedConfigOption(URL url) {
        validateNotNull(url, "url");
        this.url = url;
    }

    public URL getURL() {
        return url;
    }

}
