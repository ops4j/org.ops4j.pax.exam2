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
package org.ops4j.pax.exam.testng.servlet;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestEvent;
import org.ops4j.pax.exam.TestEventType;
import org.ops4j.pax.exam.util.Exceptions;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * @author Harald Wellmann
 *
 */
public class ContainerResultListener implements ITestListener {

    private ObjectOutputStream oos;

    public ContainerResultListener(ObjectOutputStream oos) {
        this.oos = oos;
    }

    @Override
    public void onTestStart(ITestResult result) {
        writeEvent(new TestEvent(TestEventType.TEST_STARTED, toDescription(result)));
    }

    private void writeEvent(TestEvent event) {
        try {
            oos.writeObject(event);
        }
        catch (IOException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    private TestDescription toDescription(ITestResult result) {
        return new TestDescription(result.getTestClass().getName(), result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        writeEvent(new TestEvent(TestEventType.TEST_FINISHED, toDescription(result)));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        writeEvent(new TestEvent(TestEventType.TEST_FAILED, toDescription(result), result.getThrowable()));
        writeEvent(new TestEvent(TestEventType.TEST_FINISHED, toDescription(result)));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        writeEvent(new TestEvent(TestEventType.TEST_IGNORED, toDescription(result)));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // empty
    }

    @Override
    public void onStart(ITestContext context) {
        // empty
    }

    @Override
    public void onFinish(ITestContext context) {
        // empty
    }
}
