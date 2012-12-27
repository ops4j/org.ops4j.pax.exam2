/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.container.remote;

import java.io.InputStream;

import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.ops4j.pax.exam.rbc.client.intern.RemoteBundleContextClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * TODO Needs JavaDoc
 * 
 * @author Toni Menzel
 * @since Jan 25, 2010
 */
public class RBCRemoteTarget implements TestContainer {

    private static final Logger LOG = LoggerFactory.getLogger(RBCRemoteTarget.class);

    private RemoteBundleContextClient remoteBundleContextClient;

    /**
     * @param registry
     * @param name
     * @param timeout
     */
    public RBCRemoteTarget(String name, Integer registry, RelativeTimeout timeout) {
        remoteBundleContextClient = new RemoteBundleContextClientImpl(name, registry, timeout);
    }

    /**
     * This access is kind of sneaky. Need to improve here. Usually this kind of raw access should
     * not be allowed.
     * 
     * @return underlying access
     */
    public RemoteBundleContextClient getClientRBC() {
        return remoteBundleContextClient;
    }

    public void call(TestAddress address) {
        LOG.debug("call [" + address + "]");
        remoteBundleContextClient.call(address);
    }

    public TestContainer start() {
        return this;
    }

    public long install(String location, InputStream probe) {
        LOG.debug("Preparing and Installing bundle (from stream )..");

        long id = 0;
        id = remoteBundleContextClient.install(location, probe);
        LOG.debug("Installed bundle (from stream)" + " as ID: " + id);
        return id;
    }

    public long install(InputStream probe) {
        return install("local", probe);
    }

    public TestContainer stop() {
        remoteBundleContextClient.cleanup();

        return this;
    }

}
