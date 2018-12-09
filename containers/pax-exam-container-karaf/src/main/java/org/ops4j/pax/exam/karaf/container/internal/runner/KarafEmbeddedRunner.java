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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Very simple asynchronous implementation of Java Runner. Exec is being invoked in a fresh Thread.
 */
public class KarafEmbeddedRunner implements Runner {

    private InternalRunner runner;

    public KarafEmbeddedRunner() {
        runner = new InternalRunner();
    }

    @Override
    @SuppressWarnings("deprecation")
    public synchronized void // CHECKSTYLE:SKIP : more than 10 params
        exec(final String[] environment, final File karafBase, final String javaHome,
            final String[] javaOpts, final String[] javaEndorsedDirs, final String[] javaExtDirs,
            final String karafHome, final String karafData, final String karafEtc, final String karafLog,
            final String[] karafOpts, final String[] opts, final String[] classPath,
            final String main, final String options, final boolean security) {
        Thread thread = new Thread("KarafEmbeddedRunner") {

            @Override
            public void run() {
                String cp = buildCmdSeparatedString(classPath);

                System.setProperty("karaf.instances", karafHome + "/instances");
                System.setProperty("karaf.home", karafHome);
                System.setProperty("karaf.base", karafBase.getAbsolutePath());
                System.setProperty("karaf.data", karafData);
                System.setProperty("karaf.etc", karafEtc);
                System.setProperty("karaf.log", karafLog);
                System.setProperty("java.util.logging.config.file",
                    karafEtc + "/java.util.logging.properties");

                final CommandLineBuilder commandLine = new CommandLineBuilder();
                commandLine.append(karafOpts).append(opts).append("-cp").append(cp).append(options);

                try {
                    String[] arguments = commandLine.toArray();

                    File mainBundlePath = new File(karafBase, "lib");
                    List<File> mainBundles = searchMainBundle(mainBundlePath.listFiles());

                    if (null != mainBundles && !mainBundles.isEmpty()) {

                        URL[] bundleUrls = new URL[mainBundles.size()];
                        for (int i = 0; i < mainBundles.size(); i++) {
                            bundleUrls[i] = mainBundles.get(i).toURL();
                        }

                        URLClassLoader urlCl = new URLClassLoader(bundleUrls,
                            this.getContextClassLoader());
                        Class<?> mainClass = urlCl.loadClass(main);
                        Constructor<?> constructor = mainClass.getConstructor(String[].class);
                        constructor.setAccessible(true);
                        Object karafInstance = constructor.newInstance(new Object[] { arguments });
                        Method method = mainClass.getMethod("launch", (Class<?>[]) null);
                        method.invoke(karafInstance, (Object[]) null);
                    }
                    else {
                        throw new RuntimeException("No Karaf main found");
                    }

                }
                catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                    | SecurityException | MalformedURLException e) {
                    throw new RuntimeException(e);
                }

            }

            private String buildCmdSeparatedString(final String[] splitted) {
                final StringBuilder together = new StringBuilder();
                for (String path : splitted) {
                    if (together.length() != 0) {
                        together.append(File.pathSeparator);
                    }
                    together.append(path);
                }
                return together.toString();
            }

            private List<File> searchMainBundle(File[] files) {

                List<File> mainBundles = new ArrayList<File>();

                for (File file : files) {
                    if (file.isDirectory() && file.getName().contains("boot")) {
                        // Karaf4 style
                        mainBundles.addAll(searchMainBundle(file.listFiles()));
                    }
                    else {
                        if (file.getPath().contains(File.separator + "boot")) {
                            // karaf 4.x
                            mainBundles.add(file);
                        }
                        else if (file.getName().startsWith("karaf")
                            && file.getName().endsWith(".jar")) {
                            // karaf version 3.x
                            mainBundles.add(file);
                        }
                    }
                }
                return mainBundles;
            }

        };
        thread.start();
    }

    @Override
    public synchronized void shutdown() {
        runner.shutdown();

    }

}
