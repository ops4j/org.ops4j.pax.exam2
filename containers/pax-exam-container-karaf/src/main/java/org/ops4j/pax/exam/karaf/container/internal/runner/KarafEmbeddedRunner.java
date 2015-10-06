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

/**
 * Very simple asynchronous implementation of Java Runner. Exec is being invoked in a fresh Thread.
 */
public class KarafEmbeddedRunner implements Runner {

    private InternalRunner runner;

    public KarafEmbeddedRunner() {
        runner = new InternalRunner();
    }

    @Override
    public synchronized void // CHECKSTYLE:SKIP : more than 10 params
    exec(final String[] environment, final File karafBase, final String javaHome, final String[] javaOpts,
         final String[] javaEndorsedDirs,
         final String[] javaExtDirs, final String karafHome, final String karafData, final String karafEtc, final String[] karafOpts,
         final String[] opts, final String[] classPath, final String main, final String options, final boolean security) {
        Thread thread = new Thread("KarafEmbeddedRunner") {
                @Override
                public void run() {
                    String cp = buildCmdSeparatedString(classPath);
                    String endDirs = buildCmdSeparatedString(javaEndorsedDirs);
                    String extDirs = buildCmdSeparatedString(javaExtDirs);
                    
                    System.setProperty("karaf.instances", karafHome + "/instances");
                    System.setProperty("karaf.home", karafHome);
                    System.setProperty("karaf.base", karafBase.getAbsolutePath());
                    System.setProperty("karaf.data", karafData);
                    System.setProperty("karaf.etc", karafEtc);
                    System.setProperty("java.util.logging.config.file", karafEtc + "/java.util.logging.properties");
                    
                    final CommandLineBuilder commandLine = new CommandLineBuilder();
                    commandLine.append(karafOpts)
                            .append(opts)
                            .append("-cp")
                            .append(cp)
                            .append(options);

                    try {
                    	String[] arguments = commandLine.toArray();
                    	
                    	File mainBundlePath = new File(karafBase, "lib");
                		File mainBundle = searchMainBundle(mainBundlePath.listFiles());
                		
                		if (null != mainBundle) {
                			URLClassLoader urlCl = new URLClassLoader(new URL[] { mainBundle.toURL()},this.getContextClassLoader());
                			Class<?> mainClass = urlCl.loadClass("org.apache.karaf.main.Main");
                			Constructor<?> constructor = mainClass.getConstructor(String[].class);
                			constructor.setAccessible(true);
                			Object karafInstance = constructor.newInstance(new Object[] {arguments});
                			Method method = mainClass.getMethod("launch", null);
                			method.invoke(karafInstance, null);
                		} else {
                			throw new RuntimeException("No Karaf main found");
                		}
						
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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

                private File searchMainBundle(File[] files) {
                	
                	for (File file : files) {
            			if(file.isDirectory()) {
            				File mainBundle = searchMainBundle(file.listFiles());
            				if (null != mainBundle)
            					return mainBundle; 
            			} else {
            				String name = file.getName();
            				if (name.startsWith("org.apache.karaf.main")) {
            					//karaf 4.x
            					return file;
            				} else if (name.equalsIgnoreCase("karaf.jar")) {
            					//karaf version 3.x
            					return file;
            				}
            			}
            		}
                	
                	return null;
                }
                
            };
        thread.start();
    }

    @Override
    public synchronized void shutdown() {
        runner.shutdown();
        
    }

}
