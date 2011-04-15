package org.ops4j.pax.exam.multifw;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

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

    private void neverCall()
    {
        fail( "Don't parseForTests me !" );
    }
}
