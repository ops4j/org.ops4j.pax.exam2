/*
 * Copyright 2019 Nikolas Falco
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestFilter;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.util.Injector;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@RunWith(MockitoJUnitRunner.class)
public class JUnitProbeInvokerTest {
    @Mock
    private BundleContext bundleContext;
    @Mock
    private Injector injector;
    @Mock
    private Bundle bundle;

    @Test
    public void testCustomFilter() throws Exception {
        when(bundleContext.getBundle()).thenReturn(bundle);
        doReturn(CustomFilterSimpleTestHidden.class).when(bundle).loadClass(CustomFilterSimpleTestHidden.class.getName());
        JUnitProbeInvoker invoker = new JUnitProbeInvoker(bundleContext, injector);

        TestFilter customFilter = createCustomFilter(CustomFilterSimpleTestHidden.class, new DescriptionPredicate() {
            @Override
            public boolean accept(Description description) {
                return !description.getMethodName().startsWith("_");
            }
        });

        TestDescription description = new TestDescription(CustomFilterSimpleTestHidden.class.getName(), null, null, customFilter);
        TestListener listener = mock(TestListener.class);
        invoker.runTest(description, listener);

        verify(listener, times(3)).testStarted(any(TestDescription.class));
    }

    interface DescriptionPredicate {
        boolean accept(Description description);
    }

    private TestFilter createCustomFilter(Class<?> testClass, DescriptionPredicate predicate) throws Exception {
        final List<String> uniqueIds = new ArrayList<>();
        Filter customFilter = new Filter() {
            @Override
            public boolean shouldRun(Description description) {
                if (predicate.accept(description)) {
                    uniqueIds.add(String.valueOf(description.hashCode()));
                    return true;
                };
                return false;
            }

            @Override
            public String describe() {
                return "custom filter";
            }
        };
        new BlockJUnit4ClassRunner(testClass).filter(customFilter);
        TestFilter testFilter = new TestFilter(customFilter.describe(), uniqueIds);
        return testFilter;
    }

}
