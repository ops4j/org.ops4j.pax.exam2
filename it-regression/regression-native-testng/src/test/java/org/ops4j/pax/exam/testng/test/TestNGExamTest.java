package org.ops4j.pax.exam.testng.test;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.testng.Assert.*;

import javax.inject.Inject;

import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.testng.Configuration;
import org.osgi.framework.BundleContext;
import org.testng.annotations.Test;

public class TestNGExamTest
{
    @Inject
    private BundleContext bc;
    
    @Configuration
    public Option[] config()
    {
        return options(
            
            /*
             * The TestNG 6.2 bundle does not resolve due to an incorrect
             * package version for Guice. The 6.3-SNAPSHOT used here is built
             * from https://github.com/hwellmann/testng.
             * A pull request is pending.
             */
            
            mavenBundle( "org.testng", "testng", "6.3-SNAPSHOT" ),
            
            /*
             * The following are optional direct and transitive dependencies
             * of TestNG which do not seem to be required unless you want
             * to use command line parameters, Guice injection or YAML suites.
             */
            
            //mavenBundle( "com.beust", "jcommander", "1.12" ),
            //mavenBundle( "org.beanshell", "com.springsource.bsh", "2.0.0.b4" ),
            //mavenBundle("org.apache.geronimo.specs", "geronimo-servlet_2.5_spec", "1.2"),
            //mavenBundle("com.google.inject", "guice", "2.0"),
            //wrappedBundle(mavenBundle("org.yaml", "snakeyaml", "1.6")),

            mavenBundle( "org.apache.geronimo.specs", "geronimo-atinject_1.0_spec", "1.0" ),
            mavenBundle( "org.ops4j.pax.exam", "pax-exam-inject", Info.getPaxExamVersion() ),
            systemProperty( "pax.exam.inject" ).value( "true" ),
            systemProperty( "osgi.console" ).value( "6666" ) );
    }

    @Test
    public void helloTestNG()
    {
        assertNotNull(bc);
        System.out.println( "Hello TestNG!" );
    }

    @Test
    public void helloPaxExam()
    {
        System.out.println( "Hello Pax Exam!" );
    }
}
