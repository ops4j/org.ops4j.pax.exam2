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
package org.ops4j.pax.exam.spi.container;

public class ContainerConstants {

    public static final String BEAN_MANAGER_TYPE = "javax.enterprise.inject.spi.BeanManager";
    public static final String BEAN_MANAGER_NAME = "BeanManager";
    
    public static final String WELD_MANAGER_FACTORY = "org.jboss.weld.resources.ManagerObjectFactory";
    public static final String WELD_SERVLET_LISTENER = "org.jboss.weld.environment.servlet.Listener";
    
    public static final String OWB_MANAGER_FACTORY = "org.apache.webbeans.container.ManagerObjectFactory";
    public static final String OWB_SERVLET_LISTENER = "org.apache.webbeans.servlet.WebBeansConfigurationListener";
    
    public static final String CDI_SERVLET_CLASS = "org.ops4j.pax.exam.servlet.TestRunnerServlet";
    public static final String EXAM_APPLICATION_NAME = "Pax-Exam-Probe";
    public static final String EXAM_CONTEXT_ROOT = "/" + EXAM_APPLICATION_NAME;
    public static final String TESTRUNNER_NAME = "testrunner";
    public static final String TESTRUNNER_URL = "/" + TESTRUNNER_NAME;
    public static final String HTTP_PORT_DEFAULT = "8080";
}
