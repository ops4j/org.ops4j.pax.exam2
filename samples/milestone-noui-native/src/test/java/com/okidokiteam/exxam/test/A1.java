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
import org.ops4j.pax.exam.Option;
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

    private TestContainerFactory getFactory()
    {
        return PaxExamRuntime.getTestContainerFactory( NativeTestContainerFactory.class );
    }

    /**
     * Very low level.
     */
    @Test
    public void minimalPlanBareLowLevel()
        throws Exception
    {
        TestContainerFactory factory = getFactory();
        Option[] options = new Option[]{ };

        // the parse will split all single containers into dedicated OptionDescription(s)
        for( OptionDescription testTarget : factory.parse( options ) )
        {
            TestContainer testContainer = factory.createContainer( testTarget );
            try
            {
                testContainer.start();
                TestProbeBuilder probe = createProbe().addTest( Probe.class );
                testContainer.install( probe.getStream() );

                for( ProbeCall call : probe.getTests() )
                {
                    // this is a shortcut for getting the proper Service (ServiceInvoker currently) and calls the "invoke" with that call (handle)
                    execute( testContainer, call );
                }
            } finally
            {
                testContainer.stop();
            }
        }
    }

    /**
     * Low level but reactor support.
     */
    @Test
    public void minimalPlanUsingReactor()
        throws Exception
    {
        TestContainerFactory factory = getFactory();
        Option[] options = new Option[]{ };

        /**
         * In this example we don't split and control containers ourselves, we use ExxamRactor.
         * This can be fed with
         * - probes (addProbe)
         * - options (addConfiguration)
         * Once this is done, calliing "stage()" gives you the possibility to invoke tests directly.
         *
         * Note that you don't interact with any TestContainer or how many you actually create.
         * You just iterate over all your previously added tests and invoke them using the "handles" (ProbeCall)
         *
         * Whatr is a ProbeCall ?
         * Its a handle to invoke a particular test method.
         * It is up to the ProbeBuilder to make meaningful handles so they can be found and executed.
         * TODO: Guess this needs more explanation, as its a quite powerful concept that also lets you control testclass initialization and "what to actually call on that class".
         *
         *
         */
        ExxamReactor reactor = new DefaultExamReactor( factory );

        TestProbeBuilder probe = createProbe().addTest( Probe.class );

        reactor.addProbe( probe );
        reactor.addConfiguration( options );

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
