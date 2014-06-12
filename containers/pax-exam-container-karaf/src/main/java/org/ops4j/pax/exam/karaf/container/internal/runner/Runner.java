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
 * Abstracts the runner to be able to add different runners easier.
 */
public interface Runner {

    /**
     * Starts the environment in the specific environment.
     * 
     * @param environment
     *            environment variables
     * @param karafBase
     *            Karaf base directory
     * @param javaHome
     *            Java home directory
     * @param javaOpts
     *            Java VM options
     * @param javaEndorsedDirs
     *            Java endorsed directories
     * @param javaExtDirs
     *            Java extension directories
     * @param karafHome
     *            Karaf home directory
     * @param karafData
     *            Karaf data directory
     * @param karafEtc
     *            Karaf etc directory
     * @param karafOpts
     *            Karaf options
     * @param opts
     *            options
     * @param classPath
     *            Java class path
     * @param main
     *            main class
     * @param options
     *            program arguments
     * @param security
     *            security flag
     */
    // CHECKSTYLE:SKIP - more than 10 params
    void exec(final String[] environment, final File karafBase, final String javaHome,
        final String[] javaOpts, final String[] javaEndorsedDirs, final String[] javaExtDirs,
        final String karafHome, final String karafData, final String karafEtc,
        final String[] karafOpts, final String[] opts, final String[] classPath, final String main,
        final String options, final boolean security);

    /**
     * Shutdown the runner again.
     */
    void shutdown();

}
