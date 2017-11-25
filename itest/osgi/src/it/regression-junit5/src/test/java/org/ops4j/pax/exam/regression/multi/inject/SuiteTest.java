/*
 * Copyright 2016 Harald Wellmann.
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
package org.ops4j.pax.exam.regression.multi.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.PrintWriter;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.LoggingListener;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.sample9.pde.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Disabled
public class SuiteTest implements Notifier, Remote {

    private static Logger LOG = LoggerFactory.getLogger(SuiteTest.class);

    private List<String> messages;

    @Override
    public void send(String msg) throws RemoteException {
        LOG.debug("received: {}", msg);
        messages.add(msg);
    }

    @BeforeEach
    public void setUp() {
        messages = new ArrayList<String>();
    }

    @Test
    public void runSuiteWithPerClassStrategy() throws Exception {
        System.setProperty(Constants.EXAM_REACTOR_STRATEGY_KEY, "PerClass");
        checkNumberOfRestartsInSuite(2);
    }

    @Test
    public void runSuiteWithPerMethodStrategy() throws Exception {
        System.setProperty(Constants.EXAM_REACTOR_STRATEGY_KEY, "PerMethod");
        checkNumberOfRestartsInSuite(3);
    }

    @Test
    public void runSuiteWithPerSuiteStrategy() throws Exception {
        System.setProperty(Constants.EXAM_REACTOR_STRATEGY_KEY, "PerSuite");
        checkNumberOfRestartsInSuite(1);
    }

    private void checkNumberOfRestartsInSuite(int numRestarts) throws Exception {
        FreePort freePort = new FreePort(20000, 21000);

        int rmiPort = freePort.getPort();
        System.setProperty("pax.exam.regression.rmi", Integer.toString(rmiPort));
        Registry registry = LocateRegistry.createRegistry(rmiPort);
        Remote remote = UnicastRemoteObject.exportObject(this, 0);
        registry.rebind("PaxExamNotifier", remote);

        Launcher launcher = LauncherFactory.create();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClass(FilterTest.class), DiscoverySelectors.selectClass(InjectTest.class)).build();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(LoggingListener.forJavaUtilLogging(Level.FINEST), summaryListener);
        launcher.execute(request);

        TestExecutionSummary summary = summaryListener.getSummary();
        assertEquals(0, summary.getTestsFailedCount());
        summary.printFailuresTo(new PrintWriter(System.out));

        registry.unbind("PaxExamNotifier");
        UnicastRemoteObject.unexportObject(this, true);
        UnicastRemoteObject.unexportObject(registry, true);

        assertEquals(numRestarts, messages.size());
    }

    public static void main(String[] args) {
        Launcher launcher = LauncherFactory.create();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClass(SuiteTest.class)).build();
        launcher.registerTestExecutionListeners(LoggingListener.forJavaUtilLogging(Level.FINEST));
        launcher.execute(request);
    }
}
