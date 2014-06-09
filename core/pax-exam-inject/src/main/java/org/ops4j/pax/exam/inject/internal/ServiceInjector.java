/*
 * Copyright 2011 Harald Wellmann.
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
package org.ops4j.pax.exam.inject.internal;

import static org.ops4j.pax.exam.Constants.EXAM_SERVICE_TIMEOUT_DEFAULT;
import static org.ops4j.pax.exam.Constants.EXAM_SERVICE_TIMEOUT_KEY;

import java.lang.reflect.Field;

import javax.inject.Inject;

import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.util.Filter;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;

/**
 * Injects services into all fields of the given class annotated with {@link Inject}. This includes
 * the fields of all superclasses. By default, the Injector will wait for a given timeout if no
 * matching service is available.
 * <p>
 * If the field has no {@link Filter} annotation, the service will be looked up by type with a
 * filter {@code (objectClass=<type>)}. If the field has {@link Filter} annotation with a non empty
 * value, this partial filter string will be taken to build a composite filter of the form
 * {@code (&(objectClass=<type>)<filter>)}.
 * <p>
 * If there is more than one matching service, the first one obtained from the service registry will
 * be injected.
 * <p>
 * Fields of type {@link BundleContext} will be injected with the BundleContext passed to this
 * Injector.
 * <p>
 * Constructor, setter or parameter injection is not supported.
 * 
 * @author Harald Wellmann
 * @since 2.3.0, August 2011
 */
public class ServiceInjector implements Injector {

    public void injectFields(Object target) {
        Class<?> targetClass = target.getClass();
        while (targetClass != Object.class) {
            injectDeclaredFields(target, targetClass);
            targetClass = targetClass.getSuperclass();
        }
    }

    private void injectDeclaredFields(Object target, Class<?> targetClass) {
        for (Field field : targetClass.getDeclaredFields()) {
            if (field.getAnnotation(Inject.class) != null) {
                injectField(target, targetClass, field);
            }
        }
    }

    private void injectField(Object target, Class<?> targetClass, Field field) {
        Class<?> type = field.getType();
        String filterString = "";
        String timeoutProp = System.getProperty(EXAM_SERVICE_TIMEOUT_KEY,
            EXAM_SERVICE_TIMEOUT_DEFAULT);
        long timeout = Integer.parseInt(timeoutProp);
        Filter filter = field.getAnnotation(Filter.class);
        if (filter != null) {
            filterString = filter.value();
            timeout = filter.timeout();
        }

        // Retrieve bundle Context just before calling getService to avoid that the bundle restarts
        // in between
        BundleContext bc = getBundleContext(targetClass, timeout);
        Object service = (BundleContext.class == type) ? bc : ServiceLookup.getService(bc, type,
            timeout, filterString);
        setField(target, field, service);
    }

    private void setField(Object target, Field field, Object service) {
        try {
            if (field.isAccessible()) {
                field.set(target, service);
            }
            else {
                field.setAccessible(true);
                try {
                    field.set(target, service);
                }
                finally {
                    field.setAccessible(false);
                }
            }
        }
        catch (IllegalAccessException exc) {
            throw new RuntimeException(exc);
        }
    }

    private BundleContext getBundleContext(Class<?> klass, long timeout) {
        try {
            BundleReference bundleRef = BundleReference.class.cast(klass.getClassLoader());
            Bundle bundle = bundleRef.getBundle();
            return getBundleContext(bundle, timeout);
        }
        catch (ClassCastException exc) {
            throw new TestContainerException("class " + klass.getName()
                + " is not loaded from an OSGi bundle");
        }
    }

    /**
     * Retrieve bundle context from given bundle. If the bundle is being restarted the bundle
     * context can be null for some time
     * 
     * @param bundle
     * @param timeout TODO
     * @return bundleContext or exception if bundleContext is null after timeout
     */
    private BundleContext getBundleContext(Bundle bundle, long timeout) {
        long endTime = System.currentTimeMillis() + timeout;
        BundleContext bc = null;
        while (bc == null) {
            bc = bundle.getBundleContext();
            if (bc == null) {
                if (System.currentTimeMillis() >= endTime) {
                    throw new TestContainerException(
                        "Unable to retrieve bundle context from bundle " + bundle);
                }
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    // Ignore
                }
            }
        }
        return bc;
    }

}
