/*
 * Copyright 2009 Toni Menzel.
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.WrappedTestContainerException;
import org.ops4j.pax.exam.util.Injector;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class WrappedExceptionTest {

    @Test
    public void testSerializableException() throws ClassNotFoundException {
        try { 
            callWithMethod("serializable");
        } catch (WrappedTestContainerException e) {
            Assert.fail("Should not be wrapped");
        } catch (TestContainerException e) {
            Throwable cause = e.getCause();
            Assert.assertEquals("This should be serializable. So it should not be wrapped", cause.getMessage());
            Assert.assertEquals(RuntimeException.class, cause.getClass());
        }
    }
    
    @Test
    public void testNotSerializableException() throws ClassNotFoundException {
        try {
            callWithMethod("notSerializable");
        } catch (WrappedTestContainerException e) {
            Assert.assertEquals("This should not be serializable. So it should be wrapped", e.getWrappedMessage());
            Assert.assertEquals(MyNotSerializableException.class.getName(), e.getWrappedClassName());
        }
    }

    private void callWithMethod(String method) throws ClassNotFoundException {
        BundleContext bundleContext = mock(BundleContext.class);
        Injector injector = mock(Injector.class);
        Bundle bundle = mock(Bundle.class);
        when(bundleContext.getBundle()).thenReturn(bundle);
        when(bundle.loadClass(ExceptionSource.class.getName())).thenReturn(ExceptionSource.class);
        JUnitProbeInvoker invoker = new JUnitProbeInvoker(ExceptionSource.class.getName() + ";" + method, bundleContext, injector );
        invoker.call(new Object[]{});
    }
    
    public static class ExceptionSource {

        @Test
        public void serializable() {
            throw new RuntimeException("This should be serializable. So it should not be wrapped");
        }
        
        @Test
        public void notSerializable() {
            throw new MyNotSerializableException("This should not be serializable. So it should be wrapped");
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
}
