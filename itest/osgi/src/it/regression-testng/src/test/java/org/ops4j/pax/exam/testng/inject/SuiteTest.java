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
package org.ops4j.pax.exam.testng.inject;

import static org.testng.Assert.assertEquals;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.sample9.pde.Notifier;
import org.testng.TestNG;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SuiteTest implements Notifier, Remote {
	
	private static int portBase = 21000;

    private List<String> messages;

    @Override
    public void send(String msg) throws RemoteException {
        System.out.println("received: " + msg);
        messages.add(msg);
    }

    @BeforeMethod
    public void setUp() {
        messages = new ArrayList<String>();
    }

    @Test
    public void runSuiteWithPerSuiteStrategy() throws Exception {
        System.setProperty(Constants.EXAM_REACTOR_STRATEGY_KEY, "PerSuite");
        checkNumberOfRestartsInSuite(1);
    }

    @Test
    public void runSuiteWithPerClassStrategy() throws Exception {
        System.setProperty(Constants.EXAM_REACTOR_STRATEGY_KEY, "PerClass");
        checkNumberOfRestartsInSuite(2);
    }

    @Test
    public void runSuiteWithPerMethodStrategy() throws Exception {
        System.setProperty(Constants.EXAM_REACTOR_STRATEGY_KEY, "PerMethod");
        checkNumberOfRestartsInSuite(4);
    }

    private void checkNumberOfRestartsInSuite(int numRestarts) throws Exception {
        FreePort freePort = new FreePort(portBase, portBase + 400);
        portBase += 500;

        int rmiPort = freePort.getPort();
        System.setProperty("pax.exam.regression.rmi", Integer.toString(rmiPort));
        Registry registry = LocateRegistry.createRegistry(rmiPort);
        Remote remote = UnicastRemoteObject.exportObject(this, 0);
        registry.rebind("PaxExamNotifier", remote);

        TestNG testNG = new TestNG();
        testNG.setVerbose(0);
        testNG.setTestClasses(new Class[] { FilterTest.class, InjectTest.class });
        testNG.run();

        registry.unbind("PaxExamNotifier");
        UnicastRemoteObject.unexportObject(this, true);
        UnicastRemoteObject.unexportObject(registry, true);

        assertEquals(messages.size(), numRestarts);
    }
}
