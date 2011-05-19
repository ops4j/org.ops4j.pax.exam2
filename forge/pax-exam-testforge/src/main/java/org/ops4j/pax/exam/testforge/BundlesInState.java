/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.testforge;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.ops4j.pax.exam.TestContainerException;

/**
 * Simple pre-build test to be used with Pax Exam Player.
 *
 * This checks the current state of all bundles for a certain state range.
 * The desiredMaxBundleState argument may be ommited. In this case it will be set to desiredMinBundleState.
 */
public class BundlesInState {

    @SuppressWarnings( "unused" )
    public void probe( BundleContext ctx, Integer desiredMinBundleState, Integer desiredMaxBundleState )
        throws InterruptedException, InvalidSyntaxException
    {
        if( desiredMinBundleState == null ) { throw new TestContainerException( "Argument desiredBundleState (integer) is mandatory." ); }
        if( desiredMaxBundleState == null ) {
            desiredMaxBundleState = desiredMinBundleState;
        }

        for( Bundle b : ctx.getBundles() ) {
            final Integer state = b.getState();
            if( state < desiredMinBundleState || state > desiredMaxBundleState ) {
                throw new TestContainerException( "Bundle " + b.getBundleId() + " (" + b.getSymbolicName() + ") State: " + state + " is not between state: " + desiredMinBundleState + ":" + desiredMaxBundleState );
            }
        }
    }
}
