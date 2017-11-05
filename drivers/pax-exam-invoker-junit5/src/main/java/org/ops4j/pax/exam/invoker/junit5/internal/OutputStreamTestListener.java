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
package org.ops4j.pax.exam.invoker.junit5.internal;

import static org.ops4j.pax.exam.TestEventType.TEST_ASSUMPTION_FAILED;
import static org.ops4j.pax.exam.TestEventType.TEST_FAILED;
import static org.ops4j.pax.exam.TestEventType.TEST_FINISHED;
import static org.ops4j.pax.exam.TestEventType.TEST_IGNORED;
import static org.ops4j.pax.exam.TestEventType.TEST_STARTED;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestEvent;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;

/**
 * @author Harald Wellmann
 *
 */
public class OutputStreamTestListener implements TestListener {

    private ObjectOutputStream oos;

    public OutputStreamTestListener(ObjectOutputStream oos) {
        this.oos = oos;
    }

    @Override
    public void testStarted(TestDescription description) {
        TestEvent event = new TestEvent(TEST_STARTED, description);
        sendEvent(event);
    }

    private void sendEvent(TestEvent event) {
        try {
            oos.writeObject(event);
        }
        catch (IOException exc) {
            throw new TestContainerException(exc);
        }
    }

    @Override
    public void testFinished(TestDescription description) {
        TestEvent event = new TestEvent(TEST_FINISHED, description);
        sendEvent(event);
    }

    @Override
    public void testFailure(TestFailure failure) {
        TestEvent event = new TestEvent(TEST_FAILED, failure.getDescription(),
            failure.getException());
        sendEvent(event);
    }

    @Override
    public void testAssumptionFailure(TestFailure failure) {
        TestEvent event = new TestEvent(TEST_ASSUMPTION_FAILED, failure.getDescription(),
            failure.getException());
        sendEvent(event);
    }

    @Override
    public void testIgnored(TestDescription description) {
        TestEvent event = new TestEvent(TEST_IGNORED, description);
        sendEvent(event);
    }
}
