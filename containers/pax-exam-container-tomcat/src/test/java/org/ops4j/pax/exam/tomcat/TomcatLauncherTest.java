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
package org.ops4j.pax.exam.tomcat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.util.ContextName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.io.StreamUtils;

import com.google.common.io.Files;

public class TomcatLauncherTest {

    private File tempDir;

    @Before
    public void setUp() {
        tempDir = Files.createTempDir();
    }

    @After
    public void tearDown() {
        FileUtils.delete(tempDir);
    }

    @Test
    public void launchTomcat() throws InterruptedException, IOException, LifecycleException {
        System.setProperty("java.protocol.handler.pkgs", "org.ops4j.pax.url");
        String tempDir = Files.createTempDir().getAbsolutePath();
        File baseDir = new File(tempDir, "tomcat");
        File webappDir = new File(baseDir, "webapps");
        webappDir.mkdirs();
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.getPath());
        tomcat.setPort(9080);
        tomcat.enableNaming();
        Connector connector = tomcat.getConnector();
        // see https://issues.apache.org/bugzilla/show_bug.cgi?id=50360
        connector.setProperty("bindOnInit", "false");
        Host host = tomcat.getHost();
        host.setDeployOnStartup(false);
        host.setAutoDeploy(false);
        host.setConfigClass(TomcatContextConfig.class.getName());
        TomcatHostConfig hostConfig = new TomcatHostConfig();
        host.addLifecycleListener(hostConfig);
        tomcat.start();

        File warTarget = new File(webappDir, "/wicket-examples.war");
        InputStream is = new URL("mvn:org.apache.wicket/wicket-examples/1.5.3/war").openStream();
        FileOutputStream os = new FileOutputStream(warTarget);
        StreamUtils.copyStream(is, os, true);

        hostConfig.deployWAR(new ContextName("/wicket-examples"), warTarget);
        hostConfig.unmanageApp("/wicket-examples");
        tomcat.stop();
    }
}
