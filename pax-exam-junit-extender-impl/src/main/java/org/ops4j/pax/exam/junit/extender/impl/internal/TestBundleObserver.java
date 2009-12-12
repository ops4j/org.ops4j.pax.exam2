/*
 * Copyright 2008 Alin Dreghiciu
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.junit.extender.impl.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ops4j.pax.exam.junit.extender.CallableTestMethod;
import org.ops4j.pax.exam.junit.extender.Constants;
import org.ops4j.pax.swissbox.core.BundleUtils;
import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.ops4j.pax.swissbox.extender.ManifestEntry;

/**
 * Registers/Unregisters test services for test probes.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, Nobember 18, 2008
 */
class TestBundleObserver
    implements BundleObserver<ManifestEntry>
{

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog( TestBundleObserver.class );
    /**
     * Holder for test runner registrations per bundle.
     */
    private final Map<Bundle, Registration> m_registrations;

    /**
     * Constructor.
     */
    TestBundleObserver()
    {
        m_registrations = new HashMap<Bundle, Registration>();
    }

    /**
     * {@inheritDoc}
     * Registers specified test case as a service.
     */
    public void addingEntries( final Bundle bundle,
                               final List<ManifestEntry> manifestEntries )
    {
        String testClassName = null;
        String testMethodName = null;
        for( ManifestEntry manifestEntry : manifestEntries )
        {
            if( Constants.PROBE_TEST_CLASS.equals( manifestEntry.getKey() ) )
            {
                testClassName = manifestEntry.getValue();
            }
            if( Constants.PROBE_TEST_METHOD.equals( manifestEntry.getKey() ) )
            {
                testMethodName = manifestEntry.getValue();
            }
        }
        if( testClassName != null && testMethodName != null )
        {
            LOG.info( "Found test: " + testClassName + "." + testMethodName );
            Dictionary<String, String> props = new Hashtable<String, String>();
            props.put( Constants.TEST_CASE_ATTRIBUTE, testClassName );
            props.put( Constants.TEST_METHOD_ATTRIBUTE, testMethodName );
            final BundleContext bundleContext = BundleUtils.getBundleContext( bundle );
            final ServiceRegistration serviceRegistration = bundleContext.registerService(
                CallableTestMethod.class.getName(),
                new CallableTestMethodImpl( bundleContext, testClassName, testMethodName ),
                props
            );
            m_registrations.put( bundle, new Registration( testClassName, testMethodName, serviceRegistration ) );
            LOG.info( "Registered testcase [" + testClassName + "." + testMethodName + "]" );
        }
    }

    /**
     * {@inheritDoc}
     * Unregisters prior registered test for the service.
     */
    public void removingEntries( final Bundle bundle,
                                 final List<ManifestEntry> manifestEntries )
    {
        final Registration registration = m_registrations.remove( bundle );
        if( registration != null )
        {
            // Do not unregister as bellow, because the services are automatically unregistered as soon as the bundle
            // for which the services are reigistred gets stopped
            // registration.serviceRegistration.unregister();
            LOG.info( "Unregistered testcase [" + registration.testCase + "." + registration.testMethod + "]" );
        }
    }

    /**
     * Registration holder.
     */
    private static class Registration
    {

        final String testCase;
        final String testMethod;
        final ServiceRegistration serviceRegistration;

        public Registration( final String testCase,
                             final String testMethod,
                             final ServiceRegistration serviceRegistration )
        {
            this.testCase = testCase;
            this.testMethod = testMethod;
            this.serviceRegistration = serviceRegistration;
        }
    }

}