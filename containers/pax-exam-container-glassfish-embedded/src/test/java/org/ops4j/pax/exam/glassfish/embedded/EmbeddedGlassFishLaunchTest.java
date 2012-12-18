/*
 * Copyright 2012 Harald Wellmann
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
package org.ops4j.pax.exam.glassfish.embedded;

import java.io.File;
import java.net.URI;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedGlassFishLaunchTest {

    private static Logger log = LoggerFactory.getLogger(EmbeddedGlassFishLaunchTest.class);

    @Test
    public void launchGlassFish() throws Exception {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("java.util.logging.config.file",
            "src/test/resources/glassfish-config/logging.properties");

        GlassFishProperties gfProps = new GlassFishProperties();
        File domainXml = new File("src/test/resources/glassfish-config/domain.xml");
        gfProps.setConfigFileURI(domainXml.getAbsoluteFile().toURI().toString());

        GlassFish gf = GlassFishRuntime.bootstrap().newGlassFish(gfProps);
        gf.start();

        Deployer deployer = gf.getDeployer();
        for (String appName : deployer.getDeployedApplications()) {
            log.info("undeploying " + appName);
            deployer.undeploy(appName);
        }

        URI sampleWarUri = new URI("mvn:org.apache.wicket/wicket-examples/1.5.3/war");
        String sampleAppName = "wicket-examples";

        log.info("deploying " + sampleAppName);
        deployer.deploy(sampleWarUri, "--name", sampleAppName, "--contextroot", sampleAppName);

        log.info("undeploying " + sampleAppName);
        deployer.undeploy(sampleAppName);

        log.info("stopping GlassFish");
        gf.stop();
    }
}
