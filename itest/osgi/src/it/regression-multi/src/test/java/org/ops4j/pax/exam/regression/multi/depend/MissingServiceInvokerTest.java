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
package org.ops4j.pax.exam.regression.multi.depend;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.ops4j.pax.swissbox.tracker.ServiceLookupException;

/**
 * Regression test for PAXEXAM-493. Checks that a test with an unsatisfied injection point
 * fails with a meaningful exception message.
 * 
 * @author Harald Wellmann
 */
public class MissingServiceInvokerTest {

    @Test
    public void invokeMissingServiceTest() {
        JUnitCore junit = new JUnitCore();
        Result result = junit.run(MissingServiceTestWrapped.class);
        assertThat(result.getRunCount(), is(1));
        assertThat(result.getFailureCount(), is(1));
        Failure failure = result.getFailures().get(0);
        assertThat(failure.getException(), instanceOf(ServiceLookupException.class));
        assertThat(failure.getException().getMessage(), containsString("Runnable"));
    }
}
