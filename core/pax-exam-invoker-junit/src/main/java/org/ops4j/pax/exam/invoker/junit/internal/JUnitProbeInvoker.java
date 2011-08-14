/*
 * Copyright 2011 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.invoker.junit.internal;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.ServiceLookup;
import org.osgi.framework.BundleContext;

/**
 * Turns a instruction into a service call. Currently used with encoded instructions from
 * org.ops4j.pax.exam.spi.container.ClassMethodTestAddress
 * 
 * @author Harald Wellmann
 * @since 2.3.0, August 2011
 */
public class JUnitProbeInvoker implements ProbeInvoker
{

    private BundleContext m_ctx;
    private String m_clazz;
    private String m_method;

    private Injector m_injector;

    public JUnitProbeInvoker( String encodedInstruction, BundleContext bundleContext )
    {
        // parse class and method out of expression:
        String[] parts = encodedInstruction.split( ";" );
        m_clazz = parts[0];
        m_method = parts[1];
        m_ctx = bundleContext;

        // acquire (optional) injector
        // TODO replace system property by core configuration option
        boolean inject = "true".equals( System.getProperty( "pax.exam.inject" ) );
        if( inject )
        {
            m_injector = ServiceLookup.getService( m_ctx, Injector.class );
        }
        else
        {
            // m_injector = new NoOpInjector();
        }
    }

    public void call( Object... args )
    {
        Class<?> testClass;
        try
        {
            testClass = m_ctx.getBundle().loadClass( m_clazz );
        }
        catch ( ClassNotFoundException e )
        {
            throw new TestContainerException( e );
        }

        if( !( findAndInvoke( testClass ) ) )
        {
            throw new TestContainerException( " Test " + m_method + " not found in test class " + testClass.getName() );
        }
    }

    private boolean findAndInvoke( Class<?> testClass )

    {
        try
        {
            // find matching method
            for ( Method m : testClass.getMethods() )
            {
                if( m.getName().equals( m_method ) )
                {
                    // we assume its correct:
                    invokeViaJUnit( testClass, m );
                    return true;
                }
            }
        }
        catch ( NoClassDefFoundError e )
        {
            throw new TestContainerException( e );
        }
        return false;
    }

    private void invokeViaJUnit( final Class<?> testClass, final Method testMethod )
        throws TestContainerException
    {
        Request classRequest = new ContainerTestRunnerClassRequest( testClass, m_ctx, m_injector );
        Description method = Description.createTestDescription( testClass, m_method );
        Request request = classRequest.filterWith( method );
        JUnitCore junit = new JUnitCore();
        Result result = junit.run( request );
        List<Failure> failures = result.getFailures();
        if( !failures.isEmpty() )
        {
            throw new TestContainerException( failures.toString() );
        }
    }
}
