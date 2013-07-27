/*
 * Copyright 2013 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.junit.impl;

import java.lang.reflect.Method;

import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamFactory;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.ProbeBuilder;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.spi.reactors.AnnotationHandler;

@SuppressWarnings("deprecation")
public class JUnitLegacyAnnotationHandler implements AnnotationHandler {

    @Override
    public StagedExamReactorFactory createStagedReactorFactory(Class<?> klass)
        throws InstantiationException, IllegalAccessException {
        ExamReactorStrategy strategy = klass.getAnnotation(ExamReactorStrategy.class);
        StagedExamReactorFactory fact = null;
        if (strategy != null) {
            fact = strategy.value()[0].newInstance();
        }
        return fact;
    }

    @Override
    public TestContainerFactory createTestContainerFactory(Class<?> klass)
        throws InstantiationException, IllegalAccessException {
        ExamFactory f = klass.getAnnotation(ExamFactory.class);

        TestContainerFactory fact = null;
        if (f != null) {
            fact = f.value().newInstance();
        }
        return fact;
    }

    @Override
    public boolean isProbeBuilder(Method method) {
        return method.getAnnotation(ProbeBuilder.class) != null;
    }

    @Override
    public boolean isConfiguration(Method method) {
        return method.getAnnotation(Configuration.class) != null;
    }
}
