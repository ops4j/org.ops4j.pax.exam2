/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.spi.war;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.options.WarProbeOption;
import org.ops4j.pax.exam.spi.intern.DefaultTestAddress;

/**
 * Builds a WAR probe.
 * @author Harald Wellmann
 *
 */
public class WarTestProbeBuilderImpl implements TestProbeBuilder
{
    private WarProbeOption option;
    private final Map<TestAddress, TestInstantiationInstruction> probeCalls =
        new HashMap<TestAddress, TestInstantiationInstruction>();

    public WarTestProbeBuilderImpl()
    {
        this( new WarProbeOption().classPathDefaultExcludes() );
    }

    public WarTestProbeBuilderImpl( WarProbeOption option )
    {
        this.option = option;
    }

    @Override
    public TestAddress addTest( Class<?> clazz, String methodName, Object... args )
    {
        TestAddress address =
            new DefaultTestAddress( clazz.getSimpleName() + "." + methodName, args );
        probeCalls.put( address, new TestInstantiationInstruction( clazz.getName() + ";"
                + methodName ) );
        return address;
    }

    @Override
    public TestAddress addTest( Class<?> clazz, Object... args )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<TestAddress> addTests( Class<?> clazz, Method... m )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestProbeBuilder setHeader( String key, String value )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestProbeBuilder ignorePackageOf( Class<?>... classes )
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestProbeProvider build()
    {
        WarBuilder warBuilder = new WarBuilder( option );
        URI warUri = warBuilder.buildWar();
        return new WarTestProbeProvider( warUri, getTests() );
    }

    public Set<TestAddress> getTests()
    {
        return probeCalls.keySet();
    }
}
