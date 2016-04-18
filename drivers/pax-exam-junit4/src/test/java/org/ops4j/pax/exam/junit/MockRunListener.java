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
package org.ops4j.pax.exam.junit;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;

/**
 * @author hwellmann
 *
 */
public class MockRunListener extends RunListener {


    private TestListener listener;

    public MockRunListener(TestListener listener) {
        this.listener = listener;
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        listener.testFailure(convertFailure(failure));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        listener.testFinished(convertDescription(description));
    }

    @Override
    public void testStarted(Description description) throws Exception {
        listener.testStarted(convertDescription(description));
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        listener.testIgnored(convertDescription(description));
    }

    public static TestDescription convertDescription(Description description) {
        return new TestDescription(description.getClassName(), description.getMethodName());
    }

    public static TestFailure convertFailure(Failure failure) {
        return new TestFailure(convertDescription(failure.getDescription()), failure.getException());
    }
}
