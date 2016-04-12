/*
 * Copyright 2016 Harald Wellmann
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

import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.testng.IHookCallBack;
import org.testng.IMethodInstance;
import org.testng.ISuite;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestResult;

/**
 * TestNG listener delegate running within the test container.
 *
 * @author Harald Wellmann
 * @since 5.0.0
 *
 */
class ContainerListener implements CombinedListener {

    /**
     * Runs a test method in the container. We wrap the method in an auto-rollback transaction
     * if required.
     *
     * @param callBack
     *            TestNG callback for test method
     * @param testResult
     *            test result container
     */
    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        Object testClassInstance = testResult.getInstance();
        inject(testClassInstance);
        callBack.runTestMethod(testResult);
    }

    /**
     * Performs field injection on the given object. The injection method is looked up via the Java
     * SE service loader.
     *
     * @param testClassInstance
     *            test class instance
     */
    private void inject(Object testClassInstance) {
        BundleContext bc = FrameworkUtil.getBundle(getClass()).getBundleContext();
        Injector injector = ServiceLookup.getService(bc, Injector.class);
        injector.injectFields(testClassInstance);
    }

    @Override
    public void onStart(ISuite suite) {
    }

    @Override
    public void onFinish(ISuite suite) {
    }

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        return methods;
    }

    @Override
    public void onBeforeClass(ITestClass testClass, IMethodInstance mi) {
    }

    @Override
    public void onAfterClass(ITestClass testClass, IMethodInstance mi) {
    }
}
