/*
 * Copyright 2008,2009 Toni Menzel
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.osgi.framework.BundleContext;

import static org.ops4j.lang.NullArgumentException.*;

import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.junit.extender.CallableTestMethod;

/**
 * {@link Callable} implementation.
 *
 * @author Toni Menzel (tonit)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since May 29, 2008
 */
class CallableTestMethodImpl
    implements CallableTestMethod
{

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog( CallableTestMethodImpl.class );

    /**
     * Bundle context of the bundle containing the test class (cannot be null).
     */
    private BundleContext m_bundleContext;
    /**
     * Test class name (cannot be null or empty).
     */
    private final String m_testClassName;
    /**
     * Test method name (cannot be null or empty).
     */
    private final String m_testMethodName;

    /**
     * Constructor.
     *
     * @param bundleContext  bundle context of the bundle containing the test class (cannot be null)
     * @param testClassName  test class name (cannot be null  or empty)
     * @param testMethodName test method name (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If bundle context is null
     *                                  - If test class name is null or empty
     *                                  - If test method name is null or empty
     */
    CallableTestMethodImpl( final BundleContext bundleContext,
                            final String testClassName,
                            final String testMethodName )
    {
        validateNotNull( bundleContext, "Bundle context" );
        validateNotEmpty( testClassName, true, "Test class name" );
        validateNotEmpty( testMethodName, true, "Test method name" );

        m_bundleContext = bundleContext;
        m_testClassName = testClassName;
        m_testMethodName = testMethodName;
    }

    /**
     * {@inheritDoc}
     */
    public void call()
        throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException
    {
        final Class testClass = m_bundleContext.getBundle().loadClass( m_testClassName );
        int encountered = 0;
        for( final Method testMethod : testClass.getMethods() )
        {
            if( testMethod.getName().equals( m_testMethodName ) )
            {
                injectContextAndInvoke( testClass.newInstance(), testMethod );
                encountered++;
            }
        }
        if( encountered == 0 )
        {
            throw new RuntimeException( " test " + m_testMethodName + " not found in test class " + testClass.getName() );
        }
    }

    /**
     * Invokes the bundle context (if possible and required) and executes the test method.
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
        injectFieldInstances( testInstance.getClass(), testInstance );
        boolean cleanup = false;
        try
        {
            runBefores( testInstance );
            // if there is only one param and is of type BundleContext we inject it, otherwise just call
            // this means that if there are actual params the call will fail, but that is okay as it will be reported back
            if( paramTypes.length == 1
                && paramTypes[ 0 ].isAssignableFrom( BundleContext.class ) )
            {
                testMethod.invoke( testInstance, m_bundleContext );
            }
            else
            {
                testMethod.invoke( testInstance );
            }
            cleanup = true;
            runAfters( testInstance );
        }
        finally
        {
            if( !cleanup )
            {
                try
                {
                    runAfters( testInstance );
                } catch( Throwable throwable )
                {
                    LOG.warn( "Got the exception when calling the runAfters. [Exception]: " + throwable );
                }
            }
        }
    }

    /**
     * Run all methods annotated with {@link Before}.
     *
     * @param testInstance an instance of the test class (cannot be null)
     *
     * @throws IllegalAccessException    - Re-thrown from reflection invokation
     * @throws InvocationTargetException - Re-thrown from reflection invokation
     */
    private void runBefores( final Object testInstance )
        throws IllegalAccessException, InvocationTargetException
    {
        for( final Method beforeMethod : getAnnotatedMethods( testInstance.getClass(), Before.class ) )
        {
            final Class<?>[] paramTypes = beforeMethod.getParameterTypes();
            if( paramTypes.length == 1
                && paramTypes[ 0 ].isAssignableFrom( BundleContext.class ) )
            {
                beforeMethod.invoke( testInstance, m_bundleContext );
            }
            else
            {
                beforeMethod.invoke( testInstance );
            }
        }
    }

    /**
     * Run all methods annotated with {@link After}.
     *
     * @param testInstance an instance of the test class (cannot be null)
     *
     * @throws IllegalAccessException    - Re-thrown from reflection invokation
     * @throws InvocationTargetException - Re-thrown from reflection invokation
     */
    private void runAfters( final Object testInstance )
        throws IllegalAccessException, InvocationTargetException
    {
        for( final Method afterMethod : getAnnotatedMethods( testInstance.getClass(), After.class ) )
        {
            final Class<?>[] paramTypes = afterMethod.getParameterTypes();
            if( paramTypes.length == 1
                && paramTypes[ 0 ].isAssignableFrom( BundleContext.class ) )
            {
                afterMethod.invoke( testInstance, m_bundleContext );
            }
            else
            {
                afterMethod.invoke( testInstance );
            }
        }
    }

    /**
     * Injects instances into fields found in testInstance and its superclases.
     *
     * @param clazz
     * @param inst
     */
    private void injectFieldInstances( Class clazz, Object inst )
        throws IllegalAccessException
    {
        if( clazz.getSuperclass() != null )
        {
            injectFieldInstances( clazz.getSuperclass(), inst );
        }
        for( Field field : clazz.getDeclaredFields() )
        {
            setIfMatching( inst, field, m_bundleContext );
        }
    }

    /**
     * @param testInstance object instance where you found field
     * @param field        field that is going to be set
     * @param o            target value of field
     */
    private void setIfMatching( Object testInstance, Field field, Object o )
        throws IllegalAccessException
    {
        if( isInjectionField( field ) && isMatchingType( field, o.getClass() ) )
        {
            field.setAccessible( true );
            field.set( testInstance, o );
        }
    }

    /**
     * Just checks if type of field is a assignable from clazz.
     *
     * @param field
     * @param clazz
     */
    private boolean isMatchingType( Field field, Class clazz )
    {
        return field.getType().isAssignableFrom( clazz );
    }

    /**
     * Tests if the given field has the {@link @Inject} annotation.
     * Due to some osgi quirks, currently direct getAnnotation( Inject.class ) does not work..:(
     *
     * @param field field to be tested
     *
     * @return trze if it has the Inject annotation. Otherwise false.
     */
    public boolean isInjectionField( Field field )
    {
        // Usually, this should be enough.
        if( field.getAnnotation( Inject.class ) != null )
        {
            return true;
        }
        else
        {
            // the above one fails in some cases currently (returns null) while annotation is there.
            // So this is a fallback:
            for( Annotation annot : field.getAnnotations() )
            {
                if( annot.annotationType().getName().equals( Inject.class.getName() ) )
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Find all methods marked with a specific annotation.
     *
     * @param testClass       class to be inspected
     * @param annotationClass annotation class to be found
     *
     * @return list of annotated methods (cannot be null)
     */
    public List<Method> getAnnotatedMethods( final Class testClass,
                                             final Class<? extends Annotation> annotationClass )
    {
        final List<Method> results = new ArrayList<Method>();
        for( final Class<?> clazz : getSuperClasses( testClass ) )
        {
            final Method[] methods = clazz.getDeclaredMethods();
            for( final Method method : methods )
            {
                final Annotation annotation = method.getAnnotation( annotationClass );
                if( annotation != null )
                {
                    results.add( method );
                }
            }
        }
        return results;
    }

    /**
     * Finds all superclasses of a certain class including itself.
     *
     * @param testClass class whom superclasses should be found
     *
     * @return list of superclasses (cannot be null)
     */
    private List<Class<?>> getSuperClasses( final Class<?> testClass )
    {
        final ArrayList<Class<?>> results = new ArrayList<Class<?>>();
        Class<?> current = testClass;
        while( current != null )
        {
            results.add( current );
            current = current.getSuperclass();
        }
        return results;
    }

}
