/*
 * Copyright 2015 Harald Wellmann
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
package org.ops4j.pax.exam.wildfly90;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlan;
import org.jboss.as.controller.client.helpers.standalone.InitialDeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentActionResult;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.as.controller.client.helpers.standalone.ServerUpdateActionResult.Result;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.pax.exam.ConfigurationManager;
import org.wildfly.core.embedded.EmbeddedServerFactory;
import org.wildfly.core.embedded.ServerStartException;
import org.wildfly.core.embedded.StandaloneServer;

public class WildFly90DeploymentTest {

    @Before
    public void setUp() throws IOException {
        WildFly90TestContainer tc = new WildFly90TestContainer(null, null);
        tc.installContainer();
    }

    @Test
    public void deployWar() throws ServerStartException, IOException, InterruptedException,
        ExecutionException {
        deployWarWithPortOffset(null);
    }
    
    @Test
    @Ignore
    public void deployWarWithPortOffset() throws ServerStartException, IOException, InterruptedException,
        ExecutionException {
        deployWarWithPortOffset(10000);
    }
    
    private void deployWarWithPortOffset(Integer offset) throws ServerStartException, IOException, InterruptedException,
        ExecutionException {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        System.setProperty("org.jboss.logging.provider", "slf4j");
        System.setProperty("jboss.server.config.dir", "target/test-classes/wildfly90-config");
        if (offset != null) {
            System.setProperty("jboss.socket.binding.port-offset", Integer.toString(offset));
        }

        ConfigurationManager cm = new ConfigurationManager();
        String jBossHome = cm.getProperty("pax.exam.wildfly90.home");
        StandaloneServer server = EmbeddedServerFactory.create(jBossHome,
            null, null, "org.jboss.logging");
        server.start();

        int port = 9990;
        if (offset != null) {
            port += offset;
        }
        final ModelControllerClient client = ModelControllerClient.Factory
            .create("localhost", port);
        ServerDeploymentManager deploymentManager = ServerDeploymentManager.Factory.create(client);
        InitialDeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
        String applName = "wicket-examples1";
        URL applUrl = new URL("mvn:org.ops4j.pax.web.samples/pax-web-sample-war/5.0.0.M1/war");
        DeploymentPlan plan = builder.add(applName, applUrl).andDeploy().build();
        ServerDeploymentPlanResult result = deploymentManager.execute(plan).get();
        UUID actionId = plan.getDeploymentActions().get(0).getId();
        ServerDeploymentActionResult actionResult = result.getDeploymentActionResult(actionId);
        assertThat(actionResult.getResult(), is(Result.EXECUTED));
        
        builder = deploymentManager.newDeploymentPlan();
        plan = builder.undeploy(applName).andRemoveUndeployed().build();
        result = deploymentManager.execute(plan).get();
        actionId = plan.getDeploymentActions().get(0).getId();
        actionResult = result.getDeploymentActionResult(actionId);
        assertThat(actionResult.getResult(), is(Result.EXECUTED));
        server.stop();
    }

    @Test
    @Ignore
    public void deployWarIntoRunningServer() throws ServerStartException, IOException,
        InterruptedException, ExecutionException {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        System.setProperty("org.jboss.logging.provider", "slf4j");
        System.setProperty("jboss.server.config.dir", "target/test-classes/wildfly90-config");

        final ModelControllerClient client = ModelControllerClient.Factory
            .create("localhost", 19990);
        ServerDeploymentManager deploymentManager = ServerDeploymentManager.Factory.create(client);
        InitialDeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
        String applName = "wicket-examples.war";
        URL applUrl = new URL("mvn:org.ops4j.pax.web.samples/pax-web-sample-war/5.0.0.M1/war");
        DeploymentPlan plan = builder.add(applName, applUrl).andDeploy().build();
        ServerDeploymentPlanResult result = deploymentManager.execute(plan).get();
        UUID actionId = plan.getDeploymentActions().get(0).getId();
        ServerDeploymentActionResult actionResult = result.getDeploymentActionResult(actionId);
        assertThat(actionResult.getResult(), is(Result.EXECUTED));

        plan = deploymentManager.newDeploymentPlan().undeploy(applName).andRemoveUndeployed()
            .build();
        deploymentManager.execute(plan).get();
        actionResult = result.getDeploymentActionResult(actionId);
        assertThat(actionResult.getResult(), is(Result.EXECUTED));
    }
}
