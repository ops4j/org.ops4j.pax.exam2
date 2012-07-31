/*
 * Copyright 2012 Harald Wellmann
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
package org.ops4j.pax.exam.regression.multi.inject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.*;

import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Checks that injection of a non-existing service times out, and that the default timeout
 * can be changed by setting the configuration property pax.exam.service.timeout.
 * 
 * @author Harald Wellmann
 *
 */
public class ServiceTimeoutInvokerTest
{

    /**
     * The default service lookup timeout is 10 s, but we changed it to 5 s in exam.properties,
     */
    @Test( timeout = 8000 )
    public void checkServiceLookupTimeout()
    {
        // Equinox and Knopflerfish do not shutdown cleanly after an exception during startup,
        // so we restrict this test to Felix.
        assumeTrue( isFelix() );
        assumeTrue( !isPaxRunnerContainer() );
        
        JUnitCore junit = new JUnitCore();
        Result run = junit.run( ServiceTimeout.class );
        assertThat( run.getFailureCount(), is( 1 ) );
        Failure failure = run.getFailures().get( 0 );
        assertThat( failure.getMessage(), JUnitMatchers.containsString( "gave up waiting for service" ) );
    }
}
