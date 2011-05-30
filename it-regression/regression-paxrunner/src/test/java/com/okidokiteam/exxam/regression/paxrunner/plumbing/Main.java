package com.okidokiteam.exxam.regression.paxrunner.plumbing;

import java.io.IOException;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import static org.ops4j.pax.exam.spi.container.PaxExamRuntime.*;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Interactive Pax Exam fun.
 */
public class Main {

    static Logger log = LoggerFactory.getLogger( Main.class.getName() );

    public static void main( String[] args )
        throws Exception
    {
    	ExamSystem system = createServerSystem ( options(
                  systemProperty( "org.ops4j.pax.logging.DefaultServiceLog.level" ).value( "WARN" ),
                  mavenBundle().groupId( "org.ops4j.pax.tinybundles" ).artifactId( "pax-tinybundles-core" ).version( "1.0.0-SNAPSHOT" ),
                  profile( "gogo" ),
                  profile("web"),
                  workingDirectory(  "/Users/tonit/server")  
                )         
              );
        TestContainer container = createContainer( system );
        container.start();

        //container.install( bundle( withBnd() ).add( Probe2.class ).set( "Bundle-Activator", Probe2.class.getName() ).build() );
        
 
    }

    public void test( BundleContext ctx )
        throws IOException, BundleException, InterruptedException
    {
        log.trace( "I AM A TRACE MESSAGE" );
        log.debug( "I AM A DEBUG MESSAGE" );
        log.info( "I AM AN INFO MESSAGE" );
        log.warn( "I AM A WARN MESSAGE" );
        log.error( "I AM AN ERROR MESSAGE" );
        log.warn("TONIC:" + ctx.getBundle( 8 ).getLocation());

    }

    public void test2()
    {
        
        log.warn( "You and me2." );
    }
}
