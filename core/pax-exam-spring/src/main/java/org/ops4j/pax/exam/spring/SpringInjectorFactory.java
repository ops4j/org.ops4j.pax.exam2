/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.spring;

import javax.servlet.ServletContext;

import org.ops4j.pax.exam.spring.impl.SpringInjector;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.InjectorFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SpringInjectorFactory implements InjectorFactory {

    private ServletContext servletContext;

    public void setContext(Object context) {
        servletContext = (ServletContext) context;
    }

    @Override
    public Injector createInjector() {
        assert servletContext != null;
        WebApplicationContext appContext = WebApplicationContextUtils
            .getWebApplicationContext(servletContext);
        SpringInjector injector = new SpringInjector(appContext.getAutowireCapableBeanFactory());

        return injector;
    }
}
