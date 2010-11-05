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
package com.okidokiteam.exxam.regression.base;

import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;

import static org.ops4j.pax.exam.LibraryOptions.*;
import static org.ops4j.pax.exam.spi.container.DefaultRaw.*;

/**
 *
 */
public class BareAPITest
{

    private TestContainerFactory getFactory()
    {
        return PaxExamRuntime.getTestContainerFactory();
    }

    /**
     * Very low level.
     *
     * @throws Exception problems?
     */
    @Test
    public void bareRunTest()
        throws Exception
    {
        TestContainerFactory factory = getFactory();
        Option[] options = new Option[]{ junitBundles(), easyMockBundles() };

// the parse will split all single containers into dedicated OptionDescription(s)
        for( OptionDescription testTarget : factory.parse( options ) )
        {
            TestContainer testContainer = factory.createContainer( testTarget );
            try
            {
                testContainer.start();
                TestProbeProvider probe = createProbe().addTest( Probe.class ).build();
                testContainer.install( probe.getStream() );

                for( TestAddress call : probe.getTests() )
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

    @Test
    public void bareRunMoreThanOneProbeTest()
        throws Exception
    {
        TestContainerFactory factory = getFactory();
        Option[] options = new Option[]{ junitBundles(), easyMockBundles() };

        // the parse will split all single containers into dedicated OptionDescription(s)
        for( OptionDescription testTarget : factory.parse( options ) )
        {
            TestContainer testContainer = factory.createContainer( testTarget );
            try
            {
                testContainer.start();
                TestProbeProvider probe = createProbe().addTest( Probe.class ).addTest( Probe2.class ).build();

                testContainer.install( probe.getStream() );

                for( TestAddress call : probe.getTests() )
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
}
