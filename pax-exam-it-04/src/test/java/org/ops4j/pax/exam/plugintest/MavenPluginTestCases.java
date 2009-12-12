/*
 * Copyright 2009 Toni Menzel.
 * Copyright 2009 Alin Dreghiciu.
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
package org.ops4j.pax.exam.plugintest;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.ops4j.pax.exam.Inject;

/**
 * Pax Exam Maven plugin related tests.
 *
 * @author Toni Menzel (tonit)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.4.0, March 26, 2009
 */
public class MavenPluginTestCases
{

    @Inject
    BundleContext bundleContext;

    /**
     * Tests that all maven dependencies are provisioned and stared in the target platform as configured in the
     * POM file.
     */
    @Test
    public void dependenciesAreProvisioned()
    {
        // this is a fragment that should be one of the "api" that come out of PAXEXAM-23.
        // "simple check for bundle states, wait for dependencies to resolve,unresolve" and so on.

        boolean foundAndActive = false;
        for( Bundle b : bundleContext.getBundles() )
        {
            if( b.getSymbolicName().equals( "org.ops4j.base.lang" ) )
            {
                if( b.getState() == Bundle.ACTIVE )
                {
                    foundAndActive = true;
                }
            }
        }
        if( !foundAndActive )
        {
            fail( "maven dependent bundle was not found or is not active" );
        }
    }

    /**
     * Tests that the started framework is Felix as configured in the POM file.
     */
    @Test
    public void expectedFramework()
    {
        assertThat(
            "Framework vendor",
            bundleContext.getProperty( Constants.FRAMEWORK_VENDOR ),
            is( equalTo( "Eclipse" ) )
        );
    }

}
