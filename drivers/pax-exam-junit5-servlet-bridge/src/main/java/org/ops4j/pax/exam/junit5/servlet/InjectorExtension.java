/*
 * Copyright 2017 OPS4J Contributors
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
package org.ops4j.pax.exam.junit5.servlet;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.ops4j.pax.exam.util.Injector;

/**
 * @author Harald Wellmann
 *
 */
public class InjectorExtension implements TestInstancePostProcessor {

    private Injector injector;

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context)
        throws Exception {
        injector.injectFields(testInstance);
    }

}
