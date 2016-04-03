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
package org.ops4j.pax.exam;

/**
 * @author hwellmann
 *
 */
public interface TestListener {

    void testStarted(TestDescription description);

    /**
     * Called when an atomic test has finished, whether the test succeeds or fails.
     *
     * @param description the description of the test that just ran
     */
    void testFinished(TestDescription description);

    /**
     * Called when an atomic test fails, or when a listener throws an exception.
     *
     *
     * @param failure describes the test that failed and the exception that was thrown
     */
    void testFailure(TestFailure failure);

    /**
     * Called when an atomic test flags that it assumes a condition that is
     * false
     *
     * @param failure describes the test that failed and the
     * {@link org.junit.AssumptionViolatedException} that was thrown
     */
    void testAssumptionFailure(TestFailure failure);

    /**
     * Called when a test will not be run, indicated by a corresponding annotation.
     *
     * @param description describes the test that will not be run
     */
    void testIgnored(TestDescription description);

}
