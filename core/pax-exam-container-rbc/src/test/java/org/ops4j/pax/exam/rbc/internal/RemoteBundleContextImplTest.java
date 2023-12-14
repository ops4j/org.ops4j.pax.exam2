package org.ops4j.pax.exam.rbc.internal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ops4j.pax.exam.RelativeTimeout;
import org.osgi.framework.BundleContext;

/**
 * Test cases for RemoteBundleContextImpl.
 * 
 * @author Alex Ellwein
 * 
 */
public class RemoteBundleContextImplTest {

    @Test
    public void testfilterWasUsedForProbeInvoker() throws Exception {
        
        BundleContext bundleContext = mock(BundleContext.class);
        RemoteBundleContextImpl remoteBundleContext = new RemoteBundleContextImpl(bundleContext);

        String rightFilter = "(Probe-Signature=PAXPROBE-the-right-one)";

        // return null in order to provoke NPE before entering the service tracker
        when(bundleContext.createFilter(anyString())).thenReturn(null);

        try{
            remoteBundleContext.remoteCall(RemoteBundleContextImplTest.class,
                "filterWasUsedForProbeInvoker", new Class<?>[] {}, rightFilter,
                RelativeTimeout.TIMEOUT_DEFAULT, new Object[] {});
        }
        catch(NullPointerException e) {
            // we expect it actually, since we return a mock filter and don't
            // want to wait in the tracker.
        }
        verify(bundleContext).createFilter(contains(rightFilter));
    }
}
