/*
 * Copyright 2012 Harald Wellmann, 2015 Roland Hauser
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
package org.pax.exam.sisu;

import static com.google.inject.Guice.createInjector;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.UUID;

import org.eclipse.sisu.space.SpaceModule;
import org.eclipse.sisu.space.URLClassSpace;
import org.eclipse.sisu.wire.WireModule;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.options.JarProbeOption;
import org.ops4j.pax.exam.spi.war.JarBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

/**
 * @author Roland Hauser
 * @since 4.7.0
 */
public class SisuTestContainer implements TestContainer {


    private static final Logger LOG = LoggerFactory.getLogger(SisuTestContainer.class);

    private static Injector injector;

    private boolean isValid;

    private ExamSystem system;

    private ClassLoader contextClassLoader;

    private File probeDir;

    public SisuTestContainer(ExamSystem system) {
        this.system = system;
    }

    @Override
    public void call(TestAddress address) {
    }

    @Override
    public long install(String location, InputStream stream) {
        return -1;
    }

    @Override
    public long install(InputStream stream) {
        return -1;
    }

    @Override
    public TestContainer start() {
        validateConfiguration();
        setProbeClassLoader();
        LOG.debug("starting Eclipse Sisu container");
        injector = createInjector(
        	      new WireModule(                    
        	        new SpaceModule(                 
        	          new URLClassSpace( Thread.currentThread().getContextClassLoader() ) 
        	    )));
        isValid = true;
        return this;
    }

    private void setProbeClassLoader() {
        JarProbeOption probeOption = system.getSingleOption(JarProbeOption.class);
        if (probeOption == null) {
            return;
        }

        probeDir = new File(system.getTempFolder(), UUID.randomUUID().toString());
        probeDir.mkdir();
        JarBuilder builder = new JarBuilder(probeDir, probeOption);
        URI jar = builder.buildJar();
        try {
            URLClassLoader classLoader = new URLClassLoader(new URL[] { jar.toURL() });
            contextClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        catch (MalformedURLException exc) {
            throw new TestContainerException(exc);
        }
    }

    private void validateConfiguration() {
        ConfigurationManager cm = new ConfigurationManager();
        String systemType = cm.getProperty(Constants.EXAM_SYSTEM_KEY);
        if (!Constants.EXAM_SYSTEM_CDI.equals(systemType)) {
            String msg = "OpenWebBeansTestContainer requires pax.exam.system = cdi";
            throw new TestContainerException(msg);
        }
    }

    @Override
    public TestContainer stop() {
        if (injector != null && isValid) {
            LOG.debug("stopping Eclipse Sisu container");
            unsetProbeClassLoader();
        }
        return this;
    }

    private void unsetProbeClassLoader() {
        if (contextClassLoader != null) {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public String toString() {
        return "Eclipse Sisu";
    }

    public static Injector getInjector() {
        return injector;
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
