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
package org.ops4j.pax.exam.junit5.impl;

import java.io.IOException;

import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.WrappedExtensionContext;
import org.kohsuke.MetaInfServices;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.invoker.junit5.CombinedExtension;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;

/**
 * @author hwellmann
 *
 */
@MetaInfServices
public class DriverExtension implements CombinedExtension {

    private static ReactorManager manager;
    private StagedExamReactor stagedReactor;

    @Override
    public void beforeAll(ContainerExtensionContext context) throws Exception {
        if (manager == null) {
            manager = ReactorManager.getInstance();
            WrappedExtensionContext wrapped = new WrappedExtensionContext(context);
            TestDescriptor parentDescriptor = wrapped.getTestDescriptor().getParent().get();
            parentDescriptor.getChildren().stream().forEach(this::storeTestClass);
            context.getStore().getOrComputeIfAbsent("container", x -> true);
            stagedReactor = manager.stageReactor();
        }
        manager.beforeClass(stagedReactor, context.getTestClass());
    }

    public void storeTestClass(TestDescriptor descriptor) {
        String className = descriptor.getName();
        Class<?> testClass = loadClass(className);
        Object testInstance = newInstance(testClass);
        ExamReactor examReactor = manager.prepareReactor(testClass, testInstance);
        addTestsToReactor(examReactor, testClass, testInstance);
    }



    private Class<?> loadClass(String className) {
        try {
            return DriverExtension.class.getClassLoader().loadClass(className);
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

    private void addTestsToReactor(ExamReactor reactor, Class<?> testClass, Object testClassInstance)
        {
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
    public void afterAll(ContainerExtensionContext context) throws Exception {
        manager.afterClass(stagedReactor, context.getTestClass());
    }

    @Override
    public void beforeEach(TestExtensionContext context) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void afterEach(TestExtensionContext context) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void postProcessTestInstance(TestExtensionContext context) throws Exception {
        // TODO Auto-generated method stub

    }

}
