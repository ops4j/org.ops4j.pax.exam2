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
package org.ops4j.pax.exam.invoker.testng.internal;

import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * @author hwellmann
 *
 */
public class ContainerResultListener implements ITestListener {


    private TestListener listener;

    /**
     *
     */
    public ContainerResultListener(TestListener listener) {
        this.listener = listener;
    }

    @Override
    public void onTestStart(ITestResult result) {
        listener.testStarted(toDescription(result));
    }

    private TestDescription toDescription(ITestResult result) {
        return new TestDescription(result.getTestClass().getName(), result.getMethod().getMethodName());
    }

    private TestFailure toFailure(ITestResult result) {
        return new TestFailure(toDescription(result), result.getThrowable());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        listener.testFinished(toDescription(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        listener.testFailure(toFailure(result));
        listener.testFinished(toDescription(result));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        listener.testIgnored(toDescription(result));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStart(ITestContext context) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onFinish(ITestContext context) {
        // TODO Auto-generated method stub

    }
}
