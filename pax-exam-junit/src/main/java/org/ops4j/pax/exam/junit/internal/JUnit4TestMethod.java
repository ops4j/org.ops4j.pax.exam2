/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.ops4j.pax.exam.junit.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.internal.runners.TestClass;
import org.junit.internal.runners.TestMethod;

import static org.ops4j.lang.NullArgumentException.*;
import static org.ops4j.pax.exam.Constants.*;

import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.junit.extender.CallableTestMethod;
import org.ops4j.pax.exam.junit.extender.Constants;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.exam.runtime.PaxExamRuntime;
import org.ops4j.pax.exam.spi.container.TestContainer;
import org.ops4j.pax.exam.spi.container.TestContainerFactory;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;

/**
 * A {@link TestMethod} that upon invokation starts a {@link TestContainer} and executes the test in the test container.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0 December 16, 2008
 */
public class JUnit4TestMethod
    extends TestMethod
{

    /**
     * Test flow not yet started.
     */
    private static final int NOT_STARTED = 0;

    /**
     * Test container started.
     */
    private static final int CONTAINER_STARTED = 1;

    /**
     * Test bundle installed.
     */
    private static final int PROBE_INSTALLED = 2;

    /**
     * Test bundle started.
     */
    private static final int PROBE_STARTED = 3;

    /**
     * Test flow ended
     */
    private static final int SUCCESFUL = 4;

    /**
     * JCL logger.
     */
    private static final Log LOG = LogFactory.getLog( JUnit4TestMethod.class );

    /**
     * Test method. Cannot reuse the one from super class as it is not public.
     */
    private final Method m_testMethod;

    /**
     * Configuration options.
     */
    private final Option[] m_options;

    /**
     * Configuration method name (test method name and eventual the framework and framework version)
     */
    private final String m_name;

    /**
     *
     */
    private final Store<InputStream> m_store;
    private final Handle m_probe;

    /**
     * Constructor.
     *
     * @param testMethod      test method (cannot be null)
     * @param testClass       test class (cannot be null)
     * @param frameworkOption framework option (on which framework the test method should be run) (can be null = default
     *                        framework)
     * @param userOptions     user options (can be null)
     */
    public JUnit4TestMethod( final Method testMethod,
                             final TestClass testClass,
                             final FrameworkOption frameworkOption,
                             final Option... userOptions )
    {
        super( testMethod, testClass );
        validateNotNull( testMethod, "Test method" );
        validateNotNull( testClass, "Test class" );

        m_testMethod = testMethod;
        m_options = OptionUtils.combine( userOptions, frameworkOption );
        m_name = calculateName( testMethod.getName(), frameworkOption );
        try
        {
            m_store = StoreFactory.anonymousStore();
            m_probe = m_store.store( new URL( getTestBundleUrl( testClass.getName(), m_testMethod.getName() ) ).openStream() );
        } catch( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * {@inheritDoc} Starts the test container, installs the test bundle and executes the test within the container.
     */
    @Override
    public void invoke( Object test )
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        Info.showLogo();

        final String fullTestName = m_name + "(" + m_testMethod.getDeclaringClass().getName() + ")";
        LOG.info( "Starting test " + fullTestName );

        int executionState = NOT_STARTED;
        final TestContainerFactory containerFactory = PaxExamRuntime.getTestContainerFactory();
        TestContainer container = null;
        try
        {
            LOG.trace( "Start test container" );
            container = containerFactory.newInstance( m_options );
            container.start();
            executionState = CONTAINER_STARTED;

            LOG.trace( "Install and start test bundle" );
            final long bundleId = container.installBundle( m_store.load( m_probe ) );
            executionState = PROBE_INSTALLED;

            container.setBundleStartLevel( bundleId, START_LEVEL_TEST_BUNDLE );

            executionState = PROBE_STARTED;

            LOG.trace( "Execute test [" + m_name + "]" );
            // we just have one callable if using (old) junit extender
            final CallableTestMethod callable = container.getService( CallableTestMethod.class, null, 0 );
            try
            {
                LOG.info( "Starting test " + fullTestName );
                callable.call();
                LOG.info( "Test " + fullTestName + " ended succesfully" );
                executionState = SUCCESFUL;
            }
            catch( InstantiationException e )
            {
                throw new InvocationTargetException( e );
            }
            catch( ClassNotFoundException e )
            {
                throw new InvocationTargetException( e );
            }
        }
        catch( IOException e )
        {
            e.printStackTrace();
        } finally
        {
            if( container != null )
            {
                // Leave handling of proper stop to container implementation
                try
                {
                    container.stop();
                }
                catch( RuntimeException ignore )
                {
                    if( executionState >= SUCCESFUL )
                    {
                        // throw catched exception if the test already was successful
                        // noinspection ThrowFromFinallyBlock
                        throw ignore;
                    }
                    else
                    {
                        // Do not throw an exception that could occur during stopping the container in case that an
                        // exception was already being thrown
                        LOG.error( "Cannot stop the test container: " + ignore.getMessage() );
                    }
                }
            }
        }
    }

    /**
     * Getter.
     *
     * @return test method
     */
    public Method getTestMethod()
    {
        return m_testMethod;
    }

    /**
     * Getter.
     *
     * @return test method name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Computes the test method name out of test method name, framework and framework version.
     *
     * @param testMethodName  test method name
     * @param frameworkOption framework option
     *
     * @return test method name
     */
    private static String calculateName( final String testMethodName,
                                         final FrameworkOption frameworkOption )
    {
        final StringBuilder name = new StringBuilder();
        name.append( testMethodName );
        if( frameworkOption != null )
        {
            name.append( " [" ).append( frameworkOption.getName() );
            final String version = frameworkOption.getVersion();
            if( version != null )
            {
                name.append( "/" ).append( version );
            }
            name.append( "]" );
        }
        return name.toString();
    }

    /**
     * Returns the test bundle url using an Pax URL Dir url.
     *
     * @param testClassName  test class name
     * @param testMethodName test method name
     *
     * @return test bundle url
     */
    private static String getTestBundleUrl( final String testClassName,
                                            final String testMethodName )
    {
        final StringBuilder url = new StringBuilder();
        url.append( "dir:" )
            .append( new File( "." ).getAbsolutePath() )
            .append( "$" )
            .append( "tail=" ).append( testClassName.replace( ".", "/" ) ).append( ".class" )
            .append( "&" )
            .append( Constants.PROBE_TEST_CLASS ).append( "=" ).append( testClassName )
            .append( "&" )
            .append( Constants.PROBE_TEST_METHOD ).append( "=" ).append( testMethodName )
            .append( "&" )
            .append( org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME )
            .append( "=" )
            .append( Constants.PROBE_SYMBOLICNAME )
            .append( "&" )
            .append( org.osgi.framework.Constants.DYNAMICIMPORT_PACKAGE )
            .append( "=*" )
            .append( "&" )
            .append( org.osgi.framework.Constants.EXPORT_PACKAGE )
            .append( "=!*" );
        return url.toString();
    }

}
