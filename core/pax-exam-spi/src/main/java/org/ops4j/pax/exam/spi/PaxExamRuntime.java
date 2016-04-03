/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2008-2011 Toni Menzel.

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
package org.ops4j.pax.exam.spi;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.serverMode;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.CoreOptions.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pax Exam runtime.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 09, 2008
 *
 */
public class PaxExamRuntime {

    private static final Logger LOG = LoggerFactory.getLogger(PaxExamRuntime.class);

    /** Hidden utility class constructor. */
    private PaxExamRuntime() {
    }

    /**
     * Discovers the regression container. Discovery is performed via ServiceLoader discovery
     * mechanism.
     *
     * @return discovered test container
     */
    public static TestContainerFactory getTestContainerFactory() {
        sanityCheck();
        TestContainerFactory factory = ServiceLoader.load(TestContainerFactory.class).iterator()
            .next();
        LOG.debug("Found TestContainerFactory: "
            + ((factory != null) ? factory.getClass().getName() : "<NONE>"));
        return factory;
    }

    /**
     * Convenience factory when just dealing with one container (intentionally). Note, this will
     * break if there is not exaclty one container available and parsed from options. If there are
     * more containers, just the first (whatever comes first) will be picked.
     *
     * @param system
     *            to be used.
     * @return exactly one Test Container.
     */
    public static TestContainer createContainer(ExamSystem system) {
        return getTestContainerFactory().create(system)[0];
    }

    /**
     * Creates and starts a test container using options from a configuration class.
     *
     * @param configurationClassName
     *            fully qualified class name of a configuration class.
     * @return started test container
     * @throws Exception when options cannot be parsed
     */
    public static TestContainer createContainer(String configurationClassName) throws Exception {
        Option[] options = getConfigurationOptions(configurationClassName);
        ExamSystem system = DefaultExamSystem.create(options);
        TestContainer testContainer = PaxExamRuntime.createContainer(system);
        testContainer.start();
        return testContainer;
    }

