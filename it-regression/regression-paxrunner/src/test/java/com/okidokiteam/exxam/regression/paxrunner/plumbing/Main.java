package com.okidokiteam.exxam.regression.paxrunner.plumbing;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.aries.util.tracker.BundleTrackerFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;
import org.ops4j.pax.exam.spi.container.PlumbingContext;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

/**
 * Interactive Pax Exam fun.
 */
public class Main {

    static Logger log = LoggerFactory.getLogger( Main.class.getName() );

    public static void main( String[] args )
        throws Exception
    {
        TestContainer container = PaxExamRuntime.createContainer( options(
            systemProperty( "org.ops4j.pax.logging.DefaultServiceLog.level" ).value( "WARN" ),
          //  mavenBundle().groupId( "org.ops4j.pax.tinybundles" ).artifactId( "pax-tinybundles-core" ).version( "1.0.0-SNAPSHOT" ),
            profile( "gogo" )
        )
        );
        container.start();

        //container.install( bundle( withBnd() ).add( Probe2.class ).set( "Bundle-Activator", Probe2.class.getName() ).build() );

        TestProbeBuilder probe = new PlumbingContext().createProbe();
        probe.addTest( Main.class, "test" );
        TestProbeProvider p = probe.build();

        container.install( p.getStream() );

        for( TestAddress t : p.getTests() ) {
            container.call( t );
        }
 
    }

    public void test( BundleContext ctx )
        throws IOException, BundleException, InterruptedException
    {
        log.trace( "I AM A TRACE MESSAGE" );
        log.debug( "I AM A DEBUG MESSAGE" );
        log.info( "I AM AN INFO MESSAGE" );
        log.warn( "I AM A WARN MESSAGE" );
        log.error( "I AM AN ERROR MESSAGE" );

    }

    public void test2()
    {
        log.warn( "You and me2." );
    }
}
