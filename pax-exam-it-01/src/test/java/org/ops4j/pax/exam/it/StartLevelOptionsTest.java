/*
 * Copyright 2009 Alin Dreghiciu
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
import org.osgi.service.startlevel.StartLevel;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.RequiresConfiguration;

/**
 * Start levels options integration tests.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 23, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class StartLevelOptionsTest
    extends OSGiHelper
{

    /**
     * Pax Exam test options that sets framework start level.
     *
     * @return test options
     */
    @Configuration
    public static Option[] configureFrameworkStartLevel()
    {
        return options(
            allFrameworks(),
            frameworkStartLevel( 88 )
        );
    }

    /**
     * Pax Exam test options that sets bundle start level.
     *
     * @return test options
     */
    @Configuration
    public static Option[] configureBundleStartLevel()
    {
        return options(
            felix(),
            //equinox(), // start level does not work on equinox (equinox itself not using the option)
            knopflerfish(), //start level does not work on kf if framework start level is < bundle start level
            frameworkStartLevel( 88 ),
            bundleStartLevel( 77 )
        );
    }

    /**
     * Tests that framework start level was set to 88.
     */
    @Test
    @RequiresConfiguration( "configureFrameworkStartLevel" )
    public void frameworkStartlevel()
    {
        final StartLevel startLevelService = getServiceObject( StartLevel.class );

        assertThat( "Start level service", startLevelService, is( notNullValue() ) );
        assertThat( "Start level", startLevelService.getStartLevel(), is( equalTo( 88 ) ) );
    }

    /**
     * Tests that initial bundle start level was set to 77.
     */
    @Test
    @RequiresConfiguration( "configureBundleStartLevel" )
    public void bundleStartlevel()
    {
        final StartLevel startLevelService = getServiceObject( StartLevel.class );

        assertThat( "Start level service", startLevelService, is( notNullValue() ) );
        assertThat( "Start level", startLevelService.getInitialBundleStartLevel(), is( equalTo( 77 ) ) );
    }

}