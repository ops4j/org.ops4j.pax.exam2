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

    public AbstractProbe()
        throws IOException
    {
        Store<InputStream> store = StoreFactory.defaultStore();
        Properties p = new Properties();
        m_builder = new TestProbeBuilderImpl( p, store );
    }

    public TestAddress[] getTests()
    {
        build();
        return m_built.getTests();
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

    protected void addTest( Class<?> clazz, Method m )
    {
        m_builder.addTest( clazz, m );
    }
}
