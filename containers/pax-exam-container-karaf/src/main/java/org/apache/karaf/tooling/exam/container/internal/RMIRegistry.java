/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.tooling.exam.container.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.ops4j.net.FreePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Graceful RMI registry creation/reuse. Tries to reuse an existing one but is fine with creating
 * one on another port.
 */
public class RMIRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(RMIRegistry.class);
    private static final int UNSELECTED = -1;
    private static final int TREASURE = 30;

    @SuppressWarnings("unused")
    private final Integer defaultPort;

    private final String host;
    private Integer port = UNSELECTED;
    private Integer altMin;
    private Integer altTo;

    public RMIRegistry(Integer defaultPort, Integer alternativeRangeFrom, Integer alternativeRangeTo) {
        try {
            host = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            throw new IllegalStateException(
                "Cannot select localhost. That usually not a good sign for networking..");
        }
        this.defaultPort = defaultPort;
        this.altMin = alternativeRangeFrom;
        this.altTo = alternativeRangeTo;
    }

    /**
     * This will make sure a registry exists and is valid m_port. If its not available or does not
     * work for some reason, it will select another port. This should really not happen usually. But
     * it can.
     * 
     * @return this for fluent API. Or IllegalStateException if a port has not been detected
     *         successfully.
     */
    public synchronized RMIRegistry selectGracefully() {
        // if( ( m_port = select( m_defaultPort ) ) == UNSELECTED ) {
        int alternativePort = new FreePort(altMin, altTo).getPort();
        port = select(alternativePort);
        if (port == UNSELECTED) {
            throw new IllegalStateException("No port found for RMI at all. Even though "
                + alternativePort + " should have worked. Thats.. not. good. at. all.");
        }
        printTakenStatus();
        // }

        return this;
    }

    private void printTakenStatus() {

        int inUse = port - altMin + 1; // the one we just took
        int max = altTo - altMin;
        String info = "Currently " + inUse + " out of " + max
            + " ports are in use. Port range is from " + altMin + " up to " + altTo;

        if (inUse + TREASURE > max) {
            LOG.warn("--------------");
            LOG.warn("BEWARE !!! " + info);
            LOG.warn("--------------");
        }
        else {
            LOG.debug(info);
        }
    }

    /**
     * This contains basically two paths: 1. check if the given port already is valid rmi registry.
     * Use that one if possible 2. make a new one at that port otherwise. Must also be validated.
     * 
     * @param portNumber
     *            to select.
     * @return input port if successful or UNSELECTED
     */
    private Integer select(int portNumber) {
        if (reuseRegistry(portNumber)) {
            LOG.debug("Reuse Registry on " + portNumber);
            return portNumber;

        }
        else if (createNewRegistry(portNumber)) {
            LOG.debug("Created Registry on " + portNumber);
            return portNumber;
        }
        // fail
        return UNSELECTED;

    }

    private boolean createNewRegistry(int portNumber) {
        try {
            Registry registry = LocateRegistry.createRegistry(portNumber);

            return verifyRegistry(registry);

        }
        // CHECKSTYLE:SKIP
        catch (Exception e) {
            //
        }

        return false;
    }

    private boolean reuseRegistry(int portNumber) {
        Registry reg = null;
        try {
            reg = LocateRegistry.getRegistry(portNumber);
            return verifyRegistry(reg);

        }
        // CHECKSTYLE:SKIP
        catch (Exception e) {
            // exception? then its not a fine registry.
        }
        return false;

    }

    private boolean verifyRegistry(Registry reg) {
        if (reg != null) {
            // test:
            try {
                String[] objectsRemote = reg.list();

                for (String r : objectsRemote) {
                    LOG.info("-- Remotely available already: " + r);
                }
                return true;

            }
            // CHECKSTYLE:SKIP
            catch (Exception ex) {
                // exception? then its not a fine registry.
            }
        }
        return false;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}
