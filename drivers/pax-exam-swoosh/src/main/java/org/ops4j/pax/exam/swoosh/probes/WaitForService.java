package org.ops4j.pax.exam.swoosh.probes;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Set
 */
public class WaitForService {

    /**
     * @param ctx         Injected BundleContext
     * @param servicename Parameter
     */
    public void probe( BundleContext ctx, String servicename )
    {
        System.out.println( "Service: " + servicename );
        for( Bundle b : ctx.getBundles() ) {
            System.out.println( "b: " + b.getSymbolicName() );
            // test funky things
        }
    }
}
