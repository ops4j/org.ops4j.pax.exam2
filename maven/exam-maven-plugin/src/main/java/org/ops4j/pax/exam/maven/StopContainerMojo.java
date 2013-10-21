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

import static org.ops4j.pax.exam.maven.Constants.TEST_CONTAINER_KEY;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ops4j.pax.exam.TestContainer;

/**
 * Stops a Pax Exam Forked Container started by the start-container goal.
 * 
 * @goal stop-container
 * @phase post-integration-test
 * @description Stops a Pax Exam Forked Container started by the start-container goal.
 */
public class StopContainerMojo extends AbstractMojo {

    /**
     * Mojo execution injected through Maven.
     * 
     * @parameter default-value="${mojoExecution}"
     * @readonly
     */
    private MojoExecution mojoExecution;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Object object = getPluginContext().get(TEST_CONTAINER_KEY + mojoExecution.getExecutionId());
        if (object == null) {
            throw new MojoExecutionException(
                "No Pax Exam container found. Did you run the start-container goal?");
        }
        TestContainer testContainer = (TestContainer) object;
        testContainer.stop();
    }
}
