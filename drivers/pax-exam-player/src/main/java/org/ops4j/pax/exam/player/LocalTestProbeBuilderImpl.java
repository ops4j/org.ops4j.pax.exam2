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
package org.ops4j.pax.exam.player;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.probesupport.intern.DefaultTestAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a local test probe : no bundle is installed.
 * The test is executed in the junit jvm.
 * 
 * @author Toni Menzel, Stephane Chomat
 * @since April 27, 2011
 */
public class LocalTestProbeBuilderImpl implements TestProbeBuilder, TestProbeProvider {

    private static Logger LOG = LoggerFactory.getLogger( LocalTestProbeBuilderImpl.class );
    private static final String DEFAULT_PROBE_METHOD_NAME = "probe";

    private final Map<TestAddress, TestInstantiationInstruction> m_probeCalls = new HashMap<TestAddress, TestInstantiationInstruction>();
    
    
    public TestAddress addTest( Class clazz, String methodName, Object... args  )
    {
        TestAddress address =  new DefaultTestAddress( clazz.getName() + "." + methodName ,args );
        m_probeCalls.put( address, new TestInstantiationInstruction( clazz.getName() + ";" + methodName ) );
        return address;
    }

    public TestAddress addTest( Class clazz, Object... args  )
    {
        return addTest( clazz, DEFAULT_PROBE_METHOD_NAME, args );
    }

    public List<TestAddress> addTests( Class clazz, Method... methods )
    {
        List<TestAddress> list = new ArrayList<TestAddress>();
        for( Method method : methods ) {
            list.add( addTest( clazz, method.getName() ) );
        }
        return list;
    }

    public TestProbeBuilder addAnchor( Class clazz )
    {
        return this;
    }

    public TestProbeBuilder setHeader( String key, String value )
    {
        return this;
    }

    public TestProbeBuilder ignorePackageOf( Class... classes )
    {
        return this;
    }

    public TestProbeProvider build()
    {
        return this;
    }

    public TestAddress[] getTests()
    {
        return m_probeCalls.keySet().toArray( new TestAddress[ m_probeCalls.size() ] );
    }

    public InputStream getStream() throws IOException {
        throw new UnsupportedOperationException();
    }
}
