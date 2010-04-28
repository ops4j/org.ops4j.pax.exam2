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
package org.ops4j.pax.exam.tutorial1;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

/**
 * This is the simpliest test possible.
 *
 * It uses all default options (which means felix as platform, no additional bundles but exam itself and your test)
 *
 * To get Pax Exam running with minimal code, this class shows whats neeeded.
 * Additionally you just need the required jars/dependencies from pax exam in your classpath.
 * Nothing more.
 *
 * @author Toni Menzel (tonit)
 * @since Mar 3, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class T1S1_HelloWorldTest
{

    @Configuration
    public static Option[] configure()
    {
        return options(
            felix().version( "2.0.0" )
        );
    }

    /**
     * This will just print a line to the console.
     * Actually what really happens is your test being packed into a bundle, a new java process
     * is being spawned, an osgi framework (felix by default) is being downloaded via maven,
     * all of that is being started, your test is being called at the very end via RMI (we have two processes!).
     * After all is done, the remote process (the one with the osgi framework and your running test)
     * is being shut down gracefully.
     */
    @Test
    public void simpliestTest()
    {
        System.out.println( "************ Hello from OSGi ************" );
    }

}
