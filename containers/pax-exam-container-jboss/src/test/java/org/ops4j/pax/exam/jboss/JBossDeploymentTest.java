package org.ops4j.pax.exam.jboss;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
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
import org.jboss.as.embedded.EmbeddedServerFactory;
import org.jboss.as.embedded.ServerStartException;
import org.jboss.as.embedded.StandaloneServer;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.pax.exam.ConfigurationManager;

public class JBossDeploymentTest
{
    @Test
    public void deployWar() throws ServerStartException, IOException, InterruptedException, ExecutionException
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        System.setProperty( "java.util.logging.manager", "org.jboss.logmanager.LogManager" );
        System.setProperty( "org.jboss.logging.provider", "slf4j");
        System.setProperty( "jboss.server.config.dir", "src/test/resources/jboss-config" );

        ConfigurationManager cm = new ConfigurationManager();
        String jBossHome = cm.getProperty( "pax.exam.jboss.home" );
        StandaloneServer server = EmbeddedServerFactory.create( new File( jBossHome ), System.getProperties(),
            System.getenv(),
            "org.jboss.logmanager", "org.jboss.logging", "org.slf4j", "org.jboss.threads", "ch.qos.cal10n"
            );
        server.start();
        
        ServerDeploymentManager deploymentManager = ServerDeploymentManager.Factory.create( InetAddress.getByName("localhost"), 9999  );
        InitialDeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
        String applName = "wicket-examples1";
        URL applUrl = new URL("mvn:org.apache.wicket/wicket-examples/1.5.3/war");
        DeploymentPlan plan = builder.add(applName, applUrl).andDeploy().build();
        ServerDeploymentPlanResult result = deploymentManager.execute( plan ).get();
        UUID actionId = plan.getDeploymentActions().get( 0 ).getId();
        ServerDeploymentActionResult actionResult = result.getDeploymentActionResult( actionId );
        assertThat( actionResult.getResult(), is( Result.EXECUTED));
        server.stop();
    }

    @Test
    @Ignore
    public void deployWarIntoRunningServer() throws ServerStartException, IOException, InterruptedException, ExecutionException
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        System.setProperty( "java.util.logging.manager", "org.jboss.logmanager.LogManager" );
        System.setProperty( "org.jboss.logging.provider", "slf4j");
        
        final ModelControllerClient client = ModelControllerClient.Factory.create("localhost", 9999);
        ServerDeploymentManager deploymentManager = ServerDeploymentManager.Factory.create( client  );
        InitialDeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
        String applName = "wicket-examples.war";
        URL applUrl = new URL("mvn:org.apache.wicket/wicket-examples/1.5.3/war");
        DeploymentPlan plan = builder.add(applName, applUrl).andDeploy().build();
        ServerDeploymentPlanResult result = deploymentManager.execute( plan ).get();
        UUID actionId = plan.getDeploymentActions().get( 0 ).getId();
        ServerDeploymentActionResult actionResult = result.getDeploymentActionResult( actionId );
        assertThat( actionResult.getResult(), is( Result.EXECUTED));
        
        
        plan = deploymentManager.newDeploymentPlan().undeploy( applName ).andRemoveUndeployed().build();
        deploymentManager.execute( plan ).get();
        actionResult = result.getDeploymentActionResult( actionId );
        assertThat( actionResult.getResult(), is( Result.EXECUTED));
        
    }
}
