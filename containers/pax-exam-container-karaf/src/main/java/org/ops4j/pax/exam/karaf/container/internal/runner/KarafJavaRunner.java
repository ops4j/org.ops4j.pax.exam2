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

/**
 * Very simple asynchronous implementation of Java Runner. Exec is being invoked in a fresh Thread.
 */
public class KarafJavaRunner implements Runner {

    private InternalRunner runner;

    public KarafJavaRunner() {
        runner = new InternalRunner();
    }

    @Override
    public synchronized void // CHECKSTYLE:SKIP : more than 10 params
    exec(final String[] environment, final File karafBase, final String javaHome, final String[] javaOpts,
         final String[] javaEndorsedDirs,
         final String[] javaExtDirs, final String karafHome, final String karafData, final String karafEtc, final String karafLog, final String[] karafOpts,
         final String[] opts, final String[] classPath, final String main, final String options, final boolean security) {
        Thread thread = new Thread("KarafJavaRunner") {
                @Override
                public void run() {
                    String cp = buildCmdSeparatedString(classPath);
                    String endDirs = buildCmdSeparatedString(javaEndorsedDirs);
                    String extDirs = buildCmdSeparatedString(javaExtDirs);
                    CommandLineBuilder commandLine = null;
                    if (org.ops4j.pax.exam.karaf.container.internal.KarafTestContainer.isJava9Compatible()) {
                        commandLine = new CommandLineBuilder()
                            .append(getJavaExecutable(javaHome))
                            .append(javaOpts)
                            .append("-Dkaraf.instances=" + karafHome + "/instances")
                            .append("-Dkaraf.home=" + karafHome)
                            .append("-Dkaraf.base=" + karafBase)
                            .append("-Dkaraf.data=" + karafData)
                            .append("-Dkaraf.etc=" + karafEtc)
                            .append("-Dkaraf.log=" + karafLog)
                            .append("-Djava.util.logging.config.file=" + karafEtc + "/java.util.logging.properties");
                        
                    } else {
                        commandLine = new CommandLineBuilder()
                            .append(getJavaExecutable(javaHome))
                            .append(javaOpts)
                            .append("-Djava.endorsed.dirs=" + endDirs)
                            .append("-Djava.ext.dirs=" + extDirs)
                            .append("-Dkaraf.instances=" + karafHome + "/instances")
                            .append("-Dkaraf.home=" + karafHome)
                            .append("-Dkaraf.base=" + karafBase)
                            .append("-Dkaraf.data=" + karafData)
                            .append("-Dkaraf.etc=" + karafEtc)
                            .append("-Dkaraf.log=" + karafLog)
                            .append("-Djava.util.logging.config.file=" + karafEtc + "/java.util.logging.properties");
                    }
                    if (security) {
                        commandLine.append("-Djavax.management.builder.initial=org.apache.karaf.management.boot.KarafMBeanServerBuilder");
                    }
                    boolean isExternalClasspathSet = false;
                    for (String str : javaOpts) {
                        if ("-classpath".equals(str)) {
                            isExternalClasspathSet = true;
                            break;
                        }
                    }
                    if (isExternalClasspathSet) {
                        commandLine
                            .append(karafOpts)
                            .append(opts)
                            .append(main)
                            .append(options);
                    } else {
                        commandLine
                        .append(karafOpts)
                        .append(opts)
                        .append("-cp")
                        .append(cp)
                        .append(main)
                        .append(options);
                    }
                    runner.exec(commandLine, karafBase, environment);
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

                private String getJavaExecutable(final String javaHome) {
                    if (javaHome == null) {
                        throw new IllegalStateException("JAVA_HOME is not set.");
                    }
                    return javaHome + "/bin/java";
                }
            };
        thread.start();
    }

    @Override
    public synchronized void shutdown() {
        runner.shutdown();
    }

}
