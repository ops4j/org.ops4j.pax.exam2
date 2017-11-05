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
package org.junit.gen5.engine.junit5.descriptor;

import java.lang.reflect.AnnotatedElement;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.AbstractExtensionContext;

/**
 * @author hwellmann
 *
 */
public class WrappedExtensionContext extends AbstractExtensionContext {


    /**
     *
     */
    public WrappedExtensionContext(ExtensionContext delegate) {
        super(delegate, null, null);
    }

    @Override
    public String getUniqueId() {
        return getParent().get().getUniqueId();
    }

    @Override
    public String getName() {
        return getParent().get().getName();
    }

    @Override
    public String getDisplayName() {
        return getParent().get().getDisplayName();
    }

    @Override
    public Class<?> getTestClass() {
        return getParent().get().getTestClass();
    }

    @Override
    public AnnotatedElement getElement() {
        return getParent().get().getElement();
    }

    @Override
    public TestDescriptor getTestDescriptor() {
        AbstractExtensionContext delegate = (AbstractExtensionContext) getParent().get();
        return delegate.getTestDescriptor();
    }
}
