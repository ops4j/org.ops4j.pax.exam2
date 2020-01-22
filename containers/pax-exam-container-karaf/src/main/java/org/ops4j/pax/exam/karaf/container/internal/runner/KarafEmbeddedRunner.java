/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.karaf.container.internal.runner;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Very simple asynchronous implementation of Java Runner.
 * Exec is being invoked in a fresh Thread.
 */
public class KarafEmbeddedRunner implements Runner {

    private InternalRunner runner;
    private List<Runnable> resetTasks;

    public KarafEmbeddedRunner() {
        runner = new InternalRunner();
    }

    @Override
    @SuppressWarnings("deprecation")
    public synchronized void // CHECKSTYLE:SKIP : more than 10 params
    exec(final String[] environment, final File base, final String javaHome,
         final String[] javaOpts, final String[] javaEndorsedDirs, final String[] javaExtDirs,
         final String home, final String data, final String etc, final String log,
         final String[] karafOpts, final String[] opts, final String[] classpath,
         final String main, final String options, final boolean security) {
        new Thread(
            () -> doMain(base, home, data, etc, log, karafOpts, opts, classpath, main, options),
            "KarafEmbeddedRunner")
        .start();
    }

    private void  // CHECKSTYLE:SKIP : more than 10 params
    doMain(final File karafBase,
           final String karafHome, final String karafData, final String karafEtc, final String karafLog,
           final String[] karafOpts, final String[] opts, final String[] classPath,
           final String main, final String options) {
        final String cp = String.join(File.pathSeparator, classPath);

        resetTasks = new ArrayList<>();
        resetTasks.add(setSystemProperty("karaf.instances", karafHome + "/instances"));
        resetTasks.add(setSystemProperty("karaf.home", karafHome));
        resetTasks.add(setSystemProperty("karaf.base", karafBase.getAbsolutePath()));
        resetTasks.add(setSystemProperty("karaf.data", karafData));
        resetTasks.add(setSystemProperty("karaf.etc", karafEtc));
        resetTasks.add(setSystemProperty("karaf.log", karafLog));
        resetTasks.add(setSystemProperty("java.util.logging.config.file", karafEtc + "/java.util.logging.properties"));

        final CommandLineBuilder commandLine = new CommandLineBuilder();
        commandLine.append(karafOpts).append(opts).append("-cp").append(cp).append(options);
        try {
            final String[] arguments = commandLine.toArray();

            final File mainBundlePath = new File(karafBase, "lib");
            final List<File> mainBundles = searchMainBundle(mainBundlePath.listFiles());
            final Thread thread = Thread.currentThread();
            try {
                // if endorsed is not set, add it to the classpath,
                // will work in degraded mode most of the time
                thread.getContextClassLoader()
                        .loadClass("org.apache.karaf.specs.locator.OsgiLocator");
            } catch (final ClassNotFoundException cnfe) {
                final File endorsed = new File(mainBundlePath, "endorsed");
                if (endorsed.exists()) {
                    mainBundles.addAll(ofNullable(endorsed.listFiles(
                            (dir, name) -> name.endsWith(".jar")))
                            .map(Stream::of).map(s -> s.collect(toList()))
                            .orElseGet(Collections::emptyList));
                }
            }
            if (!mainBundles.isEmpty()) {
                final URL[] bundleUrls = mainBundles.stream().map(it -> {
                    try {
                        return it.toURI().toURL();
                    } catch (final MalformedURLException e) {
                        throw new IllegalArgumentException(e);
                    }
                }).toArray(URL[]::new);

                final File jrePropsFile = new File(karafBase, "etc/jre.properties");
                final Properties jreProps = loadProps(jrePropsFile);
                final File configPropsFile = new File(karafBase, "etc/config.properties");
                final Properties configProps = loadProps(configPropsFile);
                if (!configProps.isEmpty()) {
                    final String property = configProps.getProperty("org.osgi.framework.bootdelegation");
                    if (property != null && !property.contains("com.intellij.rt.")) {
                        try (final OutputStream outputStream = new FileOutputStream(configPropsFile)) {
                            configProps.put("org.osgi.framework.bootdelegation", property + ",com.intellij.rt.*");
                            configProps.store(outputStream, "adding intellij in delegation");
                        } catch (final IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                }

                final URLClassLoader urlCl = new MainClassLoader(bundleUrls, new FilteringClassLoader(
                        thread.getContextClassLoader(),
                        findJre(jreProps, configProps)));
                resetTasks.add(() -> {
                    try {
                        urlCl.close();
                    } catch (final IOException e) {
                        // no-op: not important
                    }
                });
                thread.setContextClassLoader(urlCl); // no need to reset, this thread is single use
                final Class<?> mainClass = urlCl.loadClass(main);
                final Constructor<?> constructor = mainClass.getConstructor(String[].class);
                constructor.setAccessible(true);
                final Object karafInstance = constructor.newInstance(new Object[]{arguments});
                final Method method = mainClass.getMethod("launch", (Class<?>[]) null);
                method.invoke(karafInstance, (Object[]) null);
            } else {
                throw new RuntimeException("No Karaf main found");
            }

        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private Runnable setSystemProperty(final String key, final String value) {
        final String old = System.getProperty(key);
        System.setProperty(key, value);
        return () -> {
            if (old == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        };
    }

    private Properties loadProps(final File file) {
        final Properties props = new Properties();
        if (file.exists()) {
            try (final InputStream stream = new FileInputStream(file)) {
                props.load(stream);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return props;
    }

    private List<File> searchMainBundle(final File[] files) {
        final List<File> mainBundles = new ArrayList<File>();
        if (files == null) {
            return mainBundles;
        }
        for (final File file : files) {
            if (file.isDirectory() && isBoot(file)) {
                // Karaf4 style
                mainBundles.addAll(searchMainBundle(file.listFiles()));
            } else {
                if (isBoot(file.getParentFile())) {
                    if (file.getName().endsWith(".jar")) {
                        // karaf 4.x
                        mainBundles.add(file);
                    } // else ignore (README for ex.)
                } else if (file.getName().startsWith("karaf") && file.getName().endsWith(".jar")) {
                    // karaf version 3.x
                    mainBundles.add(file);
                }
            }
        }
        return mainBundles;
    }

    private boolean isBoot(final File file) {
        return file != null && file.getName().equals("boot");
    }

    private Collection<String> findJre(final Properties jreProps, final Properties configProps) {
        final List<String> exclusions = getForcedOsgiPackages().collect(toList());
        return Stream.concat(
                findJreProp(jreProps).stream(),
                toVersions(resolveProperty(configProps, "org.osgi.framework.bootdelegation", "")).stream()
                        .filter(it -> !it.startsWith("org.apache.karaf.")))
                .filter(it -> exclusions.stream().noneMatch(it::startsWith))
                .collect(toList());
    }

    private Collection<String> findJreProp(final Properties jreProps) {
        if (jreProps.isEmpty()) {
            return getEnforcedParentPackages().collect(toList());
        }
        final String version = System.getProperty("java.version", "1.8");

        String key;
        if (version.startsWith("1.8")) { // 7, 6 etc... are not supported so we only need to take care of it
            key = "jre-1.8";
        } else {
            final int sep = version.indexOf(".");
            try {
                key = "jre-" + Integer.parseInt(version.substring(0, sep));
            } catch (final NumberFormatException nfe) {
                key = "jre-1.8"; // fallback, should be most of the time ok
            }
        }
        return Stream.concat(
                getEnforcedParentPackages(),
                toVersions(resolveProperty(jreProps, key, null)).stream())
                .collect(toList());
    }

    private String resolveProperty(final Properties configProps, final String key, final String defaultValue) {
        final String value = configProps.getProperty(key, defaultValue);
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            return resolveProperty(configProps, value.substring("${".length(), value.length() - "}".length()), defaultValue);
        }
        return ofNullable(value).orElse(defaultValue);
    }

    private Stream<String> getEnforcedParentPackages() {
        return Stream.concat(Stream.of("java."), getCustomJvmPackages());
    }

    // let user customize it otherwise it is hard to be right for all stacks
    private Stream<String> getCustomJvmPackages() {
        final Collection<String> config = toVersions(System.getProperty(getClass().getName() + ".customJvmPackages"));
        if (config.isEmpty()) { // default is IDE friendly
            return Stream.of("com.intellij.rt.");
        }
        return config.stream();
    }

    // let user customize it otherwise it is hard to be right for all stacks
    private Stream<String> getForcedOsgiPackages() {
        final Collection<String> config = toVersions(System.getProperty(getClass().getName() + ".forcedOSGiPackages"));
        if (config.isEmpty()) { // default is IDE friendly
            return Stream.empty();
        }
        return config.stream();
    }

    private Collection<String> toVersions(final String property) {
        return property == null ?
                emptyList() :
                Stream.of(property.split(","))
                        .map(String::trim)
                        .map(it -> {
                            final int sep = it.indexOf(";");
                            return sep > 0 ? it.substring(0, sep) : it;
                        })
                        .map(it -> it.endsWith("*") ? it.substring(0, it.length() - 1) : it)
                        .collect(toList());
    }

    @Override
    public synchronized void shutdown() {
        runner.shutdown();
        if (resetTasks != null) {
            resetTasks.forEach(Runnable::run);
            resetTasks.clear();
        }
    }

    private static class MainClassLoader extends URLClassLoader {
        static {
            registerAsParallelCapable();
        }

        public MainClassLoader(final URL[] urls, final ClassLoader parent) {
            super(urls, parent);
        }
    }

    private static class FilteringClassLoader extends ClassLoader {
        static {
            registerAsParallelCapable();
        }

        private final Collection<String> packages;

        public FilteringClassLoader(final ClassLoader contextClassLoader,
                                    final Collection<String> packages) {
            super(contextClassLoader);
            this.packages = packages;
        }

        @Override // let only the JVM filter from this parent classloader to isolate the container from the tests
        protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            if (name != null && packages.stream().noneMatch(name::startsWith)) {
                throw new ClassNotFoundException(name);
            }
            return super.loadClass(name, resolve);
        }
    }
}
