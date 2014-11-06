/*
 * Copyright 2012 Harald Wellmann
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
package org.ops4j.pax.exam.tomcat;

import static org.ops4j.pax.exam.spi.container.ContainerConstants.BEAN_MANAGER_NAME;
import static org.ops4j.pax.exam.spi.container.ContainerConstants.BEAN_MANAGER_TYPE;
import static org.ops4j.pax.exam.spi.container.ContainerConstants.OWB_MANAGER_FACTORY;
import static org.ops4j.pax.exam.spi.container.ContainerConstants.OWB_SERVLET_LISTENER;
import static org.ops4j.pax.exam.spi.container.ContainerConstants.WELD_MANAGER_FACTORY;
import static org.ops4j.pax.exam.spi.container.ContainerConstants.WELD_SERVLET_LISTENER;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.ops4j.pax.exam.ConfigurationManager;

public class TomcatContextConfig extends ContextConfig {

    private ConfigurationManager cm;

    public TomcatContextConfig() {
        this.cm = new ConfigurationManager();
        setDefaultWebXml(Constants.NoDefaultWebXml);
    }

    @Override
    protected synchronized void beforeStart() {
        super.beforeStart();
        String listenerName = cm.getProperty("pax.exam.tomcat.listener", "");
        if (listenerName.equals("weld")) {
            registerBeanManager(context, WELD_MANAGER_FACTORY, WELD_SERVLET_LISTENER);
        }
        else if (listenerName.equals("openwebbeans")) {
            registerBeanManager(context, OWB_MANAGER_FACTORY, OWB_SERVLET_LISTENER);
        }
    };

    private void registerBeanManager(Context appContext, String jndiObjectFactory,
        String servletListener) {
        ContextResource resource = new ContextResource();
        resource.setAuth("Container");
        resource.setName(BEAN_MANAGER_NAME);
        resource.setType(BEAN_MANAGER_TYPE);
        resource.setProperty("factory", jndiObjectFactory);

        appContext.getNamingResources().addResource(resource);

        ContextResourceEnvRef resourceRef = new ContextResourceEnvRef();
        resourceRef.setName(BEAN_MANAGER_NAME);
        resourceRef.setType(BEAN_MANAGER_TYPE);

        appContext.getNamingResources().addResourceEnvRef(resourceRef);

        appContext.addApplicationListener(servletListener);
    }

}
