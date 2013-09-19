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
package org.ops4j.pax.exam.openwebbeans;

import java.io.InputStream;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;

import org.apache.webbeans.cditest.CdiTestContainer;
import org.apache.webbeans.cditest.CdiTestContainerLoader;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class OpenWebBeansTestContainer implements TestContainer {

    private static final Logger LOG = LoggerFactory.getLogger(OpenWebBeansTestContainer.class);

    private static CdiTestContainer container;

    private boolean isValid;

    public OpenWebBeansTestContainer(ExamSystem system) {
    }

    public void call(TestAddress address) {
    }

    public long install(String location, InputStream stream) {
        return -1;
    }

    public long install(InputStream stream) {
        return -1;
    }

    public void afterDeployment(@Observes AfterDeploymentValidation event) {
        isValid = true;
    }

    public TestContainer start() {
        validateConfiguration();
        LOG.debug("starting OpenWebBeans container");
        container = CdiTestContainerLoader.getCdiContainer();
        try {
            container.bootContainer();
            container.startContexts();
        }
        // CHECKSTYLE:SKIP : OpenWebBeans API
        catch (Exception exc) {
            throw new TestContainerException(exc);
        }
        return this;
    }
    
    private void validateConfiguration() {
        ConfigurationManager cm = new ConfigurationManager();
        String systemType = cm.getProperty(Constants.EXAM_SYSTEM_KEY);
        if (! Constants.EXAM_SYSTEM_CDI.equals(systemType)) {
            String msg = "OpenWebBeansTestContainer requires pax.exam.system = cdi";
            throw new TestContainerException(msg);
        }
    }

    

    public TestContainer stop() {
        if (container != null && isValid) {
            LOG.debug("stopping OpenWebBeans container");
            try {
                container.stopContexts();
                container.shutdownContainer();
            }
            // CHECKSTYLE:SKIP : OpenWebBeans API
            catch (Exception exc) {
                throw new TestContainerException(exc);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "OpenWebBeans";
    }

    public static CdiTestContainer getCdiContainer() {
        return container;
    }
    

    @Override
    public long installProbe(InputStream stream) {
        return -1;
    }

    @Override
    public void uninstallProbe() {
        // not used
    }    
}
