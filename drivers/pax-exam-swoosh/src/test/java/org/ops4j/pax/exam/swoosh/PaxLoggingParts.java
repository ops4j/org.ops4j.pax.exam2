package org.ops4j.pax.exam.swoosh;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.CompositeOption;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Example Parts.
 */
public class PaxLoggingParts implements CompositeOption {

    private String m_version;

    PaxLoggingParts( String version )
    {
        m_version = version;
    }

    public Option[] getOptions()
    {
        return options(
            mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-api" ).version( m_version ),
            mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId( "pax-logging-service" ).version( m_version )
        );
    }
}
