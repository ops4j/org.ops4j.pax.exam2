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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.ConfigurationManager;
import org.wildfly.core.embedded.EmbeddedServerFactory;
import org.wildfly.core.embedded.ServerStartException;
import org.wildfly.core.embedded.StandaloneServer;

public class WildFly90LauncherTest {

    @Before
    public void setUp() throws IOException {
        WildFly90TestContainer tc = new WildFly90TestContainer(null);
        tc.installContainer();
    }

    @Test
    public void launchWildFly() throws ServerStartException, IOException, InterruptedException,
        ExecutionException {
        System.setProperty("logging.configuration", "file:src/test/resources/logging.properties");
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        System.setProperty("org.jboss.logging.provider", "slf4j");
        System.setProperty("jboss.server.config.dir", "target/test-classes/wildfly90-config");

        ConfigurationManager cm = new ConfigurationManager();
        String jBossHome = cm.getProperty("pax.exam.wildfly90.home");
        StandaloneServer server = EmbeddedServerFactory.create(jBossHome, null, null,
            "org.jboss.logmanager", "org.jboss.logging", "org.slf4j");
        server.start();
        server.stop();
    }
}
