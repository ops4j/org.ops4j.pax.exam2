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
import org.osgi.framework.BundleContext;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.junit.RequiresConfiguration;

/**
 * Optional options integration tests.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 20, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class OptionalCompositeOptionsTest
{

    @Inject
    BundleContext m_bundleContext;

    /**
     * Pax Exam configuration that will include a system property option when ${user.home} system property is set,
     * which is always meaning that the option will always be included.
     *
     * @return test options
     */
    @Configuration
    public static Option[] configureWhenUserHomeIsSet()
    {
        return options(
            when( System.getProperty( "user.home" ) != null ).useOptions(
                systemProperty( "foo" ).value( "bar" )
            )
        );
    }

    /**
     * Pax Exam configuration that will include a system property option when ${user.home} system property is not set,
     * which is never meaning that the option will never be included.
     *
     * @return test options
     */
    @Configuration
    public static Option[] configureWhenUserHomeIsNotSet()
    {
        return options(
            when( System.getProperty( "user.home" ) == null ).useOptions(
                systemProperty( "foo" ).value( "bar" )
            )
        );
    }

    /**
     * Tests that the system property is set (as user.home property is always != null).
     */
    @Test
    @RequiresConfiguration( "configureWhenUserHomeIsSet" )
    public void systemPropertyIsSet()
    {
        assertThat( m_bundleContext.getProperty( "foo" ), is( notNullValue() ) );
        assertThat( m_bundleContext.getProperty( "foo" ), is( equalTo( "bar" ) ) );
    }

    /**
     * Tests that the system property is set (as user.home property is always != null).
     */
    @Test
    @RequiresConfiguration( "configureWhenUserHomeIsNotSet" )
    public void systemPropertyIsNotSet()
    {
        assertThat( m_bundleContext.getProperty( "foo" ), is( nullValue() ) );
    }

}