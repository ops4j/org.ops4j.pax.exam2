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
package org.ops4j.pax.exam.spi.probesupport.intern;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.probesupport.ContentCollector;
import org.ops4j.store.Store;

/**
 * Default implementation allows you to dynamically create a probe from current classpath.
 *
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public class TestProbeBuilderImpl implements TestProbeBuilder {

    private static Logger LOG = LoggerFactory.getLogger( TestProbeBuilderImpl.class );

    private Map<TestAddress, TestInstantiationInstruction> m_probeCalls = new HashMap<TestAddress, TestInstantiationInstruction>();

    private final List<Class> m_anchors;
    private final Properties m_extraProperties;
    private final Set<String> m_ignorePackages = new HashSet<String>();
    private final Store<InputStream> m_store;

    public TestProbeBuilderImpl( Properties p, Store<InputStream> store )
        throws IOException
    {
        m_anchors = new ArrayList<Class>();
        m_store = store;
        m_extraProperties = p;
    }

    public TestAddress addTest( Class clazz, Method m )
    {
        DefaultTestAddress address = new DefaultTestAddress( m.getName() );
        m_probeCalls.put( address, new TestInstantiationInstruction( clazz.getName() + ";" + m.getName() ) );
        addAnchor( clazz );
        return address;
    }

    public List<TestAddress> addTests( Class clazz, Method... methods )
    {
        List<TestAddress> list = new ArrayList<TestAddress>();
        for( Method method : methods ) {
            list.add( addTest( clazz, method ) );
        }
        return list;
    }

    public TestProbeBuilder addAnchor( Class clazz )
    {
        m_anchors.add( clazz );
        return this;
    }

    public TestProbeBuilder setHeader( String key, String value )
    {
        m_extraProperties.put( key, value );
        return this;
    }

    // when your testclass contains clutter in non-test methods,
    // bnd generates too many impports.
    // This makes packages optional.
    public TestProbeBuilder ignorePackageOf( Class... classes )
    {
        for( Class c : classes ) {
            m_ignorePackages.add( c.getPackage().getName() );
        }

        return this;
    }

    public TestProbeProvider build()
    {
        if( m_anchors.size() == 0 ) {
            throw new TestContainerException( "No tests added to setup!" );
        }

        constructProbeTag( m_extraProperties );
        Properties p = createExtraIgnores();

        try {

            ContentCollector collector = selectCollector( new File( "." ) );

            BundleBuilder bundleBuilder = new BundleBuilder( new DefaultResourceWriter( collector ), m_extraProperties, p );

            return new DefaultTestProbeProvider(
                getTests(),
                m_store,
                m_store.store( bundleBuilder.build() )
            );

        } catch( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    private ContentCollector selectCollector( File dir )
        throws IOException
    {
        validateTopLevelDir( dir );

        ContentCollector collector = null;

        File root = new ClassSourceFolder( dir ).find( m_anchors.get( 0 ) );
        if( root != null ) {
            collector = new CollectFromBase( root );
        }
        else {
            collector = new CollectFromItems( m_anchors );
        }
        return collector;
    }

    private void validateTopLevelDir( File f )
    {
        if( !f.exists() || !f.canRead() || !f.isDirectory() ) {
            throw new IllegalArgumentException( "topLevelDir " + f.getAbsolutePath() + " is not a readable folder" );
        }
        LOG.debug( "Top level dir " + f + " has been verified." );
    }

    private TestAddress[] getTests()
    {
        return m_probeCalls.keySet().toArray( new TestAddress[ m_probeCalls.size() ] );
    }

    private Properties createExtraIgnores()
    {
        Properties extraProperties = new Properties();
        StringBuilder sb = new StringBuilder();
        for( String p : m_ignorePackages ) {
            if( sb.length() > 0 ) {
                sb.append( "," );
            }
            sb.append( p );
        }
        extraProperties.put( "Ignore-Package", sb.toString() );
        return extraProperties;
    }

    private void constructProbeTag( Properties p )
    {
        // construct out of added Tests
        StringBuilder sbKeyChain = new StringBuilder();

        for( TestAddress address : m_probeCalls.keySet() ) {
            sbKeyChain.append( address.identifier() );
            sbKeyChain.append( "," );
            p.put( address.identifier(), m_probeCalls.get( address ).toString() );
        }
        p.put( "PaxExam-Executable", sbKeyChain.toString() );
    }

    /**
     * parse regression methods using reflection
     *
     * @param clazz
     */
    private List<TestInstantiationInstruction> parseMethods( Class clazz )
    {
        List<TestInstantiationInstruction> calls = new ArrayList<TestInstantiationInstruction>();

        for( Method m : clazz.getDeclaredMethods() ) {
            if( Modifier.isPublic( m.getModifiers() ) ) {
                calls.add( new TestInstantiationInstruction( clazz + ";" + m ) );
            }
            else {
                LOG.debug( "Skipping " + clazz.getName() + " Method " + m.getName() + " (not public)" );
            }
        }
        return calls;
    }
}
