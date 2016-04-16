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
package org.ops4j.pax.exam.testng.servlet;

import java.lang.reflect.Method;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.kohsuke.MetaInfServices;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.util.Transactional;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;

/**
 * @author hwellmann
 *
 */
@MetaInfServices
public class TransactionalHookable implements IHookable {

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        if (isTransactional(testResult)) {
            runInTransaction(callBack, testResult);
        }
        else {
            callBack.runTestMethod(testResult);
        }
    }

    /**
     * Checks if the current test method is transactional.
     *
     * @param testResult
     *            TestNG method and result wrapper
     * @return true if the method or the enclosing class is annotated with {@link Transactional}.
     */
    private boolean isTransactional(ITestResult testResult) {
        boolean transactional = false;
        Method method = testResult.getMethod().getConstructorOrMethod().getMethod();
        if (method.getAnnotation(Transactional.class) != null) {
            transactional = true;
        }
        else {
            if (method.getDeclaringClass().getAnnotation(Transactional.class) != null) {
                transactional = true;
            }
        }
        return transactional;
    }

    /**
     * Runs a test method enclosed by a Java EE auto-rollback transaction obtained from the JNDI
     * context.
     *
     * @param callBack
     *            TestNG callback for test method
     * @param testResult
     *            test result container
     */
    private void runInTransaction(IHookCallBack callBack, ITestResult testResult) {
        UserTransaction tx = null;
        try {
            InitialContext ctx = new InitialContext();
            tx = (UserTransaction) ctx.lookup("java:comp/UserTransaction");
            tx.begin();
            callBack.runTestMethod(testResult);
        }
        catch (NamingException | NotSupportedException | SystemException exc) {
            throw new TestContainerException(exc);
        }
        finally {
            rollback(tx);
        }
    }

    /**
     * Rolls back the given transaction, if not null.
     *
     * @param tx
     *            transaction
     */
    private void rollback(UserTransaction tx) {
        if (tx != null) {
            try {
                tx.rollback();
            }
            catch (IllegalStateException | SecurityException | SystemException exc) {
                throw new TestContainerException(exc);
            }
        }
    }
}
