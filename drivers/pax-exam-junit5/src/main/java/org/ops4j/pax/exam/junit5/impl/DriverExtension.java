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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.invoker.junit5.CombinedExtension;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.InjectorFactory;
import org.ops4j.spi.ServiceProviderFinder;

/**
 * @author hwellmann
 *
 */
public class DriverExtension implements CombinedExtension {

    private static ReactorManager manager;
    private static StagedExamReactor stagedReactor;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        manager = ReactorManager.getInstance();
        stagedReactor = manager.getStagedReactor();
        manager.beforeClass(stagedReactor, context.getTestClass());
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
    public void afterAll(ExtensionContext context) throws Exception {
        manager.afterClass(stagedReactor, context.getTestClass().get());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        if (ReactorManager.getInstance().getSystemType().equals(Constants.EXAM_SYSTEM_CDI)) {
            InjectorFactory injectorFactory = ServiceProviderFinder
                .loadUniqueServiceProvider(InjectorFactory.class);
            Injector injector = injectorFactory.createInjector();
            injector.injectFields(testInstance);
        }
    }
}
