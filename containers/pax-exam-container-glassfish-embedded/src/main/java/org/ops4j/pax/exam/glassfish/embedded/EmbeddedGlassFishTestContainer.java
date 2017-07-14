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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.ProbeInvokerFactory;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestDirectory;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.options.UrlDeploymentOption;
import org.ops4j.pax.exam.options.WarProbeOption;
import org.ops4j.spi.ServiceProviderFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A {@link TestContainer} for the GlassFish 3.1 Java EE 6 application server.
 * <p>
 * This container uses Embedded GlassFish and only Java EE mode, but not OSGi mode. You deploy WAR
 * modules via Pax Exam options (but no OSGi bundles).
 * <p>
 * The test probe is a WAR built on the fly from the classpath contents with some default
 * exclusions.
 * <p>
 * GlassFish logging is redirected from java.util.logging to SLF4J. The necessary artifacts are
 * provisioned by this container automatically.
 *
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class EmbeddedGlassFishTestContainer implements TestContainer {

    /**
     * Configuration property key for GlassFish installation configuration file directory. The files
     * contained in this directory will be copied to the config directory of the GlassFish instance.
     */
    public static final String GLASSFISH_CONFIG_DIR_KEY = "pax.exam.glassfish.config.dir";

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedGlassFishTestContainer.class);

    /**
     * Name of the probe web application (in Java EE mode).
     */
    private static final String PROBE_APPLICATION_NAME = "Pax-Exam-Probe";

    /**
     * XPath to read the HTTP port from the domain.xml configuration file.
     */
    private static final String HTTP_PORT_XPATH = "/domain/configs/config/network-config/network-listeners/network-listener[@name='http-listener-1']/@port";

    /**
     * Stack of deployed modules. On shutdown, the modules are undeployed in reverse order.
     */
    private Stack<String> deployed = new Stack<String>();

    private String warProbe;

    /**
     * Pax Exam system with configuration options.
     */
    private ExamSystem system;

    /**
     * GlassFish OSGi service.
     */
    private GlassFish glassFish;

    /**
     * Test directory which tracks all tests in the current suite. We need to register the context
     * URL of the probe web app as access point.
     */
    private TestDirectory testDirectory;

    private String configDirName;

    /**
     * Creates a GlassFish container, running on top of an OSGi framework.
     *
     * @param system
     *            Pax Exam system configuration
     */
    public EmbeddedGlassFishTestContainer(ExamSystem system) {
        this.system = system;
        this.testDirectory = TestDirectory.getInstance();
    }

    /**
     * Installs a probe in the test container.
     * <p>
     * In Java EE mode, the probe is a WAR, enriched by the Pax Exam servlet bridge which allows us
     * to invoke tests running within the container via an HTTP client.
     *
     * @param location
     *            bundle location, not used for WAR probes
     * @param stream
     *            input stream containing probe
     * @return bundle ID, or -1 for WAR
     */
    private synchronized long install(String location, InputStream stream) {
        try {
            // just make sure we don't get an "option not recognized" warning
            system.getOptions(WarProbeOption.class);

            LOG.info("deploying probe");
            Deployer deployer = glassFish.getDeployer();

            /*
             * FIXME The following should work, but does not. For some reason, we cannot directly
             * deploy from a stream. As a workaround, we copy the stream to a temp file and deploy
             * the file.
             *
             * deployer.deploy( stream, "--name", "Pax-Exam-Probe", "--contextroot",
             * "Pax-Exam-Probe" );
             */

            File tempFile = File.createTempFile("pax-exam", ".war");
            tempFile.deleteOnExit();
            StreamUtils.copyStream(stream, new FileOutputStream(tempFile), true);
            deployer.deploy(tempFile, "--name", PROBE_APPLICATION_NAME, "--contextroot",
                PROBE_APPLICATION_NAME);
            deployed.push(PROBE_APPLICATION_NAME);
        }
        catch (GlassFishException exc) {
            throw new TestContainerException(exc);
        }
        catch (IOException exc) {
            throw new TestContainerException(exc);
        }
        return -1;
    }

    private synchronized long install(InputStream stream) {
        return install("local", stream);
    }

    /**
     * Deploys all Java EE modules defined in Pax Exam options. For options without an explicit
     * application name, names app1, app2 etc. are generated on the fly. The context root defaults
     * to the application name if not set in the option.
     */
    public void deployModules() {
        UrlDeploymentOption[] deploymentOptions = system.getOptions(UrlDeploymentOption.class);
        int numModules = 0;
        for (UrlDeploymentOption option : deploymentOptions) {
            numModules++;
            if (option.getName() == null) {
                option.name("app" + numModules);
            }
            deployModule(option);
        }
    }

    /**
     * Deploys the module specified by the given option.
     *
     * @param option
     *            deployment option
     */
    private void deployModule(UrlDeploymentOption option) {
        try {
            String url = option.getURL();
            LOG.info("deploying module {}", url);
            URI uri = new URL(url).toURI();
            String applicationName = option.getName();
            String contextRoot = option.getContextRoot();
            if (contextRoot == null) {
                contextRoot = applicationName;
            }
            Deployer deployer = glassFish.getDeployer();
            deployer.deploy(uri, "--name", applicationName, "--contextroot", applicationName);
            deployed.push(applicationName);
            LOG.info("deployed module {}", url);
        }
        catch (IOException exc) {
            throw new TestContainerException(exc);
        }
        catch (GlassFishException exc) {
            throw new TestContainerException(exc);
        }
        catch (URISyntaxException exc) {
            throw new TestContainerException(exc);
        }
    }

    /**
     * Undeploys all modules and shuts down the GlassFish runtime.
     */
    public synchronized void cleanup() {
        undeployModules();
        try {
            glassFish.stop();
        }
        catch (GlassFishException exc) {
            throw new TestContainerException(exc);
        }
    }

    /**
     * Undeploys all deployed modules in reverse order.
     */
    private void undeployModules() {
        try {
            Deployer deployer = glassFish.getDeployer();
            while (!deployed.isEmpty()) {
                String applicationName = deployed.pop();
                deployer.undeploy(applicationName);
            }
        }
        catch (GlassFishException exc) {
            throw new TestContainerException(exc);
        }
    }

    /**
     * Starts the GlassFish container.
     */
    @Override
    public TestContainer start() {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        ConfigurationManager cm = new ConfigurationManager();
        configDirName = cm.getProperty(GLASSFISH_CONFIG_DIR_KEY,
            "src/test/resources/glassfish-config");
        File domainConfig = new File(configDirName, "domain.xml");
        GlassFishProperties gfProps = new GlassFishProperties();
        if (domainConfig.exists()) {
            gfProps.setConfigFileURI(domainConfig.toURI().toString());
        }

        try {
            glassFish = GlassFishRuntime.bootstrap().newGlassFish(gfProps);
            glassFish.start();

            // set access point in test directory
            String portNumber = getPortNumber(domainConfig);
            testDirectory.setAccessPoint(new URI("http://localhost:" + portNumber
                + "/Pax-Exam-Probe/"));

            deployModules();
        }
        catch (GlassFishException e) {
            throw new TestContainerException("Problem starting test container.", e);
        }
        catch (URISyntaxException e) {
            throw new TestContainerException("Problem starting test container.", e);
        }
        return this;
    }

    /**
     * Reads the first port number from the domain.xml configuration.
     *
     * @param domainConfig
     * @return port number as string
     */
    private String getPortNumber(File domainConfig) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(domainConfig);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xPath = xpf.newXPath();
            String port = xPath.evaluate(HTTP_PORT_XPATH, doc);
            return port;
        }
        catch (ParserConfigurationException exc) {
            throw new IllegalArgumentException(exc);
        }
        catch (SAXException exc) {
            throw new IllegalArgumentException(exc);
        }
        catch (IOException exc) {
            throw new IllegalArgumentException(exc);
        }
        catch (XPathExpressionException exc) {
            throw new IllegalArgumentException(exc);
        }
    }

    /**
     * Stops the test container gracefully, undeploying all modules and uninstalling all bundles.
     */
    @Override
    public TestContainer stop() {
        if (glassFish != null) {
            cleanup();
            system.clear();
        }
        else {
            LOG.warn("Framework does not exist. Called start() before ? ");
        }
        return this;
    }

    @Override
    public String toString() {
        return "EmbeddedGlassFish";
    }

    @Override
    public long installProbe(InputStream stream) {
        install(stream);
        this.warProbe = deployed.pop();
        return -1;
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) {
        ProbeInvokerFactory probeInvokerFactory = ServiceProviderFinder
            .loadUniqueServiceProvider(ProbeInvokerFactory.class);
        ProbeInvoker invoker = probeInvokerFactory.createProbeInvoker("", ";");
        invoker.runTest(description, listener);
    }
}
