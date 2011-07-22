package org.ops4j.pax.exam.multifw;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.testforge.CountBundles;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;

/**
 *
 */
@RunWith( JUnit4TestRunner.class )
public class JUnitMultiFwTest {

    @Configuration()
    public Option[] config()
    {
        return options( profile( "web" ),
                        junitBundles(),
                        mavenBundle().groupId( "org.ops4j.pax.tinybundles" ).artifactId( "tinybundles" ).version( "1.0.0-SNAPSHOT" ),
                        frameworkStartLevel( 100 )
        );
    }

    @Test
    public void withBC( BundleContext ctx )
    {
        assertThat( ctx, is( notNullValue() ) );
        System.out.println( "BundleContext of bundle injected: " + ctx.getBundle().getSymbolicName() );
        for ( org.osgi.framework.Bundle b : ctx.getBundles()) {
            System.out.println("+ " + b.getBundleId() + ", name=" + b.getSymbolicName() + " in State " + b.getState() );
        }

    }

    @Test
    public void without()
    {
        System.out.println( "------- HERE!" );
    }

    //@Test
    public TestAddress prebuilt( TestProbeBuilder builder )
    {
        return builder.addTest( CountBundles.class, 7 );
    }
}
