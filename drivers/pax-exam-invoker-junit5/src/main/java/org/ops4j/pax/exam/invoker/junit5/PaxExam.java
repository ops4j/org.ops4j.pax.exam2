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

import java.util.Iterator;
import java.util.ServiceLoader;

import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.TestExtensionContext;

/**
 * @author hwellmann
 *
 */
public class PaxExam implements CombinedExtension {

    private CombinedExtension getDelegate(ExtensionContext context) {
        return (CombinedExtension) context.getStore().getOrComputeIfAbsent("delegate", this::createDelegate);
    }

    private CombinedExtension createDelegate(Object o) {
        ServiceLoader<CombinedExtension> loader = ServiceLoader.load(CombinedExtension.class, PaxExam.class.getClassLoader());
        Iterator<CombinedExtension> it = loader.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        else {
            return new ContainerExtension();
        }
    }

    @Override
    public void postProcessTestInstance(TestExtensionContext context) throws Exception {
        getDelegate(context).postProcessTestInstance(context);
    }

    @Override
    public void afterEach(TestExtensionContext context) throws Exception {
        getDelegate(context).afterEach(context);
    }

    @Override
    public void beforeEach(TestExtensionContext context) throws Exception {
        getDelegate(context).beforeEach(context);
    }

    @Override
    public void afterAll(ContainerExtensionContext context) throws Exception {
        getDelegate(context).afterAll(context);
    }

    @Override
    public void beforeAll(ContainerExtensionContext context) throws Exception {
        getDelegate(context).beforeAll(context);
    }
}
