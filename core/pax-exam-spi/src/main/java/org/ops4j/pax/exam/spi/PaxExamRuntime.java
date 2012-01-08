/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2008-2011 Toni Menzel.

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
package org.ops4j.pax.exam.spi;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.serverMode;
import static org.ops4j.pax.exam.CoreOptions.url;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Pax Exam runtime.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 09, 2008
 * 
 */
public class PaxExamRuntime {
	 private static final Logger LOG = LoggerFactory.getLogger( PaxExamRuntime.class );
	 
	/**
     * Discovers the regression container. Discovery is performed via ServiceLoader discovery mechanism.
     *
     * @return discovered test container
     */
    public static TestContainerFactory getTestContainerFactory()
    {
        sanityCheck( );
        TestContainerFactory factory = ServiceLoader.load( TestContainerFactory.class ).iterator().next();
        LOG.debug( "Found TestContainerFactory: " + ( ( factory != null ) ? factory.getClass().getName() : "<NONE>" ) );
        return factory;
    }

    /**
     * Convenience factory when just dealing with one container (intentionally).
     * Note, this will break if there is not exaclty one container available and parsed from options.
     * If there are more containers, just the first (whatever comes first) will be picked.
     *
     * @param system to be used.
     * @return exactly one Test Container.
     */
    public static TestContainer createContainer( ExamSystem system ) {
        return getTestContainerFactory().create( system )[0];
    }

    public static ExamSystem createTestSystem ( Option... options ) throws IOException {
        return DefaultExamSystem.create( OptionUtils.combine(options, defaultTestSystemOptions() ) );
    }
    
    public static ExamSystem createServerSystem ( Option... options ) throws IOException {
        return DefaultExamSystem.create( OptionUtils.combine (options, defaultServerSystemOptions( ) ) );
    }
    
    private static Option[] defaultTestSystemOptions()
    {
        return new Option[] {
                bootDelegationPackage( "sun.*" ),
                frameworkStartLevel( Constants.START_LEVEL_TEST_BUNDLE ),
                url( "link:classpath:META-INF/links/org.ops4j.pax.exam.rbc.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                url( "link:classpath:META-INF/links/org.ops4j.pax.exam.inject.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                url( "link:classpath:META-INF/links/org.ops4j.pax.extender.service.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                url( "link:classpath:META-INF/links/org.osgi.compendium.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                url( "link:classpath:META-INF/links/org.ops4j.pax.logging.api.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                url( "link:classpath:META-INF/links/org.ops4j.base.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                url( "link:classpath:META-INF/links/org.ops4j.pax.swissbox.framework.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES ),
                url( "link:classpath:META-INF/links/org.apache.geronimo.specs.atinject.link" ).startLevel( START_LEVEL_SYSTEM_BUNDLES )
        };
    }
    
    private static Option[] defaultServerSystemOptions()
    {
        return new Option[] {
                bootDelegationPackage( "sun.*" ),
                serverMode( )
            };
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
        try {
            return select.newInstance();
        } catch( InstantiationException e ) {
            throw new IllegalArgumentException( "Class  " + select + "is not a valid Test Container Factory.", e );
        } catch( IllegalAccessException e ) {
            throw new IllegalArgumentException( "Class  " + select + "is not a valid Test Container Factory.", e );
        }
    }
    
    /**
     * Exits with an exception if Classpath not set up properly.
     */
    private static void sanityCheck( )
    {
        List<TestContainerFactory> factories = new ArrayList<TestContainerFactory>();

        Iterator<TestContainerFactory> iter = ServiceLoader.load( TestContainerFactory.class ).iterator();
        while( iter.hasNext() ) {
            factories.add( iter.next() );
        }
        if( factories.size() == 0 ) {
            throw new TestContainerException( "No TestContainer implementation in Classpath" );
        }
        else if( factories.size() > 1 ) {
            for( TestContainerFactory fac : factories ) {
                LOG.error( "Ambiguous TestContainer:  " + fac.getClass().getName() );
            }
            throw new TestContainerException( "Too many TestContainer implementations in Classpath" );
        }
        else {
            // good!
            return;
        }
    }
}