    /**
     * Opens a server socket listening for text commands on the given port.
     * Each command is terminated by a newline. The server expects a "stop" command
     * followed by a "quit" command.
     *
     * @param testContainer
     * @param localPort
     */
    private static void waitForStop(TestContainer testContainer, int localPort) {
        try {
            ServerSocket serverSocket = new ServerSocket(localPort);
            Socket socket = serverSocket.accept();

            InputStreamReader isr = new InputStreamReader(socket.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(isr);
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
            PrintWriter pw = new PrintWriter(writer, true);
            boolean running = true;
            while (running) {
                String command = reader.readLine();
                LOG.debug("command = {}", command);
                if (command.equals("stop")) {
                    testContainer.stop();
                    pw.println("stopped");
                    writer.flush();
                    LOG.info("test container stopped");
                }
                else if (command.equals("quit")) {
                    LOG.debug("quitting PaxExamRuntime");
                    pw.close();
                    socket.close();
                    serverSocket.close();
                    running = false;
                }
            }
        }
        catch (IOException exc) {
            LOG.debug("socket error", exc);
        }
    }

    private static Option[] getConfigurationOptions(String configurationClassName)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException,
        InvocationTargetException {
        Class<?> klass = Class.forName(configurationClassName, true,
            PaxExamRuntime.class.getClassLoader());
        Method m = getConfigurationMethod(klass);
        Object configClassInstance = klass.newInstance();

        Option[] options = (Option[]) m.invoke(configClassInstance);
        return options;
    }

    private static Method getConfigurationMethod(Class<?> klass) {
        Method[] methods = klass.getMethods();
        for (Method m : methods) {
            Configuration conf = m.getAnnotation(Configuration.class);
            if (conf != null) {
                return m;
            }
        }
        throw new IllegalArgumentException(klass.getName() + " has no @Configuration method");
    }

    public static ExamSystem createTestSystem(Option... options) throws IOException {
        return DefaultExamSystem.create(OptionUtils.combine(options, defaultTestSystemOptions()));
    }

    public static ExamSystem createServerSystem(Option... options) throws IOException {
        return DefaultExamSystem.create(OptionUtils.combine(options, defaultServerSystemOptions()));
    }

    private static Option[] defaultTestSystemOptions() {
        ConfigurationManager cm = new ConfigurationManager();
        String logging = cm.getProperty(Constants.EXAM_LOGGING_KEY,
            Constants.EXAM_LOGGING_PAX_LOGGING);

        return new Option[] {
            bootDelegationPackage("sun.*"),
            frameworkStartLevel(Constants.START_LEVEL_TEST_BUNDLE),
            url("link:classpath:META-INF/links/org.ops4j.pax.exam.link").startLevel(
                START_LEVEL_SYSTEM_BUNDLES),
            url("link:classpath:META-INF/links/org.ops4j.pax.extender.service.link").startLevel(
                START_LEVEL_SYSTEM_BUNDLES),
            url("link:classpath:META-INF/links/org.osgi.compendium.link").startLevel(
                START_LEVEL_SYSTEM_BUNDLES),

            when(logging.equals(Constants.EXAM_LOGGING_PAX_LOGGING)).useOptions(
                url("link:classpath:META-INF/links/org.ops4j.pax.logging.api.link").startLevel(
                    START_LEVEL_SYSTEM_BUNDLES)),

            url("link:classpath:META-INF/links/org.ops4j.base.link").startLevel(
                START_LEVEL_SYSTEM_BUNDLES),
            url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.core.link").startLevel(
                START_LEVEL_SYSTEM_BUNDLES),
            url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.extender.link").startLevel(
                START_LEVEL_SYSTEM_BUNDLES),
            url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.framework.link").startLevel(
                START_LEVEL_SYSTEM_BUNDLES),
            url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.lifecycle.link").startLevel(
                START_LEVEL_SYSTEM_BUNDLES),
            url("link:classpath:META-INF/links/org.ops4j.pax.swissbox.tracker.link").startLevel(
                START_LEVEL_SYSTEM_BUNDLES),
            url("link:classpath:META-INF/links/org.apache.geronimo.specs.atinject.link")
                .startLevel(START_LEVEL_SYSTEM_BUNDLES) };
    }

    private static Option[] defaultServerSystemOptions() {
        return new Option[] { bootDelegationPackage("sun.*"), serverMode() };
    }

    /**
     * Select yourself
     *
     * @param select
     *            the exact implementation if you dont want to rely on commons util discovery or
     *            change different containers in a single project.
     *
     * @return discovered regression container
     */
    public static TestContainerFactory getTestContainerFactory(
        Class<? extends TestContainerFactory> select) {
        try {
            return select.newInstance();
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("Class  " + select
                + "is not a valid Test Container Factory.", e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Class  " + select
                + "is not a valid Test Container Factory.", e);
        }
    }

    /**
     * Exits with an exception if Classpath not set up properly.
     */
    private static void sanityCheck() {
        List<TestContainerFactory> factories = new ArrayList<TestContainerFactory>();

        Iterator<TestContainerFactory> iter = ServiceLoader.load(TestContainerFactory.class)
            .iterator();
        while (iter.hasNext()) {
            factories.add(iter.next());
        }
        if (factories.size() == 0) {
            throw new TestContainerException("No TestContainer implementation in Classpath");
        }
        else if (factories.size() > 1) {
            for (TestContainerFactory fac : factories) {
                LOG.error("Ambiguous TestContainer:  " + fac.getClass().getName());
            }
            throw new TestContainerException("Too many TestContainer implementations in Classpath");
        }
        else {
            // good!
            return;
        }
    }

    /**
     * Runs a standalone container in server mode which can be terminated gracefully by sending text
     * commands over a socket.
     * <p>
     * This class must be invoked with two arguments:
     * <ul>
     * <li>The fully qualified name of a {@code @Configuration} class
     * <li>A port number.
     * </ul>
     * After starting the container, this process will listen on the given port for a "stop"
     * command, sending a reply of "stopped". When finally receiving a "quit" command, the process
     * will exit.
     *
     * @param args
     *            command line argument
     * @throws Exception when options cannot be parsed
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException(
                "required arguments: <configuration class name> <shutdown port>");
        }
        TestContainer testContainer = createContainer(args[0]);
        waitForStop(testContainer, Integer.parseInt(args[1]));
    }
}
