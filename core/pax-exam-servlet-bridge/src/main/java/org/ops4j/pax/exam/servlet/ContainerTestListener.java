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
package org.ops4j.pax.exam.servlet;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestEvent;
import org.ops4j.pax.exam.TestEventType;
import org.ops4j.pax.exam.WrappedTestContainerException;
import org.ops4j.pax.exam.util.Exceptions;

/**
 * @author hwellmann
 *
 */
public class ContainerTestListener extends RunListener {

    private ObjectOutputStream oos;


    public ContainerTestListener(ObjectOutputStream oos) {
        this.oos = oos;
    }

    private void writeEvent(TestEvent event) {
        try {
            oos.writeObject(event);
        }
        catch (IOException exc) {
            throw Exceptions.unchecked(exc);
        }
    }


    @Override
    public void testFailure(Failure failure) throws Exception {
        TestDescription description = convertDescription(failure.getDescription());
        Exception exc = new WrappedTestContainerException(failure.getException());
        writeEvent(new TestEvent(TestEventType.TEST_FAILED, description, exc));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        writeEvent(new TestEvent(TestEventType.TEST_FINISHED, convertDescription(description)));
    }

    @Override
    public void testStarted(Description description) throws Exception {
        writeEvent(new TestEvent(TestEventType.TEST_STARTED, convertDescription(description)));
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        writeEvent(new TestEvent(TestEventType.TEST_IGNORED, convertDescription(description)));
    }

    public static TestDescription convertDescription(Description description) {
        return new TestDescription(description.getClassName(), description.getMethodName());
    }
}
