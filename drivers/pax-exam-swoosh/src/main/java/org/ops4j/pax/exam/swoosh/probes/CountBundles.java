package org.ops4j.pax.exam.swoosh.probes;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.ops4j.pax.exam.TestContainerException;

/**
 *
 */
public class CountBundles {

    public void probe( BundleContext ctx, Integer assume )
        throws InterruptedException, InvalidSyntaxException
    {
        if( assume == null ) { throw new TestContainerException( "Argument assume (integer) is mandatory." ); }
        for (Bundle b: ctx.getBundles()) {
            System.out.println("+ " + b.getBundleId() + " --> " + b.getSymbolicName() + " --> " + b.getState());
        }
        int bundles = ctx.getBundles().length;
        if ( bundles != assume) {
            throw new TestContainerException( "Assumed " + assume + " bundles. But have " + bundles);
        }
    }
}
