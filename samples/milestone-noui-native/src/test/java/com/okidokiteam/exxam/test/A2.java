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
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.ProbeBuilder;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.LibraryOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

/**
 * Simple Test Rack that uses the JUnit4 UI
 */
@RunWith( JUnit4TestRunner.class )
//@ExamReactorStrategy( AllConfinedStagedReactorFactory.class )
public class A2
{

    @Configuration()
    public Option[] config()
    {
        return options(
            junitBundles(),
            easyMockBundles(),
            cleanCaches()
        );
    }

    @ProbeBuilder
    public TestProbeBuilder customizeProbe( TestProbeBuilder overwrite )
    {
        return overwrite
            .setHeader( "Bundle-SymbolicName", "ItsAMario" );
    }

    //@Test
    public void withoutBC()
    {
        System.out.println( "++++ PEAK ++++" );
    }

    @Test
    public void withBC( BundleContext ctx )
    {
        assertThat( ctx, is( notNullValue() ) );
        System.out.println( "BundleContext of bundle injected: " + ctx.getBundle().getSymbolicName() );

    }

    public void neverCall()
    {
        fail( "Don't call me !" );
    }
}
