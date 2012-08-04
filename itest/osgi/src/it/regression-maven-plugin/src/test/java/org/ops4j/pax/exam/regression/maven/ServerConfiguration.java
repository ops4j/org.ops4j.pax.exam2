package org.ops4j.pax.exam.regression.maven;

import static org.ops4j.pax.exam.CoreOptions.*;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;

public class ServerConfiguration
{

    @Configuration
    public Option[] configuration() {
        return options(
            frameworkProperty( "osgi.console" ).value( "6666" ),
            mavenBundle("org.ops4j.pax.exam", "regression-pde-bundle", "3.0.0-SNAPSHOT"));
    }
}
