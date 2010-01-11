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
import java.util.Properties;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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

    private String m_tail = null;
    private final Multimap<String, String> m_map = HashMultimap.create();

    public TestProbeBuilder addTest( Class clazz, String method )
    {
        m_tail = clazz.getName().replace( ".", "/" ) + ".class";
        m_map.put( clazz.getName(), method );
        return this;
    }

    public TestProbe get()
    {
        return new TestProbe()
        {

            public TestHandle[] getTestHandles()
            {
                return new TestHandle[0];
            }

            public InputStream getProbe()
            {
                Properties p = new Properties();
                p.put( "PaxExam-Executable", constructProbeTag() );

                try
                {
                    File base = new FileTailImpl( new File( "." ), m_tail ).getParentOfTail();
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

            private String constructProbeTag()
            {
                // construct out of added Tests
                StringBuilder sb = new StringBuilder();

                for( String clazz : m_map.keySet() )
                {
                    sb.append( clazz );
                    sb.append( "=" );
                    for( String m : m_map.get( clazz ) )
                    {
                        sb.append( m );
                        sb.append( "," );
                    }
                    sb.append( ";" );
                }
                return sb.toString();
            }
        };
    }
}
