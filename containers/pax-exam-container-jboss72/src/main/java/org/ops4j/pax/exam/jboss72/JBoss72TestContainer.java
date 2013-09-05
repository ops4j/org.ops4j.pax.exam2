/*
 * Copyright 2013 Harald Wellmann
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
package org.ops4j.pax.exam.jboss72;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jboss.as.controller.client.helpers.standalone.DeploymentPlan;
import org.jboss.as.controller.client.helpers.standalone.InitialDeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentActionResult;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.as.controller.client.helpers.standalone.ServerUpdateActionResult.Result;
import org.jboss.as.embedded.EmbeddedServerFactory;
import org.jboss.as.embedded.ServerStartException;
import org.jboss.as.embedded.StandaloneServer;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.ProbeInvokerFactory;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDirectory;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.options.UrlDeploymentOption;
import org.ops4j.pax.exam.options.WarProbeOption;
import org.ops4j.pax.exam.zip.ZipInstaller;
import org.ops4j.spi.ServiceProviderFinder;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author Harald Wellmann
 * @since 3.1.0
 */
public class JBoss72TestContainer implements TestContainer {

    /**
     * Configuration property specifying the download URL for a JBoss AS distribution. The default
     * value is {@link #JBOSS72_DIST_URL_DEFAULT}.
     */
    public static final String JBOSS72_DIST_URL_KEY = "pax.exam.jboss72.dist.url";

    /**
     * Default download URL for JBoss AS distribution. Since JBoss does not publish official
     * binaries, we use an inofficial source.
     */
    public static final String JBOSS72_DIST_URL_DEFAULT = "http://www.redpill-linpro.com/sites/default/files/node_files/jboss-as-7.2.0.final_.zip";

    /**
     * Configuration property key for JBoss AS installation configuration file directory. The files
     * contained in this directory will be copied to the config directory of the JBoss AS instance.
     */
    public static final String JBOSS72_CONFIG_DIR_KEY = "pax.exam.jboss72.config.dir";

    /**
     * Configuration property key for overwriting standalone.xml and other configuration files in an
     * existing JBoss AS installation. If the value is {@code true}, existing files in
     * {@code standalone/configuration/} will be overwritten with files from {@code jboss72-config/}
     * , if present. The default value is {@code false}.
     */
    public static final String JBOSS72_CONFIG_OVERWRITE_KEY = "pax.exam.jboss72.config.overwrite";

    private static final Logger LOG = LoggerFactory.getLogger(JBoss72TestContainer.class);

    private static final String HTTP_PORT_XPATH = "/server/socket-binding-group/socket-binding[@name='http']/@port";

    private static final String MGMT_PORT_XPATH = "/server/socket-binding-group/socket-binding[@name='management-native']/@port";

    private final Stack<String> deployed = new Stack<String>();

    private final ExamSystem system;

    private final TestDirectory testDirectory;

    private String jBossHome;

    private StandaloneServer server;

    private ServerDeploymentManager deploymentManager;

    private int httpPort;

    private int mgmtPort;

    private File configSourceDir;
    private File configTargetDir;

    public JBoss72TestContainer(ExamSystem system, FrameworkFactory frameworkFactory) {
        this.system = system;
        this.testDirectory = TestDirectory.getInstance();
    }

    public synchronized void call(TestAddress address) {
        TestInstantiationInstruction instruction = testDirectory.lookup(address);
        ProbeInvokerFactory probeInvokerFactory = ServiceProviderFinder
            .loadUniqueServiceProvider(ProbeInvokerFactory.class);
        ProbeInvoker invoker = probeInvokerFactory.createProbeInvoker(null, instruction.toString());
        invoker.call(address.arguments());
    }

    public synchronized long install(String location, InputStream stream) {
        // just make sure we don't get an "option not recognized" warning
        system.getOptions(WarProbeOption.class);
        deployModule("Pax-Exam-Probe", "war", stream);
        return -1;
    }

    public synchronized long install(InputStream stream) {
        return install("local", stream);
    }

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

