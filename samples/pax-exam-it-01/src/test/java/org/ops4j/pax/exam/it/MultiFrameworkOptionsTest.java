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
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * Framework options integration tests.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, November 17, 2008
 */
@RunWith( JUnit4TestRunner.class )
public class MultiFrameworkOptionsTest
{

    /**
     * Pax Exam test options that specify to run the test on all frameworks.
     * Valid for all test methods.
     *
     * @return test options
     */
    @Configuration
    public static Option[] configure()
    {
        return options(
            allFrameworksVersions()
        );
    }

    /**
     * Tests that the bundle context is valid on all platforms.
     * This means that the whole infrastructure works.
     *
     * @param bundleContext injected bundle context
     */
    @Test
    public void allVersionsAreUpAndRunning( final BundleContext bundleContext )
    {
        assertThat( "Bundle context", bundleContext, is( notNullValue() ) );
        assertThat(
            "Framework vendor",
            bundleContext.getProperty( Constants.FRAMEWORK_VENDOR ),
            is( notNullValue() )
        );
    }

}