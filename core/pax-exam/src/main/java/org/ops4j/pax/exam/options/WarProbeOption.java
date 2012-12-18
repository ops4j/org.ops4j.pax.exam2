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
package org.ops4j.pax.exam.options;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;

/**
 * This option can be used to explicitly define the contents of a WAR probe, overriding the default
 * of the probe being built automatically from the classpath contents, excluding all libraries
 * matching some predefined patterns.
 * <p>
 * Users should create instances of this class using {@link CoreOptions#warProbe()} and then invoke
 * methods of this class in fluent syntax to configure the WAR probe.
 * 
 * @author hwellmann
 * 
 */
public class WarProbeOption implements Option {

    public static final String[] DEFAULT_CLASS_PATH_EXCLUDES = { ".cp", "bndlib",
        "geronimo-atinject_", "geronimo-ejb_", "geronimo-jcdi_", "geronimo-jpa_",
        "glassfish-embedded", "javaee-api", "jboss-log", "jersey-client", "jstl", "myfaces",
        "openejb", "openwebbeans", "org.eclipse.osgi", "pax-exam-container", "pax-url-aether",
        "scattered-archive-api", "servlet-api", "simple-glassfish-api", "sisu", "tinybundles",
        "tomcat", };

    private List<String> overlays;
    private List<String> libraries;
    private List<String> resources;
    private List<Class<?>> classes;
    private List<String> metaInfResources;
    private List<String> webInfResources;
    private List<String> classpathFilters;
    private boolean useClasspath;
    private String name;

    /**
     * Creates an empty WAR probe option. Application code should not invoke this constructor. Use
     * {@link CoreOptions#warProbe()} instead.
     */
    public WarProbeOption() {
        overlays = new ArrayList<String>();
        libraries = new ArrayList<String>();
        classes = new ArrayList<Class<?>>();
        resources = new ArrayList<String>();
        metaInfResources = new ArrayList<String>();
        webInfResources = new ArrayList<String>();
        classpathFilters = new ArrayList<String>();
    }

    /**
     * Sets the application name of this WAR.
     * 
     * @param name
     *            application name
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Adds the library from the given path to the WAR. If the path is a directory, it is assumed to
     * be a class folder, and its contents are copied recursively to {@code WEB-INF/classes}.
     * Otherwise, the path is assumed to be a JAR, and its contents are copied to
     * {@code WEB-INF/lib}.
     * 
     * @param libraryPath
     *            path to library
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption library(String libraryPath) {
        libraries.add(new File(libraryPath).toURI().toString());
        return this;
    }

    /**
     * Adds the library from the URL to the WAR. The URL is assumed to reference a JAR. The JAR will
     * be downloaded if required and its contents will be copied to {@code WEB-INF/lib}.
     * 
     * @param libraryURL
     *            URL referencing a library JAR
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption library(UrlReference libraryUrl) {
        libraries.add(libraryUrl.getURL());
        return this;
    }

    /**
     * Adds an overlay from the given path to the WAR. This is similar to the overlay concept of the
     * Maven WAR Plugin. If the overlay path is a directory, its contents are copied recursively to
     * the root of the WAR. If the overlay path is an archive, its exploded contents are copied to
     * the root of the WAR. All overlays are copied in the given order. All overlay are copied
     * before any libraries, classes or resources.
     * 
     * @param overlayPath
     *            path to overlay
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption overlay(String overlayPath) {
        overlays.add(new File(overlayPath).toURI().toString());
        return this;
    }

    /**
     * Adds an overlay from the given URL to the WAR. This is similar to the overlay concept of the
     * Maven WAR Plugin. The URL is assumed to reference an archive. The archive is downloaded if
     * required, and then its exploded contents are copied to the root of the WAR. All overlays are
     * copied in the given order. All overlay are copied before any libraries, classes or resources.
     * 
     * @param overlayPath
     *            path to overlay
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption overlay(UrlReference overlayUrl) {
        overlays.add(overlayUrl.getURL());
        return this;
    }

    /**
     * Adds the given classes to the WAR in {@code WEB-INF/classes}.
     * 
     * @param klass
     *            list of classes
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption classes(Class<?>... klass) {
        for (Class<?> c : klass) {
            String resource = c.getName().replaceAll("\\.", "/") + ".class";
            resources.add(resource);
        }
        return this;
    }

    /**
     * Adds the given resources from the current class path to the WAR in {@code WEB-INF/classes}.
     * 
     * @param klass
     *            list of resource paths, relative to the class path root
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption resources(String... resourcePath) {
        for (String resource : resourcePath) {
            resources.add(resource);
        }
        return this;
    }

    /**
     * Adds the given resourcs from the current class path to the WAR in {@code META-INF/}.
     * 
     * @param klass
     *            list of resource paths, relative to the class path root
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption metaInfResource(String resourcePath) {
        metaInfResources.add(resourcePath);
        return this;
    }

    /**
     * Adds the given resourcs from the current class path to the WAR in {@code WEB-INF/}.
     * 
     * @param klass
     *            list of resource paths, relative to the class path root
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption webInfResource(String resourcePath) {
        webInfResources.add(resourcePath);
        return this;
    }

    /**
     * Automatically add libraries and class folders from the current classpath.
     * 
     * @param includeDefaultFilters
     *            should the default classpath excludes be applied?
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption autoClasspath(boolean includeDefaultFilters) {
        useClasspath = true;
        if (includeDefaultFilters) {
            for (String filter : DEFAULT_CLASS_PATH_EXCLUDES) {
                classpathFilters.add(filter);
            }
        }
        return this;
    }

    /**
     * Automatically add libraries and class folders from the current classpath, applying the
     * default classpath excludes.
     * 
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption classPathDefaultExcludes() {
        useClasspath = true;
        return autoClasspath(true);
    }

    /**
     * This option implies {@code autoClasspath(false)} and adds the given regular expressions to
     * the classpath filters.
     * 
     * @param excludeRegExp
     *            list of regular expressions. Classpath libraries or folders matching any of these
     *            will be excluded
     * @return {@code this} for fluent syntax
     */
    public WarProbeOption exclude(String... excludeRegExp) {
        useClasspath = true;
        for (String exclude : excludeRegExp) {
            classpathFilters.add(exclude);
        }
        return this;
    }

    /**
     * Returns the application name of this WAR.
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return application name
     */
    public String getName() {
        return name;
    }

    /**
     * Shall libraries and class folders be added automatically from the current classpath?
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return
     */
    public boolean isClassPathEnabled() {
        return useClasspath;
    }

    /**
     * Returns the WEB-INF libraries.
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return libraries (possibly empty but not null)
     */
    public List<String> getLibraries() {
        return libraries;
    }

    /**
     * Returns the overlays.
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return overlays (possibly empty but not null)
     */
    public List<String> getOverlays() {
        return overlays;
    }

    /**
     * Returns the classpath filters.
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return filters (possibly empty but not null)
     */
    public List<String> getClassPathFilters() {
        return classpathFilters;
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
     * Returns the WEB-INF resources.
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return resources (possibly empty but not null)
     */
    public List<String> getWebInfResources() {
        return webInfResources;
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
     * Returns the classes to be added to WEB-INF/classes.
     * <p>
     * Internal API, do no use in application code.
     * 
     * @return classes (possibly empty but not null)
     */
    public List<Class<?>> getClasses() {
        return classes;
    }
}
