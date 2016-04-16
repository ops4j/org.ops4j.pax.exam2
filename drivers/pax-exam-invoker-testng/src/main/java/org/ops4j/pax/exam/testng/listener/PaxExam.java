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
package org.ops4j.pax.exam.testng.listener;

import java.util.List;

import org.ops4j.spi.ServiceProviderFinder;
import org.testng.IConfigureCallBack;
import org.testng.IHookCallBack;
import org.testng.IMethodInstance;
import org.testng.ISuite;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlTest;

/**
 * TestNG driver for Pax Exam, implementing a number of ITestNGListener interfaces. To run a TestNG
 * test class with Pax Exam, add this class as a listener to your test class:
 *
 * <pre>
 * &#064;Listeners(PaxExam.class)
 * public class MyTest {
 *
 *     &#064;BeforeMethod
 *     public void setUp() {
 *     }
 *
 *     &#064;AfterMethod
 *     public void tearDown() {
 *     }
 *
 *     &#064;Test
 *     public void test1() {
 *     }
 * }
 * </pre>
 *
 * In OSGi and Java EE modes, Pax Exam processes each test class twice, once by test driver and then
 * again inside the test container. The driver delegates each test method invocation to a probe
 * invoker which excutes the test method inside the container via the probe.
 * <p>
 * Dependencies annotated by {@link javax.inject.Inject} get injected into the test class in the
 * container (OSGi and Java EE modes) or by the driver (CDI mode).
 *
 * @author Harald Wellmann
 * @since 2.3.0
 *
 */
public class PaxExam implements CombinedListener {

    public static final String PAX_EXAM_SUITE_NAME = "PaxExamInternal";

    private int numMethodsInvoked;
    private int numMethodsPerClass;

    private CombinedListener delegate;

    private CombinedListener getDelegate(ISuite suite) {
        if (delegate == null) {
            initDelegate(isRunningInTestContainer(suite));
        }
        return delegate;
    }

    private CombinedListener getDelegate(XmlTest xmlTest) {
        if (delegate == null) {
            initDelegate(isRunningInTestContainer(xmlTest));
        }
        return delegate;
    }

    /**
     * @param runningInTestContainer
     */
    private void initDelegate(boolean runningInTestContainer) {
        if (runningInTestContainer) {
            delegate = new ContainerListener();
        }
        else {
            delegate = ServiceProviderFinder.findAnyServiceProvider(CombinedListener.class);
        }
    }

    /**
     * Are we running in the test container or directly under the driver?
     *
     * @return true if running in container
     */
    private boolean isRunningInTestContainer(ISuite suite) {
        return PAX_EXAM_SUITE_NAME.equals(suite.getName());
    }

    private boolean isRunningInTestContainer(XmlTest xmlTest) {
        return PAX_EXAM_SUITE_NAME.equals(xmlTest.getSuite().getName());
    }

    /**
     * Called by TestNG before the suite starts. When running in the container, this is a no op.
     * Otherwise, we create and stage the reactor.
     *
     * @param suite
     *            test suite
     */
    @Override
    public void onStart(ISuite suite) {
       getDelegate(suite).onStart(suite);
    }

    /**
     * Called by TestNG after the suite has finished. When running in the container, this is a no
     * op. Otherwise, we stop the reactor.
     *
     * @param suite
     *            test suite
     */
    @Override
    public void onFinish(ISuite suite) {
        getDelegate(suite).onFinish(suite);
    }

    /**
     * NOTE: This callback is invoked once per method, so we count the methods per class to
     * fire our internal event only once.
     */
    @Override
    public void onBeforeClass(ITestClass testClass, IMethodInstance mi) {
        if (numMethodsInvoked == 0) {
            numMethodsPerClass = testClass.getTestMethods().length;
            onceBeforeClass(testClass, mi);
        }
    }

    private void onceBeforeClass(ITestClass testClass, IMethodInstance mi) {
        getDelegate(testClass.getXmlTest()).onBeforeClass(testClass, mi);
    }

    /**
     * NOTE: This callback is invoked once per method, so we count the methods per class to
     * fire our internal event only once.
     */
    @Override
    public void onAfterClass(ITestClass testClass, IMethodInstance mi) {
        numMethodsInvoked++;
        if (numMethodsInvoked == numMethodsPerClass) {
            onceAfterClass(testClass, mi);
            numMethodsInvoked = 0;
        }
    }

    /**
     * @param testClass
     * @param mi
     */
    private void onceAfterClass(ITestClass testClass, IMethodInstance mi) {
        getDelegate(testClass.getXmlTest()).onAfterClass(testClass, mi);
    }

    /**
     * Callback from TestNG which lets us intercept a test method invocation. The two cases of
     * running in the container or under the driver are handled in separate methods.
     */
    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        getDelegate(testResult.getTestClass().getXmlTest()).run(callBack, testResult);
    }

    /**
     * Leave test methods unchanged when running in the container.
     */
    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> testMethods, ITestContext context) {
        return getDelegate(context.getCurrentXmlTest()).intercept(testMethods, context);
    }

    /**
     * Ignore configuration methods when running under the driver.
     */
    @Override
    public void run(IConfigureCallBack callBack, ITestResult testResult) {
        getDelegate(testResult.getTestClass().getXmlTest()).run(callBack, testResult);
    }
}