    private void deployModule(UrlDeploymentOption option) {
        try {
            URL applUrl = new URL(option.getURL());
            deployModule(option.getName(), option.getType(), applUrl.openStream());
        }
        catch (MalformedURLException exc) {
            throw new TestContainerException("Problem deploying " + option, exc);
        }
        catch (IOException exc) {
            throw new TestContainerException("Problem deploying " + option, exc);
        }
    }

    private void deployModule(String applicationName, String deploymentType, InputStream stream) {

        try {
            String warName = String.format("%s.%s", applicationName, deploymentType);
            InitialDeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
            DeploymentPlan plan = builder.add(warName, stream).deploy(warName).build();
            ServerDeploymentPlanResult result = deploymentManager.execute(plan).get();
            UUID actionId = plan.getDeploymentActions().get(0).getId();
            ServerDeploymentActionResult actionResult = result.getDeploymentActionResult(actionId);

            if (actionResult.getResult() != Result.EXECUTED) {
                throw new TestContainerException("problem deploying " + applicationName);
            }
            deployed.push(warName);
        }
        catch (ExecutionException exc) {
            throw new TestContainerException("Problem deploying " + applicationName, exc);
        }
        catch (InterruptedException exc) {
            throw new TestContainerException("Problem deploying " + applicationName, exc);
        }
    }

    public void cleanup() {
        undeployModules();
        if (server != null) {
            server.stop();
        }
    }

    private void undeployModules() {
        while (!deployed.isEmpty()) {
            String applicationName = deployed.pop();
            undeployModule(applicationName);
        }
    }

    private void undeployModule(String applName) {
        InitialDeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
        DeploymentPlan plan = builder.undeploy(applName).andRemoveUndeployed().build();
        ServerDeploymentPlanResult result;
        try {
            result = deploymentManager.execute(plan).get();
        }
        catch (InterruptedException exc) {
            throw new TestContainerException("problem undeploying " + applName, exc);
        }
        catch (ExecutionException exc) {
            throw new TestContainerException("problem undeploying " + applName, exc);
        }
        UUID actionId = plan.getDeploymentActions().get(0).getId();
        ServerDeploymentActionResult actionResult = result.getDeploymentActionResult(actionId);

        if (actionResult.getResult() != Result.EXECUTED) {
            throw new TestContainerException("problem undeploying " + applName);
        }
    }

    public TestContainer start() {
        installContainer();
        File tempDir = system.getTempFolder();
        File dataDir = new File(tempDir, "data");
        dataDir.mkdir();

        File configFile = new File(configTargetDir, "standalone.xml");
        if (!configFile.exists()) {
            throw new TestContainerException(configFile + " does not exist");
        }
        parseServerConfiguration(configFile);
        System.setProperty("jboss.server.data.dir", dataDir.getAbsolutePath());
        server = EmbeddedServerFactory.create(jBossHome, null, null,
        // packages to be loaded from system class loader
            "org.jboss.logging");
        try {
            server.start();
            deploymentManager = ServerDeploymentManager.Factory.create(
                InetAddress.getByName("localhost"), mgmtPort);
            testDirectory.setAccessPoint(new URI("http://localhost:" + httpPort
                + "/Pax-Exam-Probe/"));
            deployModules();
        }
        catch (ServerStartException exc) {
            throw new TestContainerException("Problem starting test container.", exc);
        }
        catch (URISyntaxException exc) {
            throw new TestContainerException("Problem starting test container.", exc);
        }
        catch (UnknownHostException exc) {
            throw new TestContainerException("Problem starting test container.", exc);
        }
        return this;
    }

