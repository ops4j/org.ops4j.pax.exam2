/*
 * Copyright (c) 2009 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.ops4j.pax.exam.raw.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.ops4j.pax.exam.raw.ProbeCall;
import org.ops4j.pax.exam.raw.TestHandle;
import org.ops4j.pax.exam.raw.TestProbe;
import org.ops4j.pax.exam.raw.TestProbeBuilder;
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

    private List<ProbeCall> m_probeCalls = new ArrayList<ProbeCall>();

    private Class m_anchor;

    public TestProbeBuilder addTest( ProbeCall call )
    {
        m_probeCalls.add( call );
        return this;
    }

    public TestProbeBuilder setAnchor( Class clazz )
    {
        m_anchor = clazz;
        return this;
    }

    public ProbeCall[] getTests()
    {
        return m_probeCalls.toArray( new ProbeCall[m_probeCalls.size()] );
    }

    public InputStream get()
    {

        Properties p = new Properties();
        constructProbeTag( p );

        try
        {
            String tail = m_anchor.getName().replace( ".", "/" ) + ".class";
            File base = new FileTailImpl( new File( "." ), tail ).getParentOfTail();
            return sink( new BundleBuilder( p, new ResourceWriter( base ) ).build() );

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

    private String constructProbeTag( Properties p )
    {
        // construct out of added Tests
        StringBuilder sbKeyChain = new StringBuilder();

        for( ProbeCall call : m_probeCalls )
        {
            sbKeyChain.append( call.signature() );
            sbKeyChain.append( "," );
            p.put( call.signature(), call.getInstruction() );
        }
        p.put( "PaxExam-Executable", sbKeyChain.toString() );
        return sbKeyChain.toString();
    }
}
