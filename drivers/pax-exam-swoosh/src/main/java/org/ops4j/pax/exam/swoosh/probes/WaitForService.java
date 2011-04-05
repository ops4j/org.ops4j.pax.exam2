package org.ops4j.pax.exam.swoosh.probes;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.ops4j.pax.exam.TestContainerException;

/**
 * Set
 */
public class WaitForService {

    /**
     * @param ctx         Injected BundleContext
     * @param servicename Parameter
     * @param wait        waiting length (may be omitted)
     */
    public void probe( BundleContext ctx, String servicename, Integer wait )
        throws InterruptedException
    {
        if( wait == null ) { wait = 1000; }

        ServiceTracker tracker = new ServiceTracker( ctx, servicename, null );
        tracker.open();

        long start = System.currentTimeMillis();

        Object ref = null;
        while( ( ref = ( tracker.getService() ) ) == null && ( start + wait > System.currentTimeMillis() ) ) {
            Thread.sleep( 100 );
        }
        tracker.close();
        if( ref == null ) {
            throw new TestContainerException( "Service " + servicename + " has not been available in time." );
        }
    }
}
