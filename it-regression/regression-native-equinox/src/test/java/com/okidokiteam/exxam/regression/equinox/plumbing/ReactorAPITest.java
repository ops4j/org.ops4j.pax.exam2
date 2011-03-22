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
package com.okidokiteam.exxam.regression.equinox.plumbing;

import org.junit.Test;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.ExxamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;
import org.ops4j.pax.exam.spi.container.PlumbingContext;
import org.ops4j.pax.exam.spi.driversupport.DefaultExamReactor;
import org.ops4j.pax.exam.spi.reactors.EagerSingleStagedReactorFactory;

import static org.ops4j.pax.exam.LibraryOptions.*;
import static org.ops4j.pax.exam.spi.container.PlumbingContext.*;

/**
 * Simple regression
 */
public class ReactorAPITest
{

    private TestContainerFactory getFactory()
    {
        return PaxExamRuntime.getTestContainerFactory();
    }

    @Test
    public void reactorRunAllConfinedTest()
        throws Exception
    {
        //reactorRun( new AllConfinedStagedReactorFactory(),
        //            new Option[]{ junitBundles(), easyMockBundles() }
        //);

        reactorRun( new EagerSingleStagedReactorFactory(),
                    new Option[]{ junitBundles(), easyMockBundles() },
                    new Option[]{ junitBundles(), easyMockBundles() }
        );
    }

    //@Test
    public void reactorRunEagerTest()
        throws Exception
    {
        reactorRun( new EagerSingleStagedReactorFactory(), new Option[]{ junitBundles(), easyMockBundles() } );
        reactorRun( new EagerSingleStagedReactorFactory(), new Option[]{ junitBundles(), easyMockBundles() } );
    }

    public void reactorRun( StagedExamReactorFactory strategy, Option[]... options )
        throws Exception
    {
        TestContainerFactory factory = getFactory();
        PlumbingContext ctx = new PlumbingContext();
        ExxamReactor reactor = new DefaultExamReactor( ctx,factory );

        TestProbeProvider probe = ctx.createProbe().addTest( Probe.class ).build();

        reactor.addProbe( probe );
        for( Option[] option : options )
        {
            reactor.addConfiguration( option );
        }

        StagedExamReactor stagedReactor = reactor.stage( strategy );
        try
        {
            for( TestAddress call : probe.getTests() )
            {
                stagedReactor.invoke( call );
            }

        } finally
        {
            stagedReactor.tearDown();
        }
    }

}
