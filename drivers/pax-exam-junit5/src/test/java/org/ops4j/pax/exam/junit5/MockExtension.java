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
package org.ops4j.pax.exam.junit5;

import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.WrappedExtensionContext;
import org.ops4j.pax.exam.invoker.junit5.CombinedExtension;

/**
 * @author hwellmann
 *
 */
public class MockExtension implements CombinedExtension {

    @Override
    public void beforeAll(ContainerExtensionContext context) throws Exception {
        System.out.println("MockExtension beforeAll");

        WrappedExtensionContext wrapped = new WrappedExtensionContext(context);
        TestDescriptor parentDescriptor = wrapped.getTestDescriptor().getParent().get();
        parentDescriptor.getChildren().stream().map(d -> d.getName()).forEach(System.out::println);

        context.getStore().getOrComputeIfAbsent("container", x -> true);
    }

    @Override
    public void afterAll(ContainerExtensionContext context) throws Exception {
        System.out.println("MockExtension afterAll");
    }

    @Override
    public void beforeEach(TestExtensionContext context) throws Exception {
        Object container = context.getStore().get("container");
        System.out.println("MockExtension beforeEach " + container);
    }

    @Override
    public void afterEach(TestExtensionContext context) throws Exception {
        System.out.println("MockExtension afterEach");
    }

    @Override
    public void postProcessTestInstance(TestExtensionContext context) throws Exception {
        System.out.println("MockExtension postProcess");
    }

}
