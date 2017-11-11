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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.ops4j.pax.exam.invoker.junit5.CombinedExtension;

/**
 * @author hwellmann
 *
 */
public class MockExtension implements CombinedExtension {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        System.out.println("MockExtension beforeAll");
        context.getStore(Namespace.GLOBAL).getOrComputeIfAbsent("container", x -> true);
    }



    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        System.out.println("MockExtension afterAll");
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        Object container = context.getStore(Namespace.GLOBAL).get("container");
        System.out.println("MockExtension beforeEach " + container);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        System.out.println("MockExtension afterEach");
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        System.out.println("MockExtension postProcess");
    }

}
