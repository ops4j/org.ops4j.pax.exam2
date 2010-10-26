/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.raw.extender.intern;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.raw.extender.ProbeInvoker;

/**
 * Turns a instruction into a service call.
 * Currently used with encoded instructions from org.ops4j.pax.exam.spi.container.ClassMethodTestAddress
 *
 * @author Toni Menzel
 * @since Dec 4, 2009
 */
public class ProbeInvokerImpl implements ProbeInvoker
{

    private BundleContext m_ctx;
    private String m_clazz;
    private String m_method;

    public ProbeInvokerImpl( String encodedInstruction, BundleContext bundleContext )
    {
        // parse class and method out of expression:
        String[] parts = encodedInstruction.split( ";" );
        m_clazz = parts[ 0 ];
        m_method = parts[ 1 ];
        m_ctx = bundleContext;
    }

    public void call()
        throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        final Class testClass = m_ctx.getBundle().loadClass( m_clazz );

        if( !( findAndInvoke( testClass ) || findAndInvoke( testClass, BundleContext.class ) ) )
        {
            throw new RuntimeException( " test " + m_method + " not found in test class " + testClass.getName() );
        }

        //iteratingSearcher( testClass, encountered );
    }

    private void iteratingSearcher( Class testClass, int encountered )
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        for( final Method testMethod : testClass.getMethods() )
        {
            if( testMethod.getName().equals( m_method ) )
            {
                injectContextAndInvoke( testClass.newInstance(), testMethod );
                encountered++;
            }
        }
        if( encountered == 0 )
        {
            throw new RuntimeException( " test " + m_method + " not found in test class " + testClass.getName() );
        }
    }

    private boolean findAndInvoke( Class testClass, Class<?>... params )
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        try
        {
            Method meth = testClass.getMethod( m_method, params );
            if( meth != null )
            {
                injectContextAndInvoke( testClass.newInstance(), meth );
                return true;
            }
        } catch( NoSuchMethodException e )
        {
            //
        } catch( NoClassDefFoundError e )
        {
            //
        }
        return false;
    }

    /**
     * Invokes the bundle context (if possible and required) and executes the test method.
     *
     * TODO this is a trimmed down minimal version that does not support any junit before/afters or
     * self made injection.
     * The only thing you get here is a parameter injection for BundleContext types.
     *
     * @param testInstance an instance of the test class
     * @param testMethod   test method
     *
     * @throws IllegalAccessException    - Re-thrown from reflection invokation
     * @throws InvocationTargetException - Re-thrown from reflection invokation
     */
    private void injectContextAndInvoke( final Object testInstance,
                                         final Method testMethod )
        throws IllegalAccessException, InvocationTargetException
    {
        final Class<?>[] paramTypes = testMethod.getParameterTypes();
        //injectFieldInstances( testInstance.getClass(), testInstance );
        boolean cleanup = false;
        try
        {
            //runBefores( testInstance );
            // if there is only one param and is of type BundleContext we inject it, otherwise just call
            // this means that if there are actual params the call will fail, but that is okay as it will be reported back
            if( paramTypes.length == 1
                && paramTypes[ 0 ].isAssignableFrom( BundleContext.class ) )
            {
                testMethod.invoke( testInstance, m_ctx );
            }
            else
            {
                testMethod.invoke( testInstance );
            }
            cleanup = true;
            //runAfters( testInstance );
        } finally
        {
            if( !cleanup )
            {
                try
                {
                    //runAfters( testInstance );
                } catch( Throwable throwable )
                {
                    //LOG.warn( "Got the exception when calling the runAfters. [Exception]: " + throwable );
                }
            }
        }
    }
}
