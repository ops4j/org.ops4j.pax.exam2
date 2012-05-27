package org.ops4j.pax.exam.jboss;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.jboss.as.embedded.EmbeddedServerFactory;
import org.jboss.as.embedded.ServerStartException;
import org.jboss.as.embedded.StandaloneServer;
import org.junit.Test;
import org.ops4j.pax.exam.ConfigurationManager;

public class JBossLauncherTest
{

    
    @Test
    public void launchJBoss() throws ServerStartException, IOException, InterruptedException, ExecutionException
    {
        System.setProperty( "java.util.logging.manager", "org.jboss.logmanager.LogManager" );
        System.setProperty( "org.jboss.logging.provider", "slf4j");

        ConfigurationManager cm = new ConfigurationManager();
        String jBossHome = cm.getProperty( "pax.exam.server.home" );
        StandaloneServer server = EmbeddedServerFactory.create( new File( jBossHome ), System.getProperties(),
            System.getenv(),
            "org.jboss.logmanager", "org.jboss.logging", "org.slf4j"
            );
        server.start();
        server.stop();
    }
}
