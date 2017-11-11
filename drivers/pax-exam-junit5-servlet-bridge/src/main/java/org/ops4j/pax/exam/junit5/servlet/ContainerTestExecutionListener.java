/*
 * Copyright 2017 OPS4J Contributors
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
package org.ops4j.pax.exam.junit5.servlet;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestEvent;
import org.ops4j.pax.exam.TestEventType;
import org.ops4j.pax.exam.util.Exceptions;

/**
 * @author Harald Wellmann
 *
 */
public class ContainerTestExecutionListener implements TestExecutionListener {

    private ObjectOutputStream oos;

    public ContainerTestExecutionListener(ObjectOutputStream oos) {
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
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        TestDescription description = convertDescription(testIdentifier);
        if (description != null) {
            writeEvent(new TestEvent(TestEventType.TEST_IGNORED, description));
        }
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        TestDescription description = convertDescription(testIdentifier);
        if (description != null) {
            writeEvent(new TestEvent(TestEventType.TEST_STARTED, description));
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier,
        TestExecutionResult testExecutionResult) {
        TestDescription description = convertDescription(testIdentifier);
        if (description != null) {
            if (testExecutionResult.getStatus() == Status.FAILED) {
                writeEvent(new TestEvent(TestEventType.TEST_FAILED, description, testExecutionResult.getThrowable().orElse(null)));
            }
            writeEvent(new TestEvent(TestEventType.TEST_FINISHED, description));
        }
    }

    public static TestDescription convertDescription(TestIdentifier testIdentifier) {
        String uniqueId = testIdentifier.getUniqueId();
        String[] parts = uniqueId.split("/");
        if (parts.length < 2) {
            return null;
        }
        String methodName = null;
        if (parts.length >= 3) {
            String methodSegment = parts[2];
            int colon = methodSegment.indexOf(":");
            methodName = methodSegment.substring(colon + 1, methodSegment.length() - 3);
        }
        String classSegment = parts[1];
        int colon = classSegment.indexOf(":");
        String className = classSegment.substring(colon + 1, classSegment.length() - 1);

        return new TestDescription(className, methodName);
    }
}
