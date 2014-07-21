/*
 * Copyright 2014 Harald Wellmann.
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
package org.ops4j.pax.exam.regression.multi.strategies;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.sample9.pde.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MixedStrategyTest implements Notifier, Remote {

    private static Logger LOG = LoggerFactory.getLogger(MixedStrategyTest.class);

    private List<String> messages;

    private Registry registry;

    @Override
    public void send(String msg) throws RemoteException {
        System.out.println("received: " + msg);
        messages.add(msg);
    }

    @Before
    public void setUp() throws RemoteException {
        messages = new ArrayList<String>();
        FreePort freePort = new FreePort(20000, 21000);

        int rmiPort = freePort.getPort();
        System.setProperty("pax.exam.regression.rmi", Integer.toString(rmiPort));
        registry = LocateRegistry.createRegistry(rmiPort);
        Remote remote = UnicastRemoteObject.exportObject(this, 0);
        registry.rebind("PaxExamNotifier", remote);

    }
    
    @After
    public void tearDown() throws Exception {
        registry.unbind("PaxExamNotifier");
        UnicastRemoteObject.unexportObject(this, true);
        UnicastRemoteObject.unexportObject(registry, true);
    }

    @Test
    public void runSuiteWithPerClassStrategy() throws Exception {
        checkNumberOfRestartsInSuite(3);
    }

    private void checkNumberOfRestartsInSuite(int numRestarts) throws Exception {
        JUnitCore junit = new JUnitCore();
        Result result = junit.run(C1.class, C2.class, C3.class, C4.class, C5.class);
        for (Failure failure : result.getFailures()) {
            LOG.error("failure in nested test", failure.getException());
        }
        assertThat(result.getFailureCount(), is(0));

        assertThat(messages.size(), is(numRestarts));
    }
}
