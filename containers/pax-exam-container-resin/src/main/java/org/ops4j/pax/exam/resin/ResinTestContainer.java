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
package org.ops4j.pax.exam.resin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Stack;

import org.ops4j.io.StreamUtils;
import org.ops4j.io.ZipExploder;
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
import org.ops4j.spi.ServiceProviderFinder;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caucho.resin.HttpEmbed;
import com.caucho.resin.ResinEmbed;
import com.caucho.resin.WebAppEmbed;

/**
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class ResinTestContainer implements TestContainer {

    private static final Logger LOG = LoggerFactory.getLogger(ResinTestContainer.class);

    private Stack<WebAppEmbed> deployed = new Stack<WebAppEmbed>();
    
    private WebAppEmbed probe;

    private ExamSystem system;

    private TestDirectory testDirectory;

    private File webappDir;

    private ResinEmbed resin;

    public ResinTestContainer(ExamSystem system, FrameworkFactory frameworkFactory) {
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
        deployModule("Pax-Exam-Probe", stream);
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
            deployModule(option.getName(), applUrl.openStream());
        }
        catch (MalformedURLException exc) {
            throw new TestContainerException("Problem deploying " + option, exc);
        }
        catch (IOException exc) {
            throw new TestContainerException("Problem deploying " + option, exc);
        }
    }

    private void deployModule(String applicationName, InputStream stream) {

        try {
            File warFile = File.createTempFile("paxexam", ".war");
            StreamUtils.copyStream(stream, new FileOutputStream(warFile), true);
            ZipExploder exploder = new ZipExploder();
            File appDir = new File(webappDir, applicationName);
            appDir.mkdir();
            exploder.processFile(warFile.getAbsolutePath(), appDir.getAbsolutePath());

            WebAppEmbed webapp = new WebAppEmbed("/" + applicationName, appDir.getAbsolutePath());
            resin.addWebApp(webapp);
            deployed.push(webapp);
        }
        catch (IOException exc) {
            throw new TestContainerException("Problem deploying " + applicationName, exc);
        }
    }

    public void cleanup() {
        undeployModules();
        LOG.info("stopping Resin");
        resin.stop();
    }

    private void undeployModules() {
        while (!deployed.isEmpty()) {
            WebAppEmbed webApp = deployed.pop();
            resin.removeWebApp(webApp);
        }
    }

    public TestContainer start() {
        LOG.info("starting Resin");
        File tempDir = system.getTempFolder();
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        System.setProperty("resin.home", system.getTempFolder().getPath());
        resin = new ResinEmbed();
        resin.setRootDirectory(tempDir.getPath());
        int httpPort = 9080;
        HttpEmbed http = new HttpEmbed(httpPort);

        resin.addPort(http);
        resin.start();

        webappDir = new File(tempDir, "webapps");
        webappDir.mkdir();
        try {
            testDirectory.setAccessPoint(new URI("http://localhost:" + httpPort
                + "/Pax-Exam-Probe/"));
        }
        catch (URISyntaxException exc) {
            new TestContainerException(exc);
        }
        return this;
    }

    public TestContainer stop() {
        cleanup();
        system.clear();
        return this;
    }

    @Override
    public String toString() {
        return "Resin";
    }

    @Override
    public long installProbe(InputStream stream) {
        deployModule("Pax-Exam-Probe", stream);
        probe = deployed.pop();
        return -1;
    }

    @Override
    public void uninstallProbe() {
        resin.removeWebApp(probe);
        probe = null;
    }
}
