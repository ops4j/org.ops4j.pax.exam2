/*
 * Copyright 2013 Harald Wellmann
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
package org.ops4j.pax.exam.options;

import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;

/**
 * This option can be used to define the contents of a JAR probe for CDI test containers. When a JAR
 * probe option is present, the test container builds a JAR on the fly, creates a classloader for
 * this JAR and uses this as thread context classloader while the container is running.
 * <p>
 * Otherwise, when no JAR probe option is present, the CDI test container simply uses the system
 * classloader.
 * <p>
 * Users should create instances of this class using {@link CoreOptions#jarProbe()} and then invoke
 * methods of this class in fluent syntax to configure the JAR probe.
 * 
 * @author Harald Wellmann
 * 
 */
public class JarProbeOption implements Option {

    private List<String> resources;
    private List<Class<?>> classes;
    private List<String> metaInfResources;
    private String name;

    /**
     * Creates an empty JAR probe option. Application code should not invoke this constructor. Use
     * {@link CoreOptions#warProbe()} instead.
     */
    public JarProbeOption() {
        classes = new ArrayList<Class<?>>();
        resources = new ArrayList<String>();
        metaInfResources = new ArrayList<String>();
    }

    /**
     * Sets the base name of this JAR.
     * 
     * @param _name
     *            base name
     * @return {@code this} for fluent syntax
     */
    public JarProbeOption name(String _name) {
        this.name = _name;
        return this;
    }

    /**
     * Adds the given classes to the JAR.
     * 
     * @param klass
     *            list of classes
     * @return {@code this} for fluent syntax
     */
    public JarProbeOption classes(Class<?>... klass) {
        for (Class<?> c : klass) {
            String resource = c.getName().replaceAll("\\.", "/") + ".class";
            resources.add(resource);
        }
        return this;
    }

    /**
     * Adds the given resources from the current class path to the JAR.
     * 
     * @param resourcePaths
     *            list of resource paths, relative to the class path root
     * @return {@code this} for fluent syntax
     */
    public JarProbeOption resources(String... resourcePaths) {
        for (String resource : resourcePaths) {
            resources.add(resource);
        }
        return this;
    }

    /**
     * Adds the given resources from the current class path to the WAR in {@code META-INF/}.
     * 
     * @param resourcePath
     *            resource path, relative to the class path root
     * @return {@code this} for fluent syntax
     */
    public JarProbeOption metaInfResource(String resourcePath) {
        metaInfResources.add(resourcePath);
        return this;
    }

    /**
     * Returns the base name of this JAR.
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return application name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the META-INF resources.
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return resources (possibly empty but not null)
     */
    public List<String> getMetaInfResources() {
        return metaInfResources;
    }

    /**
     * Returns the classpath resources.
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return resources (possibly empty but not null)
     */
    public List<String> getResources() {
        return resources;
    }

    /**
     * Returns the classes of this JAR.
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return classes (possibly empty but not null)
     */
    public List<Class<?>> getClasses() {
        return classes;
    }
}
