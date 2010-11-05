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
package org.ops4j.pax.exam.spi.container;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestTargetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pax Exam runtime.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 09, 2008
 */
public class PaxExamRuntime
{

    private static final Logger LOG = LoggerFactory.getLogger( PaxExamRuntime.class );

    /**
     * Utility class. Ment to be used via the static factory methods.
     */
    private PaxExamRuntime()
    {
        // utility class
    }

    /**
     * Discovers the regression container. Discovery is performed via Appache Commons discovery mechanism.
     *
     * @return discovered regression container
     */
    public static TestContainerFactory getTestContainerFactory()
    {
        sanityCheck( PaxExamRuntime.class.getClassLoader() );
        TestContainerFactory factory = (TestContainerFactory) DiscoverSingleton.find( TestContainerFactory.class );
        LOG.debug( "Found TestContainerFactory: " + ( ( factory != null ) ? factory.getClass().getName() : "<NONE>" ) );
        return factory;
    }

    /**
     * Exits with an exception if Classpath not set up properly.
     */
    private static void sanityCheck( ClassLoader cl )
    {
        try
        {
            List<URL> factories = new ArrayList<URL>();

            Enumeration<URL> systemResources = cl.getSystemResources( "META-INF/services/org.ops4j.pax.exam.TestContainerFactory" );
            while( systemResources.hasMoreElements() )
            {
                factories.add( systemResources.nextElement() );
            }
            if( factories.size() == 0 )
            {
                throw new TestContainerException( "No TestContainer implementation in Classpath.. " );

            }
            else if( factories.size() > 1 )
            {
                for( URL fac : factories )
                {
                    LOG.error( "Ambiquous TestContainer:  " + fac.toExternalForm() );
                }
                throw new TestContainerException( "Too many TestContainer implementations in Classpath.. " );

            }
            else
            {
                // good!
                return;
            }
        } catch( IOException e )
        {
            throw new TestContainerException( "Problem looking for TestContainerFactory descriptors in Classpath.. ", e );
        }
    }

    /**
     * Select yourself
     *
     * @param select the exact implementation if you dont want to rely on commons util discovery or
     *               change different containers in a single project.
     *
     * @return discovered regression container
     */
    public static TestContainerFactory getTestContainerFactory( Class<? extends TestContainerFactory> select )
    {
        try
        {
            return select.newInstance();
        } catch( InstantiationException e )
        {
            throw new IllegalArgumentException( "Class  " + select + "is not a valid Test Container Factory.", e );
        } catch( IllegalAccessException e )
        {
            throw new IllegalArgumentException( "Class  " + select + "is not a valid Test Container Factory.", e );
        }
    }

    /**
     * Discovers the regression target. Discovery is performed via Appache Commons discovery mechanism.
     *
     * @return discovered regression target
     */
    public static TestTargetFactory getTestTargetFactory()
    {
        LOG.info( "Pax Exam Runtime: looking for a " + TestTargetFactory.class.getName() );

        return (TestTargetFactory) DiscoverSingleton.find( TestTargetFactory.class );
    }

}
