/*
 * Copyright 2012 Harald Wellmann
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
package org.ops4j.pax.exam.maven;

import static org.ops4j.pax.exam.maven.Constants.TEST_CONTAINER_RUNNER_KEY;
import static org.ops4j.pax.exam.maven.Constants.TEST_CONTAINER_PORT_KEY;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ops4j.exec.DefaultJavaRunner;
import org.ops4j.pax.exam.spi.PaxExamRuntime;

/**
 * Starts a Pax Exam Container in server mode for the given configuration class. The container
 * is running in a background process which should be terminated by the {@code stop-container}
 * goal.
 * 
 * @goal start-container
 * @phase pre-integration-test
 * @requiresDependencyResolution test
 * @description Starts Pax Exam in server mode
 */
public class StartContainerMojo extends AbstractMojo {

    /**
     * Maven defined system property name.
     */
    private static final String BASEDIR = "basedir";

    /**
     * Mojo execution injected through Maven.
     * 
     * @parameter default-value="${mojoExecution}"
     * @readonly
     */
    private MojoExecution mojoExecution;

    /**
     * The base directory of the project being built. This can be obtained in your
     * {@code @Configuration} method integration test via System.getProperty("basedir").
     * 
     * @parameter default-value="${basedir}"
     */
    private File basedir;

    /**
     * Fully qualified name of a Java class with a {@code @Configuration} method, providing the test
     * container configuration.
     * 
     * @parameter
     * @required
     */
    private String configClass;

    /**
     * Test classpath.
     * 
     * @parameter expression="${project.testClasspathElements}"
     * @required
     */
    private String[] classpathElements;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("classpath for forked process:");
        for (String cp : classpathElements) {
            getLog().debug(cp);
        }
        
        DefaultJavaRunner javaRunner = new DefaultJavaRunner(false);
        String basedirProp = String.format("-D%s=%s", BASEDIR, basedir.getAbsolutePath());
        String[] vmOptions = new String[] { basedirProp };
        String javaHome = System.getProperty("java.home");
        int port = getFreePort();
        String[] args = new String[] { configClass, Integer.toString(port) };

        // inherit working directory from this process
        javaRunner.exec(vmOptions, classpathElements, PaxExamRuntime.class.getName(), args, javaHome, null);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> context = getPluginContext();
        
        context.put(TEST_CONTAINER_RUNNER_KEY + mojoExecution.getExecutionId(), javaRunner);
        context.put(TEST_CONTAINER_PORT_KEY + mojoExecution.getExecutionId(), port);
    }


    private int getFreePort() throws MojoExecutionException {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        }
        catch (IOException exc) {
            throw new MojoExecutionException("", exc);
        }
    }

}
