/*
 * Copyright 2011 Harald Wellmann
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
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.Transactional;

public class ContainerTestRunner extends BlockJUnit4ClassRunner {

    private Injector injector;
    private boolean transactionalClass;

    public ContainerTestRunner(Class<?> klass, Injector injector) throws InitializationError {
        super(klass);
        this.injector = injector;
        transactionalClass = klass.getAnnotation(Transactional.class) != null;
    }

    @Override
    protected Object createTest() throws Exception {
        Object test = super.createTest();
        injector.injectFields(test);
        return test;
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