    public void installContainer() {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager");
        System.setProperty("org.jboss.logging.provider", "slf4j");

        ConfigurationManager cm = new ConfigurationManager();
        jBossHome = cm.getProperty("pax.exam.jboss72.home");
        if (jBossHome == null) {
            throw new TestContainerException(
                "System property pax.exam.jboss72.home must be set to JBoss AS 7.2 install root");
        }

        String configDirName = cm.getProperty(JBOSS72_CONFIG_DIR_KEY,
            "src/test/resources/jboss72-config");
        configSourceDir = new File(configDirName);
        boolean overwriteConfig = Boolean.parseBoolean(cm.getProperty(JBOSS72_CONFIG_OVERWRITE_KEY,
            "false"));

        if (isValidInstallation()) {
            if (overwriteConfig) {
                installConfiguration();
            }
        }
        else {
            LOG.info("installing JBoss AS in {}", jBossHome);
            String distUrl = cm.getProperty(JBOSS72_DIST_URL_KEY, JBOSS72_DIST_URL_DEFAULT);
            LOG.info("installing JBoss AS 7.2 from {} in {}", distUrl, jBossHome);
            try {
                URL url = new URL(distUrl);
                File installDir = new File(jBossHome);
                File installParent = installDir.getParentFile();
                File tempInstall = new File(installParent, UUID.randomUUID().toString());
                ZipInstaller installer = new ZipInstaller(url, tempInstall.getAbsolutePath());
                installer.downloadAndInstall();
                File unpackedRoot = tempInstall.listFiles()[0];
                unpackedRoot.renameTo(installDir);
                installConfiguration();
            }
            catch (MalformedURLException exc) {
                throw new TestContainerException(exc);
            }
            catch (IOException exc) {
                throw new TestContainerException("error during JBoss AS 7.2 installation", exc);
            }
        }
    }

    private boolean isValidInstallation() {
        boolean valid = false;
        File installDir = new File(jBossHome);
        if (installDir.exists()) {
            File moduleLoader = new File(installDir, "jboss-modules.jar");
            if (!moduleLoader.exists()) {
                String msg = String.format("%s exists, but %s does not. "
                    + "This does not look like a valid JBoss AS 7.2 installation.", jBossHome,
                    moduleLoader);
                throw new TestContainerException(msg);
            }
            File modulesDir = new File(installDir, "modules");
            File systemDir = new File(modulesDir, "system");
            if (!systemDir.exists()) {
                String msg = String.format("%s does not exist. "
                    + "This does not look like a valid JBoss AS 7.2 installation.", systemDir);
                throw new TestContainerException(msg);
            }

            LOG.info("using existing JBoss AS 7.2 installation in {}", jBossHome);
            valid = true;
        }
        return valid;
    }

    /**
     * Copies all files in a user-defined configuration directory to the JBoss AS configuration
     * directory.
     */
    private void installConfiguration() {
        if (!configSourceDir.exists()) {
            throw new TestContainerException("configuration directory " + configSourceDir
                + " does not exist");
        }

        configTargetDir = new File(jBossHome, "standalone/configuration");
        for (File configFile : configSourceDir.listFiles()) {
            if (!configFile.isDirectory()) {
                File targetFile = new File(configTargetDir, configFile.getName());
                try {
                    LOG.info("copying {} to {}", configFile, targetFile);
                    FileUtils.copyFile(configFile, targetFile, null);
                }
                catch (IOException exc) {
                    throw new TestContainerException("error copying config file " + configFile, exc);
                }
            }
        }
    }

    private void parseServerConfiguration(File serverConfig) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(serverConfig);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xPath = xpf.newXPath();
            String httpPortString = substituteProperties(xPath.evaluate(HTTP_PORT_XPATH, doc));
            String mgmtPortString = substituteProperties(xPath.evaluate(MGMT_PORT_XPATH, doc));
            String portOffsetString = System.getProperty("jboss.socket.binding.port-offset", "0");
            httpPort = Integer.parseInt(httpPortString);
            mgmtPort = Integer.parseInt(mgmtPortString);
            int portOffset = Integer.parseInt(portOffsetString);
            httpPort += portOffset;
            mgmtPort += portOffset;

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

    public String substituteProperties(String value) {
        String result = value;
        if (value.startsWith("${") && value.endsWith("}")) {
            String propWithDefault = value.substring(2, value.length() - 1);
            int colon = propWithDefault.indexOf(':');
            String defaultValue = "";
            String propertyKey = propWithDefault;
            if (colon >= 0) {
                propertyKey = propWithDefault.substring(0, colon);
                defaultValue = propWithDefault.substring(colon + 1);
            }
            result = System.getProperty(propertyKey, defaultValue);
        }
        return result;
    }

    public TestContainer stop() {
        cleanup();
        system.clear();
        return this;
    }

    @Override
    public String toString() {
        return "JBoss72";
    }
}
