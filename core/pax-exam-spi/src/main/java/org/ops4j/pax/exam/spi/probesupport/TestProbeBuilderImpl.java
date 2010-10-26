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
package org.ops4j.pax.exam.spi.probesupport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.spi.container.DefaultRaw;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;

/**
 * Default implementation allows you to dynamically create a probe from current classpath.
 *
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public class TestProbeBuilderImpl implements TestProbeBuilder
{

    private List<TestAddress> m_probeCalls = new ArrayList<TestAddress>();

    private Class m_anchor;
    private Properties m_extraProperties;

    public TestProbeBuilderImpl( Properties p )
    {
        m_extraProperties = p;
    }

    public TestProbeBuilder addTest( TestAddress... calls )
    {
        m_probeCalls.addAll( Arrays.asList( calls ) );

        return this;
    }

    public TestProbeBuilder addTest( Class... calls )
    {
        for( Class call : calls )
        {
            addTest( DefaultRaw.call( call ) );
            setAnchor( call );
        }
        return this;
    }

    public TestProbeBuilder setAnchor( Class clazz )
    {
        m_anchor = clazz;
        return this;
    }

    public TestProbeBuilder setHeader( String key, String value )
    {
        return null;
    }

    public TestAddress[] getTests()
    {
        return m_probeCalls.toArray( new TestAddress[ m_probeCalls.size() ] );
    }

    public InputStream getStream()
    {
        constructProbeTag( m_extraProperties );
        try
        {
            String tail = m_anchor.getName().replace( ".", "/" ) + ".class";
            File base = new FileTailImpl( new File( "." ), tail ).getParentOfTail();
            return sink( new BundleBuilder( m_extraProperties, new ResourceWriter( base ) ).build() );

        } catch( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    private InputStream sink( InputStream inputStream )
        throws IOException
    {
        Store<InputStream> store = StoreFactory.anonymousStore();
        return store.load( store.store( inputStream ) );
    }

    private void constructProbeTag( Properties p )
    {
        // construct out of added Tests
        StringBuilder sbKeyChain = new StringBuilder();

        for( TestAddress call : m_probeCalls )
        {
            sbKeyChain.append( call.signature() );
            sbKeyChain.append( "," );
            p.put( call.signature(), call.getInstruction() );
        }
        p.put( "PaxExam-Executable", sbKeyChain.toString() );
    }
}
