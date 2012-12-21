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
package org.ops4j.pax.exam.regression.multi.unresolved;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * 
 * @author Harald Wellmann
 * 
 */
public class UnresolvedBundleInvokerTest {

    @Test
    public void exceptionOnUnresolvedBundle() {

        JUnitCore junit = new JUnitCore();
        Result result = junit.run(UnresolvedBundleTestWrapped.class);
        assertThat(result.getFailureCount(), is(1));

        for (Failure failure : result.getFailures()) {
            assertThat(failure.getMessage(), containsString("There are unresolved bundles"));
        }
    }
}
