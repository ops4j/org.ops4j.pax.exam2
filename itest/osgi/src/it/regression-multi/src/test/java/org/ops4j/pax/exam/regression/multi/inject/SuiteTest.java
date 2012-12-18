/*
 * Copyright 2012 Harald Wellmann.
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.regression.pde.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuiteTest implements Notifier, Remote {

    private static Logger LOG = LoggerFactory.getLogger(SuiteTest.class);

    private List<String> messages;

    @Override
    public void send(String msg) throws RemoteException {
        System.out.println("received: " + msg);
        messages.add(msg);
    }

    @Before
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

        JUnitCore junit = new JUnitCore();
        Result result = junit.run(FilterTest.class, InjectTest.class);
        for (Failure failure : result.getFailures()) {
            LOG.error("failure in nested test", failure.getException());
        }
        assertThat(result.getFailureCount(), is(0));

        registry.unbind("PaxExamNotifier");
        UnicastRemoteObject.unexportObject(this, true);
        UnicastRemoteObject.unexportObject(registry, true);

        assertThat(messages.size(), is(numRestarts));
    }
}
