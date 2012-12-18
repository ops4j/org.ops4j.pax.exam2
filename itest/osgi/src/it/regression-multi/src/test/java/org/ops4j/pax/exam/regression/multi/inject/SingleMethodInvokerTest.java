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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests that we can invoke a single test method from a class by name. Regression test for
 * PAXEXAM-425.
 * 
 * @author Harald Wellmann
 */
public class SingleMethodInvokerTest {

    private static final Logger LOG = LoggerFactory.getLogger(SingleMethodInvokerTest.class);

    @Test
    public void invokeSingleTestMethod() {
        assumeTrue(isNativeContainer());
        assumeTrue(isEquinox());

        JUnitCore junit = new JUnitCore();
        String method = "getInjectedServices";
        Request request = Request.method(FilterTest.class, method);
        Result result = junit.run(request);
        if (result.getFailureCount() > 0) {
            LOG.error(result.getFailures().get(0).getTrace());
        }
        assertThat(result.getFailureCount(), is((0)));
    }
}
