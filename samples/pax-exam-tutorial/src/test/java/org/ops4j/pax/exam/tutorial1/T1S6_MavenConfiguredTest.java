package org.ops4j.pax.exam.tutorial1;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.Inject;
//import org.ops4j.pax.exam.junit.MavenConfiguredJUnit4TestRunner;

/**
 * @author Toni Menzel (tonit)
 * @since Mar 30, 2009
 */
//@RunWith( MavenConfiguredJUnit4TestRunner.class )
public class T1S6_MavenConfiguredTest
{

    //@Test
    public void t2estMe( BundleContext context )
    {
        assertNotNull( context );
        System.out.println( "printing bundle states.." );
        for( Bundle b : context.getBundles() )
        {

            System.out.println( "Bundle " + b.getBundleId() + ":" + b.getSymbolicName() + " is " + b.getState() );
        }
    }
}
