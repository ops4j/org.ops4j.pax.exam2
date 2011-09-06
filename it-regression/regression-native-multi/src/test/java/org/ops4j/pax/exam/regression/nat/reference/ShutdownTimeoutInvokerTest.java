/*
 * Copyright (C) 2011 Harald Wellmann
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
package org.ops4j.pax.exam.regression.nat.reference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.prefs.BackingStoreException;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * 
 * @author Harald Wellmann
 * 
 */
public class ShutdownTimeoutInvokerTest
{
    @Test
    public void exceptionOnShutdownTimeout() throws IOException, BackingStoreException
    {
        JUnitCore junit = new JUnitCore();
        Result result = junit.run( ShutdownTimeoutTestWrapped.class );
        assertEquals( 2, result.getFailureCount() );

        for ( Failure failure : result.getFailures() )
        {
            assertTrue( failure.getMessage().startsWith( "Framework has not yet stopped" ) );
        }
    }
}
