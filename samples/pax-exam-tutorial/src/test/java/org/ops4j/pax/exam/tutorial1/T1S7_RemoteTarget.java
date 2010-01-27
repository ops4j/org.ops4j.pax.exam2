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
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.remote.RBCRemoteContainerFactory;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.options.ReUsePolicy;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.remote.RBCRemoteTargetOptions.*;
import static org.ops4j.pax.exam.junit.JUnitOptions.*;

/**
 * Running a test on a remote device using an already installed rbc and a know connection
 *
 * @author Toni Menzel
 * @since Jan 26, 2010
 */
@RunWith( JUnit4TestRunner.class )
public class T1S7_RemoteTarget
{

    /*
     * Here you can configure most of exam.
     * Annotate any method with @Configuration and be sure to set return type to Option[]
     *
     * Thats all. In this case, we just "tell" via fluent api, to use equinox.
     *
     */

    @Configuration
    public static Option[] configure()
    {
        return options(
            //
            executionPolicy()
                .testContainer( RBCRemoteContainerFactory.class )
                .reuseContainer( ReUsePolicy.NEVER ),
            waitForRBCFor( 2000 ),
            //-Dorg.ops4j.pax.exam.rbc.rmi.port=9191
            location( "localhost", 9191 )
        );
    }

    /**
     * You will get a list of bundles installed by default
     * plus your testcase, wrapped into a bundle called pax-exam-probe
     *
     * @param bundleContext injected
     */
    @Test
    public void helloRemote( BundleContext bundleContext )
    {
        for( Bundle b : bundleContext.getBundles() )
        {
            System.out.println( "boo " + b.getBundleId() + " : " + b.getSymbolicName() );
        }
    }
}
