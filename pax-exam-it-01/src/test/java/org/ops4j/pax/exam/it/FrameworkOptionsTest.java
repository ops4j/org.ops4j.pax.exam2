/*
 * Copyright 2008 Alin Dreghiciu
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.it;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.AppliesTo;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * Framework options integration tests.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, November 17, 2008
 */
@RunWith( JUnit4TestRunner.class )
public class FrameworkOptionsTest
{

    /**
     * Pax Exam test options that specified Equinox as test framework.
     * Valid for test methods that starts with "equinox".
     *
     * @return test options
     */
    @Configuration
    @AppliesTo( "equinox.*" )
    public static Option[] configureEquinox()
    {
        return options(
            equinox()
        );
    }

    /**
     * Test that the started framewrok is Equinox.
     *
     * @param bundleContext injected bundle context
     */
    @Test
    public void equinoxIsUpAndRunning( final BundleContext bundleContext )
    {
        assertThat( "Bundle context", bundleContext, is( notNullValue() ) );
        assertThat(
            "Framework vendor",
            bundleContext.getProperty( Constants.FRAMEWORK_VENDOR ),
            is( equalTo( "Eclipse" ) )
        );
    }

    /**
     * Pax Exam test options that specified Felix as test framework.
     * Valid for test methods that starts with "felix".
     *
     * @return test options
     */
    @Configuration
    @AppliesTo( "felix.*" )
    public static Option[] configureFelix()
    {
        return options(
            felix()
        );
    }

    /**
     * Test that the started framewrok is Felix.
     *
     * @param bundleContext injected bundle context
     */
    @Test
    public void felixIsUpAndRunning( final BundleContext bundleContext )
    {
        assertThat( "Bundle context", bundleContext, is( notNullValue() ) );
        assertThat(
            "Framework vendor",
            bundleContext.getProperty( Constants.FRAMEWORK_VENDOR ),
            is( equalTo( "Apache Software Foundation" ) )
        );
    }

    /**
     * Pax Exam test options that specified Knopflerfish as test framework.
     * Valid for test methods that starts with "knopflerfish".
     *
     * @return test options
     */
    @Configuration
    @AppliesTo( "knopflerfish.*" )
    public static Option[] configureKnopflerfish()
    {
        return options(
            knopflerfish()
        );
    }

    /**
     * Test that the started framewrok is Knopflerfish.
     *
     * @param bundleContext injected bundle context
     */
    @Test
    public void knopflerfishIsUpAndRunning( final BundleContext bundleContext )
    {
        assertThat( "Bundle context", bundleContext, is( notNullValue() ) );
        assertThat(
            "Framework vendor",
            bundleContext.getProperty( Constants.FRAMEWORK_VENDOR ),
            is( equalTo( "Knopflerfish" ) )
        );
    }

}