/*
 * Copyright 2008 Toni Menzel
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.rbc.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Callable;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.rbc.Constants;
import org.ops4j.pax.swissbox.core.ContextClassLoaderUtils;

/**
 * Registers the an instance of RemoteTestRunnerService as RMI service using a port set by system
 * property pax.exam.communication.port.
 * 
 * Test
 * 
 * @author Toni Menzel (tonit)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since Jun 2, 2008
 */
public class Activator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(Activator.class);

    private static final int MAXRETRYCOUNT = 14;
    private static final String MSG_RETRY = "RBC bind stuff failed before. Will retry again perhaps.";

    /**
     * RMI registry.
     */
    private Registry registry;
    /**
     * Strong reference to {@link RemoteBundleContext}. !Note: this must be here otherwise JVM will
     * garbage collect it and this will result in an java.rmi.NoSuchObjectException: no such object
     * in table
     */
    private volatile RemoteBundleContext remoteBundleContext;

    private Thread registerRBCThread;

    /**
     * {@inheritDoc}
     */
    public synchronized void start(final BundleContext bundleContext) throws Exception {
        String host = getHost();
        String name = getName();
        Integer port = getPort();
        if (host == null || port == null || name == null) {
            LOG.info("Name, port or host is null. So this RBC remains inactive.");
            return;
        }
        // !! Absolutely necessary for RMIClassLoading to work
        registerRBCThread = new Thread(new Runnable() {

            public void run() {
                int retries = 0;
                boolean valid = false;
                do {
                    retries++;
                    valid = register(bundleContext);
                    if (!valid) {
                        try {
                            LOG.debug(MSG_RETRY);
                            Thread.sleep(500);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                while (!Thread.currentThread().isInterrupted() && !valid && retries < MAXRETRYCOUNT);
            }
        });
        registerRBCThread.start();

    }

    private boolean register(final BundleContext bundleContext) {
        try {
            ContextClassLoaderUtils.doWithClassLoader(null, // getClass().getClassLoader()
                new Callable<Object>()

                {

                    public Object call() throws Exception {
                        // try to find port from property
                        int port = getPort();
                        String host = getHost();
                        String name = getName();

                        LOG.debug("Trying to find registry on [host=" + host + " port=" + port
                            + "]");
                        registry = LocateRegistry.getRegistry(getHost(), getPort());

                        bindRBC(registry, name, bundleContext);
                        LOG.debug("Container with name " + name + " has added its RBC");

                        return null;
                    }
                }

            );
            return true;
        }
        catch (Exception e) {
            LOG.warn("Registration of RBC failed: ", e);
        }
        return false;
    }

    private void bindRBC(Registry registry, String name, BundleContext bundleContext)
        throws RemoteException, BundleException {
        LOG.debug("Now Binding " + RemoteBundleContext.class.getSimpleName() + " as name=" + name
            + " to RMI registry");
        Remote remoteStub = UnicastRemoteObject.exportObject(
            remoteBundleContext = new RemoteBundleContextImpl(bundleContext.getBundle(0)
                .getBundleContext()), 0);
        registry.rebind(getName(), remoteStub);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void stop(BundleContext bundleContext) throws Exception {
        if (registerRBCThread != null) {
            registerRBCThread.interrupt();
            String name = getName();
            registry.unbind(name);
            UnicastRemoteObject.unexportObject(remoteBundleContext, true);

            // UnicastRemoteObject.unexportObject( registry, true );
            registry = null;
            remoteBundleContext = null;
            LOG.debug("Container with name " + name + " has removed its RBC");
        }
    }

    /**
     * @return the port where {@link RemoteBundleContext} is being exposed as an RMI service.
     * 
     * @throws BundleException
     *             - If communication port cannot be determined
     */
    private int getPort() throws BundleException {
        // The port is usually given by starting client (owner of this process).
        try {
            return Integer.parseInt(System.getProperty(Constants.RMI_PORT_PROPERTY));
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    private String getHost() throws BundleException {
        return System.getProperty(Constants.RMI_HOST_PROPERTY);

    }

    private String getName() throws BundleException {
        return System.getProperty(Constants.RMI_NAME_PROPERTY);

    }
}
