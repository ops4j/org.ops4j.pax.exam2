/*
 * Copyright 2016 Harald Wellmann.
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
package org.ops4j.pax.exam.invoker.junit5;

import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_TEST;

import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.InjectorFactory;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.ops4j.spi.ServiceProviderFinder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * @author hwellmann
 *
 */
public class ContainerExtension implements CombinedExtension {

    @Override
    public void beforeAll(ContainerExtensionContext context) throws Exception {
        // empty
    }

    @Override
    public void afterAll(ContainerExtensionContext context) throws Exception {
        // empty
    }

    @Override
    public void beforeEach(TestExtensionContext context) throws Exception {
        // empty
    }

    @Override
    public void afterEach(TestExtensionContext context) throws Exception {
        // empty
    }

    @Override
    public void postProcessTestInstance(TestExtensionContext context) throws Exception {
        ConfigurationManager cm = new ConfigurationManager();
        String systemType = cm.getProperty(Constants.EXAM_SYSTEM_KEY);
        if (systemType.equals(EXAM_SYSTEM_TEST)) {
            BundleContext bc = FrameworkUtil.getBundle(getClass()).getBundleContext();
            Injector injector = ServiceLookup.getService(bc, Injector.class);
            injector.injectFields(context.getTestInstance());
        }
        else {
            InjectorFactory injectorFactory = ServiceProviderFinder
                .loadUniqueServiceProvider(InjectorFactory.class);
            Injector injector = injectorFactory.createInjector();
            injector.injectFields(context.getTestInstance());
        }
    }
}
