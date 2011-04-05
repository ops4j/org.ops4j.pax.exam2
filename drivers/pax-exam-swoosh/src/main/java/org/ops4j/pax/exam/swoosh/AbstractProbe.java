/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.swoosh;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.probesupport.intern.TestProbeBuilderImpl;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;

/**
 *
 */
public class AbstractProbe implements TestProbeProvider {

    private TestProbeBuilderImpl m_builder;
    private TestProbeProvider m_built;
    private List<ParameterizedAddress> m_list = new ArrayList<ParameterizedAddress>();

    public static AbstractProbe TEST = new AbstractProbe();

    public AbstractProbe()
    {
        try {
            Store<InputStream> store = StoreFactory.defaultStore();
            Properties p = new Properties();
            m_builder = new TestProbeBuilderImpl( p, store );
        } catch( IOException e ) {
            //
            throw new RuntimeException( "problem" );
        }
    }

    public AbstractProbe add( Class c, Object... args )
        throws NoSuchMethodException
    {
        addTest( c, "probe", args );
        return this;
    }

    public TestAddress[] getTests()
    {
        build();
        return m_list.toArray( new TestAddress[ m_list.size() ] );
    }

    public InputStream getStream()
        throws IOException
    {
        build();
        return m_built.getStream();
    }

    private void build()
    {
        if( m_built == null ) { m_built = m_builder.build(); }
    }

    protected void addTest( Class<?> clazz, String m, Object... args )
    {
        m_list.add( new ParameterizedAddress( m_builder.addTest( clazz, m ), args ) );
    }

}
