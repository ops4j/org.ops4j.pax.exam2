package org.ops4j.pax.exam.regression.plumbing;

import static org.junit.Assert.fail;

import org.osgi.framework.BundleContext;

/**
 * Created by IntelliJ IDEA.
 * User: tonit
 * Date: 3/11/11
 * Time: 9:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class Probe {

    public void probe()
    {
    }

    public void withBCTest( BundleContext ctx )
    {
    }

    @SuppressWarnings( "unused" )
    private void neverCall()
    {
        fail( "Don't parseForTests me !" );
    }
}
