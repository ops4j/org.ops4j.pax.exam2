/*
 * Copyright 2011 Harald Wellmann
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
package org.ops4j.pax.exam.spring.impl;

import javax.enterprise.inject.spi.BeanManager;

import org.ops4j.pax.exam.util.Injector;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * Obtains a CDI {@link BeanManager} and injects fields into the given target.
 * 
 * @author Harald Wellmann
 * 
 */
public class SpringInjector implements Injector {

    private AutowireCapableBeanFactory beanFactory;

    /**
     * Constructs a SpringInjection using a given bean factory.
     * 
     * @param beanFactory
     *            Spring bean factory
     */
    public SpringInjector(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Injects dependencies into the given target object whose lifecycle is not managed by Spring
     * itself.
     * 
     * @param target
     *            an object with injection points
     */
    @Override
    public void injectFields(Object target) {
        beanFactory.autowireBean(target);
    }
}
