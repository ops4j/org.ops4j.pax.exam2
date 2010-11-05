/*
 * Copyright 2009 Alin Dreghiciu.
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
package org.ops4j.pax.exam.junit.internal;

import org.junit.After;
import org.junit.Before;
import org.junit.internal.runners.MethodRoadie;
import org.junit.internal.runners.TestMethod;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;

/**
 * Extends {@link MethodRoadie} in order to supress the running of {@link Before}s and {@link After}s.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, January 28, 2009
 */
public class JUnit4MethodRoadie
    extends MethodRoadie
{

    /**
     * Constructor matching super.
     *
     * {@inheritDoc}
     */
    public JUnit4MethodRoadie( final Object test,
                               final TestMethod method,
                               final RunNotifier notifier,
                               final Description description )
    {
        super( test, method, notifier, description );
    }

    /**
     * Do not run the before and afters are they are run in the container by the moment the regression is run.
     *
     * {@inheritDoc}
     */
    @Override
    public void runBeforesThenTestThenAfters( final Runnable test )
    {
        try
        {
            test.run();
        }
        catch( Exception e )
        {
            throw new RuntimeException( "regression should never throw an exception to this level" );
        }
    }

}
