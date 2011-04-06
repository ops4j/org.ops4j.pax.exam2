package org.ops4j.pax.exam.swoosh.probes;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
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
        throws InterruptedException, InvalidSyntaxException
    {
        if( wait == null ) { wait = 1000; }
        ServiceTracker tracker = new ServiceTracker( ctx, servicename, null );
        tracker.open( true );
        long start = System.currentTimeMillis();

        while( ( ( tracker.getTrackingCount() ) == 0 ) && ( start + wait > System.currentTimeMillis() ) ) {
            Thread.sleep( 100 );
        }
        int c = tracker.getTrackingCount();
        tracker.close();
        if( c == 0 ) {
            throw new TestContainerException( "Service " + servicename + " has not been available in time." );
        }
    }
}
