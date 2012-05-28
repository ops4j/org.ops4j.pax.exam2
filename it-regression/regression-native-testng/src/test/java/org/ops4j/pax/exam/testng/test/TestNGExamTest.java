package org.ops4j.pax.exam.testng.test;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.testng.Assert.assertNotNull;

import javax.inject.Inject;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.testng.listener.ExamTestNGListener;
import org.osgi.framework.BundleContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners({ExamTestNGListener.class})
public class TestNGExamTest
{
    @Inject
    private BundleContext bc;
    
    @Configuration
    public Option[] config()
    {
        return options(
            
            mavenBundle( "org.testng", "testng", "6.3.1" ),
            
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
