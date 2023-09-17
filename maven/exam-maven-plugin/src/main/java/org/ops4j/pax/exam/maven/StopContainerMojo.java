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

import static org.ops4j.pax.exam.maven.Constants.TEST_CONTAINER_PORT_KEY;
import static org.ops4j.pax.exam.maven.Constants.TEST_CONTAINER_RUNNER_KEY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.ops4j.pax.exam.ExamJavaRunner;

/**
 * Stops a Pax Exam Container started by the start-container goal.
 */
@Mojo(name = "stop-container", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class StopContainerMojo extends AbstractMojo {

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    private MojoExecution mojoExecution;

    /**
     * If true, skip execution.
     */
    @Parameter
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            return;
        }
        
        Object object = getPluginContext().get(TEST_CONTAINER_RUNNER_KEY + mojoExecution.getExecutionId());
        if (object == null) {
            throw new MojoExecutionException(
                "No Pax Exam container found. Did you run the start-container goal?");
        }
        ExamJavaRunner javaRunner = (ExamJavaRunner) object;
        
        object = getPluginContext().get(TEST_CONTAINER_PORT_KEY + mojoExecution.getExecutionId());
        if (object == null) {
            throw new MojoExecutionException(
                "No Pax Exam container port found. Did you run the start-container goal?");
        }
        Integer port = (Integer) object;
        try {
            getLog().debug("stopping test container");
            Socket socket = new Socket((String) null, port);
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
            PrintWriter pw = new PrintWriter(writer, true);
            InputStreamReader isr = new InputStreamReader(socket.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(isr);

            pw.println("stop");
            getLog().debug("quitting test container");
            reader.readLine();
            pw.println("quit");
            reader.close();
            pw.close();
            socket.close();            
        }
        catch (IOException exc) {
            getLog().info("exception communicating with background process, terminating process");
            getLog().info(exc);
        }
        
        javaRunner.shutdown();
    }
}
