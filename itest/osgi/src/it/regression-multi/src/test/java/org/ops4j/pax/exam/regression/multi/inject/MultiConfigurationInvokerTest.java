/*
 * Copyright 2012 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.regression.multi.inject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.isEquinox;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.isNativeContainer;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

/**
 * Check that tests get invoked once per configuration.
 * 
 * Regression test for PAXEXAM-427.
 * 
 * @author Harald Wellmann
 */
public class MultiConfigurationInvokerTest {

    @Test
    public void invokeMultiConfigurationTest() {
        JUnitCore junit = new JUnitCore();
        Result result = junit.run(MultiConfigurationTest.class);
        assertThat(result.getRunCount(), is((2)));
        assertThat(result.getFailureCount(), is((0)));
    }

    @Test
    public void invokeSingleTestMethod() {
        assumeTrue(isNativeContainer());
        assumeTrue(isEquinox());

        JUnitCore junit = new JUnitCore();
        String method = "getServiceFromInjectedBundleContext";
        String klass = MultiConfigurationTest.class.getName();
        // when there is more than one configuration, test method names are mangled
        String testName = String.format("%s:%s.%s:Native:EquinoxFactory[1]", method, klass, method);
        Request request = Request.method(MultiConfigurationTest.class, testName);
        Result result = junit.run(request);
        assertThat(result.getRunCount(), is((1)));
        assertThat(result.getFailureCount(), is((0)));
    }
}
