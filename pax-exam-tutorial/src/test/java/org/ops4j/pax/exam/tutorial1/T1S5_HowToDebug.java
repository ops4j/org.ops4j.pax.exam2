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
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * @author Toni Menzel (tonit)
 * @since Mar 3, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class T1S5_HowToDebug
{

    @Inject
    BundleContext bundleContext = null;

    @Configuration
    public static Option[] configure()
    {
        return options(
            // this just adds all what you write here to java vm argumenents of the (new) osgi process.
            vmOption( "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5006" ),
            // this is necessary to let junit runner not timout the remote process before attaching debugger
            // setting timeout to 0 means wait as long as the remote service comes available.
            // starting with version 0.5.0 of PAx Exam this is no longer required as by default the framework tests
            // will not be triggered till the framework is not started
            waitForFrameworkStartup()
        );
    }

    @Test
    public void helloAgain()
    {
        System.out.println( "This is running inside Felix. With all configuration set up like you specified. " );
        // feel free to add breakpoints and debug.
        for( Bundle b : bundleContext.getBundles() )
        {
            System.out.println( "Bundle " + b.getBundleId() + " : " + b.getSymbolicName() );
        }

    }
}
