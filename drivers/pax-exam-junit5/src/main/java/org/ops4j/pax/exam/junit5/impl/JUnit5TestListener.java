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
package org.ops4j.pax.exam.junit5.impl;

import java.util.Optional;

import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;

/**
 * @author Harald Wellmann
 *
 */
public class JUnit5TestListener implements TestListener {

    private EngineExecutionListener engineListener;
    private TestDescriptor root;

    public JUnit5TestListener(EngineExecutionListener engineListener, TestDescriptor root) {
        this.engineListener = engineListener;
        this.root = root;
    }

    @Override
    public void testStarted(TestDescription description) {
        TestDescriptor descriptor = findTestDescriptor(description);
        if (descriptor != null) {
            engineListener.executionStarted(descriptor);
        }
    }

    @Override
    public void testFinished(TestDescription description) {
        TestDescriptor descriptor = findTestDescriptor(description);
        if (descriptor != null) {
            engineListener.executionFinished(descriptor, TestExecutionResult.successful());
        }
    }

    @Override
    public void testFailure(TestFailure failure) {
        TestDescriptor descriptor = findTestDescriptor(failure.getDescription());
        if (descriptor != null) {
            engineListener.executionFinished(descriptor, TestExecutionResult.failed(failure.getException()));
        }
    }

    @Override
    public void testAssumptionFailure(TestFailure failure) {
        // TODO Auto-generated method stub

    }

    @Override
    public void testIgnored(TestDescription description) {
        // TODO Auto-generated method stub

    }


    private TestDescriptor findTestDescriptor(TestDescription testDescription) {
        Optional<? extends TestDescriptor> opt = root.getChildren().stream().filter(d -> hasClassName(d, testDescription.getClassName())).findAny();
        if (!opt.isPresent()) {
            return null;
        }
        if (testDescription.getMethodName() == null) {
            return opt.get();
        }
        return opt.get().getChildren().stream().filter(d -> hasMethodName(d, testDescription.getMethodName())).findAny().orElse(null);
    }

    private boolean hasClassName(TestDescriptor descriptor, String className) {
        if (descriptor instanceof ClassTestDescriptor) {
            ClassTestDescriptor classDescriptor = (ClassTestDescriptor) descriptor;
            return classDescriptor.getTestClass().getName().equals(className);
        }
        return false;
    }

    private boolean hasMethodName(TestDescriptor descriptor, String className) {
        if (descriptor instanceof TestMethodTestDescriptor) {
            TestMethodTestDescriptor methodDescriptor = (TestMethodTestDescriptor) descriptor;
            return methodDescriptor.getTestMethod().getName().equals(className);
        }
        return false;
    }
}
