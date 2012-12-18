package org.ops4j.pax.exam.junit;

import java.lang.reflect.Method;

import org.ops4j.pax.exam.TestContainerFactory;
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
