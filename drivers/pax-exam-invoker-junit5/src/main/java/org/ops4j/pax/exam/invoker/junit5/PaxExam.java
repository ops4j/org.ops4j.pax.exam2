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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

/**
 * @author hwellmann
 *
 */
public class PaxExam implements CombinedExtension {

    private CombinedExtension getDelegate(ExtensionContext context) {
        String key;
        if (context.getStore(Namespace.GLOBAL).get("container") == null) {
            key = "driverDelegate";
        }
        else {
            key = "containerDelegate";
        }
        return (CombinedExtension) context.getStore(Namespace.GLOBAL).getOrComputeIfAbsent(key, k -> createDelegate(k, context));
    }

    private CombinedExtension createDelegate(String key, ExtensionContext context) {
        if ("containerDelegate".equals(key)) {
            return new ContainerExtension();
        }
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
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        getDelegate(context).postProcessTestInstance(testInstance, context);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        getDelegate(context).afterEach(context);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        getDelegate(context).beforeEach(context);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        getDelegate(context).afterAll(context);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        getDelegate(context).beforeAll(context);
    }
}
