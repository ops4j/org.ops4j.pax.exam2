package $packageName;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * @author Toni Menzel (tonit)
 * @since Mar 3, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class SampleTest
{

    @Inject
    BundleContext bundleContext = null;

    /**
     * You will get a list of bundles installed by default
     * plus your testcase, wrapped into a bundle called pax-exam-probe
     */
    @Test
    public void listBundles()
    {
        for( Bundle b : bundleContext.getBundles() )
        {
            System.out.println( "Bundle " + b.getBundleId() + " : " + b.getSymbolicName() );
        }

    }
}