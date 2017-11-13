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
package org.junit.jupiter.engine;

import java.io.IOException;

import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.DelegatingExecutionExtension;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.hierarchical.EngineExecutionContext;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;
import org.ops4j.pax.exam.util.Exceptions;

/**
 * @author Harald Wellmann
 *
 */
public class PaxExamDelegatingExecutionExtension implements DelegatingExecutionExtension {

    private EngineExecutionListener engineListener;
    private TestDescriptor root;
    private ConfigurationParameters configurationParameters;
    private ReactorManager manager;
    private StagedExamReactor stagedReactor;
    private Boolean isDelegating;

    public PaxExamDelegatingExecutionExtension(ExecutionRequest executionRequest) {
        this.engineListener = executionRequest.getEngineExecutionListener();
        this.root = executionRequest.getRootTestDescriptor();
        this.configurationParameters = executionRequest.getConfigurationParameters();
        this.isDelegating = configurationParameters.getBoolean("pax.exam.delegating").orElse(false);
    }

    @Override
    public void before(EngineExecutionContext executionContext, TestDescriptor testDescriptor) {
        if (testDescriptor instanceof JupiterEngineDescriptor) {
            if (!isDelegating && manager == null) {
                manager = ReactorManager.getInstance();
                testDescriptor.getChildren().stream().forEach(this::storeTestClass);
                stagedReactor = manager.stageReactor();
                manager.beforeSuite(stagedReactor);
            }
        }
        else if (testDescriptor instanceof ClassTestDescriptor) {
            if (isDelegating) {
                ((JupiterEngineExecutionContext) executionContext).getExtensionContext()
                    .getStore(Namespace.GLOBAL).getOrComputeIfAbsent("container", x -> true);
            }
        }
    }

    public void storeTestClass(TestDescriptor descriptor) {
        ClassSource source = (ClassSource) descriptor.getSource().get();
        Class<?> testClass = loadClass(source.getClassName());
        Object testInstance = newInstance(testClass);
        ExamReactor examReactor = manager.prepareReactor(testClass, testInstance);
        addTestsToReactor(examReactor, testClass, testInstance);
    }

    private Class<?> loadClass(String className) {
        try {
            return getClass().getClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException exc) {
            throw new TestContainerException(exc);
        }
    }

    private Object newInstance(Class<?> klass) {
        try {
            return klass.newInstance();
        }
        catch (InstantiationException | IllegalAccessException exc) {
            throw new TestContainerException(exc);
        }
    }

    private void addTestsToReactor(ExamReactor reactor, Class<?> testClass,
        Object testClassInstance) {
        try {
            TestProbeBuilder probe = manager.createProbeBuilder(testClassInstance);
            probe.addTest(testClass);
            reactor.addProbe(probe);
        }
        catch (IOException | ExamConfigurationException exc) {
            throw new TestContainerException(exc);
        }
    }

    @Override
    public void after(EngineExecutionContext executionContext, TestDescriptor testDescriptor) {
        if (!isDelegating && testDescriptor instanceof JupiterEngineDescriptor) {
            manager.afterSuite(stagedReactor);
        }
    }

    @Override
    public boolean shouldDelegate(TestDescriptor testDescriptor) {
        if (isDelegating()) {
            return false;
        }

        if (ReactorManager.getInstance().getSystemType().equals(Constants.EXAM_SYSTEM_CDI)) {
            return false;
        }

        if (testDescriptor instanceof TestMethodTestDescriptor) {
            return true;
        }

        if (testDescriptor instanceof ClassTestDescriptor) {
            return true;
        }

        return false;
    }

    @Override
    public void delegate(TestDescriptor testDescriptor) {
        if (testDescriptor instanceof TestMethodTestDescriptor) {
            delegateTestMethod((TestMethodTestDescriptor) testDescriptor);
        }
        else if (testDescriptor instanceof ClassTestDescriptor) {
            delegateTestClass((ClassTestDescriptor) testDescriptor);
        }
    }

    private void delegateTestClass(ClassTestDescriptor classDescriptor) {
        TestListener listener = new JUnit5TestListener(engineListener, root);
        try {
            ReactorManager.getInstance().getStagedReactor()
                .runTest(new TestDescription(classDescriptor.getTestClass().getName()), listener);
        }
        // CHECKSTYLE:SKIP : StagedExamReactor API
        catch (Exception exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    private void delegateTestMethod(TestMethodTestDescriptor methodDescriptor) {
        TestListener listener = new JUnit5TestListener(engineListener, root);
        try {
            TestDescription testDescription = new TestDescription(
                methodDescriptor.getTestClass().getName(),
                methodDescriptor.getTestMethod().getName());
            ReactorManager.getInstance().getStagedReactor().runTest(testDescription, listener);
        }
        // CHECKSTYLE:SKIP : StagedExamReactor API
        catch (Exception exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    private boolean isDelegating() {
        return configurationParameters.getBoolean("pax.exam.delegating").orElse(false);
    }
}
