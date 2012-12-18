package org.ops4j.pax.exam.spi.reactors;

import java.lang.reflect.Method;

import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;

public interface AnnotationHandler {

    StagedExamReactorFactory createStagedReactorFactory(Class<?> klass)
        throws InstantiationException, IllegalAccessException;

    TestContainerFactory createTestContainerFactory(Class<?> klass) throws InstantiationException,
        IllegalAccessException;

    boolean isProbeBuilder(Method method);

    boolean isConfiguration(Method method);
}
