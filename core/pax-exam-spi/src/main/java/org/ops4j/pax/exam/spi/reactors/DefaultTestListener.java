/*
 * Copyright 2016 Harald Wellmann.
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
package org.ops4j.pax.exam.spi.reactors;

import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;

/**
 * A no-op {@link TestListener} implementation.
 * 
 * @author hwellmann
 *
 */
public class DefaultTestListener implements TestListener {

    public DefaultTestListener() {
        // empty
    }

    @Override
    public void testStarted(TestDescription description) {
        // empty
    }

    @Override
    public void testFinished(TestDescription description) {
        // empty
    }

    @Override
    public void testFailure(TestFailure failure) {
        // empty
    }

    @Override
    public void testAssumptionFailure(TestFailure failure) {
        // empty
    }

    @Override
    public void testIgnored(TestDescription description) {
        // empty
    }
}
