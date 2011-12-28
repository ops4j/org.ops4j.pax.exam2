package org.ops4j.pax.exam.multifw;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.profile;
import static org.ops4j.pax.exam.regression.nat.RegressionConfiguration.regressionDefaults;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.testforge.CountBundles;
import org.osgi.framework.BundleContext;

/**
 *
 */
@RunWith( JUnit4TestRunner.class )
public class JUnitMultiFwTest {

    @Inject
    private BundleContext ctx;
    
    @Configuration()
    public Option[] config()
    {
        return options( profile( "web" ),
                        regressionDefaults(),
                        junitBundles(),
                        mavenBundle().groupId( "org.ops4j.pax.tinybundles" ).artifactId( "tinybundles" ).version( "1.0.0" ),
                        frameworkStartLevel( 100 )
        );
    }

    @Test
    public void withBC( )
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
