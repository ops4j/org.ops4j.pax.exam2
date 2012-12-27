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
package org.ops4j.pax.exam.candi;

import java.io.InputStream;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;

import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.resin.BeanContainerRequest;
import com.caucho.resin.ResinBeanContainer;

/**
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class CandiTestContainer implements TestContainer {

    private static final Logger LOG = LoggerFactory.getLogger(CandiTestContainer.class);

    private static ResinBeanContainer container;

    private boolean isValid;

    private BeanContainerRequest request;

    public CandiTestContainer(ExamSystem system) {
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
        LOG.debug("starting CanDI container");
        container = new ResinBeanContainer();
        container.start();
        request = container.beginRequest();
        return this;
    }

    public TestContainer stop() {
        if (container != null && isValid) {
            LOG.debug("stopping CanDI container");
            container.completeRequest(request);
            container.close();
        }
        return this;
    }

    @Override
    public String toString() {
        return "Candi";
    }

    public static ResinBeanContainer getCdiContainer() {
        return container;
    }
}
