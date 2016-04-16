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

import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_KEY;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_TEST;

import java.util.List;

import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.InjectorFactory;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.ops4j.spi.ServiceProviderFinder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.testng.IConfigureCallBack;
import org.testng.IHookCallBack;
import org.testng.IHookable;
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


    private String systemType;

    /**
     *
     */
    public ContainerListener() {
        ConfigurationManager cm = new ConfigurationManager();
        systemType = cm.getProperty(EXAM_SYSTEM_KEY, EXAM_SYSTEM_TEST);
    }


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

        IHookable hookable = ServiceProviderFinder.findAnyServiceProvider(IHookable.class);
        if (hookable == null) {
            callBack.runTestMethod(testResult);
        }
        else {
            hookable.run(callBack, testResult);
        }
    }

    /**
     * Performs field injection on the given object. The injection method is looked up via the Java
     * SE service loader.
     *
     * @param testClassInstance
     *            test class instance
     */
    private void inject(Object testClassInstance) {
        if (systemType.equals(EXAM_SYSTEM_TEST)) {
            BundleContext bc = FrameworkUtil.getBundle(getClass()).getBundleContext();
            Injector injector = ServiceLookup.getService(bc, Injector.class);
            injector.injectFields(testClassInstance);
        }
        else {
            InjectorFactory injectorFactory = ServiceProviderFinder
                .loadUniqueServiceProvider(InjectorFactory.class);
            Injector injector = injectorFactory.createInjector();
            injector.injectFields(testClassInstance);
        }
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

    @Override
    public void run(IConfigureCallBack callBack, ITestResult testResult) {
        callBack.runConfigurationMethod(testResult);
    }
}
