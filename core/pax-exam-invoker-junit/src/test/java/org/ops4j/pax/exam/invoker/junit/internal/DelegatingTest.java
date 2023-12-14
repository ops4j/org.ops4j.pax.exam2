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
package org.ops4j.pax.exam.invoker.junit.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.ops4j.pax.exam.util.Injector;
import org.osgi.framework.BundleContext;

@RunWith(MockitoJUnitRunner.class)
public class DelegatingTest {

    @Mock
    private Injector injector;

    @Mock
    private BundleContext bc;

    @Test
    public void switchRunner() {
        JUnitCore junit = new JUnitCore();
        Description method = Description.createTestDescription(SimpleTestHidden.class, "doNothing");
        Request classRequest = new ContainerTestRunnerClassRequest(SimpleTestHidden.class, injector, null);
        Request request = classRequest.filterWith(method);
        Result result = junit.run(request);
        assertTrue(result.getFailures().isEmpty());

        verify(injector).injectFields(isNotNull());
    }

    @Test
    public void wrapFailingTest() throws Throwable {
        JUnitCore junit = new JUnitCore();
        Description method = Description.createTestDescription(SimpleTestHidden.class,
            "failingTest");
        Request classRequest = new ContainerTestRunnerClassRequest(SimpleTestHidden.class, injector, null);
        Request request = classRequest.filterWith(method);
        Result result = junit.run(request);
        assertEquals(1, result.getFailures().size());
        verify(injector).injectFields(isNotNull());
    }
}
