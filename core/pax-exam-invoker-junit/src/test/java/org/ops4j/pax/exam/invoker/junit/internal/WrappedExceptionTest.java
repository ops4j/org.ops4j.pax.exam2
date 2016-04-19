/*
 * Copyright 2013 Christian Schneider
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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestFailure;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.WrappedTestContainerException;
import org.ops4j.pax.exam.util.Injector;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class WrappedExceptionTest {

    private static final String SHOULD_BE_WRAPPED = "This should not be serializable. So it should be wrapped";
    private static final String SHOULD_NOT_BE_WRAPPED = "This should be serializable. So it should not be wrapped";

    private TestFailure savedFailure;

    @Test
    public void testSerializableException() throws ClassNotFoundException {
        callWithMethod("serializable");
        assertThat(savedFailure.getException(), instanceOf(RuntimeException.class));
        assertThat(savedFailure.getException().getMessage(), is(SHOULD_NOT_BE_WRAPPED));
    }

    @Test
    public void testNotSerializableException() throws ClassNotFoundException {
        callWithMethod("notSerializable");
        assertThat(savedFailure.getException(), instanceOf(WrappedTestContainerException.class));
        assertThat(savedFailure.getException().getMessage(), is(SHOULD_BE_WRAPPED));
    }

    private void callWithMethod(String method) throws ClassNotFoundException {
        BundleContext bundleContext = mock(BundleContext.class);
        Injector injector = mock(Injector.class);
        Bundle bundle = mock(Bundle.class);
        when(bundleContext.getBundle()).thenReturn(bundle);
        doReturn(ExceptionSource.class).when(bundle).loadClass(ExceptionSource.class.getName());
        JUnitProbeInvoker invoker = new JUnitProbeInvoker(ExceptionSource.class.getName() + ";"
            + method, bundleContext, injector);
        TestDescription description = new TestDescription(ExceptionSource.class.getName(), method);
        invoker.runTest(description, new MyTestListener());
        assertThat(savedFailure, is(notNullValue()));
    }

    public static class ExceptionSource {

        @Test
        public void serializable() {
            throw new RuntimeException(SHOULD_NOT_BE_WRAPPED);
        }

        @Test
        public void notSerializable() {
            throw new MyNotSerializableException(SHOULD_BE_WRAPPED);
        }
    }

    public static class MyNotSerializableException extends RuntimeException {

        private static final long serialVersionUID = 6429496713575239757L;
        private NotSerializablePart detail;

        public MyNotSerializableException(String message) {
            super(message);
            this.detail = new NotSerializablePart();
        }

        public NotSerializablePart getDetail() {
            return detail;
        }

        public class NotSerializablePart {
        }
    }

    private class MyTestListener implements TestListener {

        @Override
        public void testStarted(TestDescription description) {
        }

        @Override
        public void testFinished(TestDescription description) {
        }

        @Override
        public void testFailure(TestFailure failure) {
            savedFailure = failure;
        }

        @Override
        public void testAssumptionFailure(TestFailure failure) {
            savedFailure = failure;
        }

        @Override
        public void testIgnored(TestDescription description) {
        }

    }

}
