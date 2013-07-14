/*
 * Copyright 2013 Harald Wellmann
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
package org.ops4j.pax.exam.servlet;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.Transactional;

public class ParameterizedContainerTestRunner extends BlockJUnit4ClassRunner {

    private Injector injector;
    private boolean transactionalClass;
    private Object[] parameters;

    public ParameterizedContainerTestRunner(Class<?> klass, Injector injector, Integer index)
        throws InitializationError {
        super(klass);
        this.injector = injector;
        transactionalClass = klass.getAnnotation(Transactional.class) != null;
        if (index != null) {
            try {
                Iterator<Object[]> it = allParameters().iterator();
                for (int i = 0; i <= index; i++) {
                    parameters = it.next();
                }
            }
            catch (Throwable t) {
                throw new InitializationError(Collections.singletonList(t));
            }
        }
    }

    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = null;
        if (fieldsAreAnnotated()) {
            test = createTestUsingFieldInjection();
        }
        else {
            test = createTestUsingConstructorInjection();
        }
        injector.injectFields(test);
        return test;
    }

    private Object createTestUsingConstructorInjection() throws Exception {
        return getTestClass().getOnlyConstructor().newInstance(parameters);
    }

    private Object createTestUsingFieldInjection() throws Exception {
        List<FrameworkField> annotatedFieldsByParameter = getAnnotatedFieldsByParameter();
        if (annotatedFieldsByParameter.size() != parameters.length) {
            throw new Exception("Wrong number of parameters and @Parameter fields."
                + " @Parameter fields counted: " + annotatedFieldsByParameter.size()
                + ", available parameters: " + parameters.length + ".");
        }
        Object testClassInstance = getTestClass().getJavaClass().newInstance();
        for (FrameworkField each : annotatedFieldsByParameter) {
            Field field = each.getField();
            Parameter annotation = field.getAnnotation(Parameter.class);
            int index = annotation.value();
            try {
                field.set(testClassInstance, parameters[index]);
            }
            catch (IllegalArgumentException iare) {
                throw new Exception(getTestClass().getName() + ": Trying to set " + field.getName()
                    + " with the value " + parameters[index] + " that is not the right type ("
                    + parameters[index].getClass().getSimpleName() + " instead of "
                    + field.getType().getSimpleName() + ").", iare);
            }
        }
        return testClassInstance;
    }

    private List<FrameworkField> getAnnotatedFieldsByParameter() {
        return getTestClass().getAnnotatedFields(Parameter.class);
    }

    private boolean fieldsAreAnnotated() {
        return !getAnnotatedFieldsByParameter().isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Iterable<Object[]> allParameters() throws Throwable {
        Object parameters = getParametersMethod().invokeExplosively(null);
        if (parameters instanceof Iterable) {
            return (Iterable<Object[]>) parameters;
        }
        else {
            throw parametersMethodReturnedWrongType();
        }
    }

    private Exception parametersMethodReturnedWrongType() throws Exception {
        String className = getTestClass().getName();
        String methodName = getParametersMethod().getName();
        String message = MessageFormat.format("{0}.{1}() must return an Iterable of arrays.",
            className, methodName);
        return new Exception(message);
    }

    private FrameworkMethod getParametersMethod() throws Exception {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Parameters.class);
        for (FrameworkMethod each : methods) {
            if (each.isStatic() && each.isPublic()) {
                return each;
            }
        }

        throw new Exception("No public static parameters method on class "
            + getTestClass().getName());
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        boolean transactional = isTransactional(method);
        if (transactional) {
            runInTransaction(method, notifier);
        }
        else {
            super.runChild(method, notifier);
        }
    }

    private void runInTransaction(FrameworkMethod method, RunNotifier notifier) {
        UserTransaction tx = null;
        EachTestNotifier eachNotifier = makeNotifier(method, notifier);
        if (method.getAnnotation(Ignore.class) != null) {
            eachNotifier.fireTestIgnored();
            return;
        }

        eachNotifier.fireTestStarted();
        try {
            InitialContext ctx = new InitialContext();
            tx = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
            tx.begin();
            methodBlock(method).evaluate();
        }
        catch (NamingException exc) {
            eachNotifier.addFailure(exc);
        }
        catch (NotSupportedException exc) {
            eachNotifier.addFailure(exc);
        }
        catch (SystemException exc) {
            eachNotifier.addFailure(exc);
        }
        catch (AssumptionViolatedException e) {
            eachNotifier.addFailedAssumption(e);
        }
        // CHECKSTYLE:SKIP : base class API
        catch (Throwable e) {
            eachNotifier.addFailure(e);
        }
        finally {
            rollback(tx, eachNotifier);
            eachNotifier.fireTestFinished();
        }
    }

    private void rollback(UserTransaction tx, EachTestNotifier eachNotifier) {
        if (tx != null) {
            try {
                tx.rollback();
            }
            catch (IllegalStateException exc) {
                eachNotifier.addFailure(exc);
            }
            catch (SecurityException exc) {
                eachNotifier.addFailure(exc);
            }
            catch (SystemException exc) {
                eachNotifier.addFailure(exc);
            }
        }
    }

    private boolean isTransactional(FrameworkMethod method) {
        return (method.getAnnotation(Transactional.class) != null) || transactionalClass;
    }

    private EachTestNotifier makeNotifier(FrameworkMethod method, RunNotifier notifier) {
        Description description = describeChild(method);
        return new EachTestNotifier(notifier, description);
    }

}
