package org.ops4j.pax.exam.growl.internal;

import java.util.Hashtable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ops4j.pax.exam.growl.GrowlFactory;
import org.ops4j.pax.exam.growl.GrowlService;

/**
 * Just registers the growl service if possible
 */
public class Activator implements BundleActivator
{

    private ServiceRegistration m_reg;

    public void start( BundleContext bundleContext )
        throws Exception
    {
        m_reg = bundleContext.registerService( GrowlService.class.getName(), GrowlFactory.getService(), new Hashtable() );
    }

    public void stop( BundleContext bundleContext )
        throws Exception
    {
        if( m_reg != null )
        {
            m_reg.unregister();
        }
    }
}
