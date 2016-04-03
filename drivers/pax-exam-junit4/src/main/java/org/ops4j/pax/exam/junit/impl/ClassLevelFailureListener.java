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
package org.ops4j.pax.exam.junit.impl;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;


/**
 * @author hwellmann
 *
 */
public class ClassLevelFailureListener implements TestListener {
    
    
    private RunNotifier notifier;

    public ClassLevelFailureListener(RunNotifier notifier) {
        this.notifier = notifier;
    }

    @Override
    public void testStarted(TestDescription description) {
        //notifier.fireTestStarted(convertDescription(description));
    }

    @Override
    public void testFinished(TestDescription description) {
        //notifier.fireTestFinished(convertDescription(description));
    }

    @Override
    public void testFailure(TestFailure failure) {
        notifier.fireTestFailure(convertFailure(failure));
    }

    @Override
    public void testAssumptionFailure(TestFailure failure) {
        //notifier.fireTestAssumptionFailed(convertFailure(failure));
    }

    @Override
    public void testIgnored(TestDescription description) {
        //notifier.fireTestIgnored(convertDescription(description));
    }
    
    public static Description convertDescription(TestDescription description) {
        return Description.createTestDescription(description.getClassName(), description.getMethodName());
    }
    
    public static Failure convertFailure(TestFailure failure) {
        return new Failure(convertDescription(failure.getDescription()), failure.getException());
    }
}
