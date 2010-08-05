/*
 * Copyright (C) 2010 Okidokiteam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okidokiteam.exxam.test;

import org.junit.Test;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.nat.internal.NativeTestContainerFactory;
import org.ops4j.pax.exam.spi.ExxamReactor;
import org.ops4j.pax.exam.spi.ProbeCall;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.TestProbeBuilder;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;
import org.ops4j.pax.exam.spi.driversupport.DefaultExamReactor;

import static org.ops4j.pax.exam.spi.container.DefaultRaw.*;

/**
 * Simple test
 */
public class A1
{

    /**
     * Very low level.
     */
    @Test
    public void minimalPlan()
        throws Exception
    {
        System.out.println( "************ runTest1" );
        TestContainerFactory factory = PaxExamRuntime.getTestContainerFactory( NativeTestContainerFactory.class );

        // we know there can be only one container
        OptionDescription testTarget = factory.parse()[ 0 ];

        TestContainer testContainer = factory.createContainer( testTarget );
        try
        {
            testContainer.start();
            TestProbeBuilder probe = createProbe().addTest( Probe.class );
            testContainer.install( probe.getStream() );

            for( ProbeCall call : probe.getTests() )
            {
                execute( testContainer, call );
            }
        } finally
        {
            testContainer.stop();
        }
    }

    /**
     * Low level but reactor support.
     */
    @Test
    public void useReactor()
        throws Exception
    {
        System.out.println( "************ runTest2" );
        TestContainerFactory factory = PaxExamRuntime.getTestContainerFactory( NativeTestContainerFactory.class );

        ExxamReactor reactor = new DefaultExamReactor( factory );
        TestProbeBuilder probe = createProbe().addTest( Probe.class );
        reactor.addProbe( probe );

        StagedExamReactor stagedReactor = reactor.stage();
        try
        {
            for( ProbeCall call : probe.getTests() )
            {
                stagedReactor.invoke( call );
            }

        } finally
        {
            stagedReactor.tearDown();
        }
    }
}
