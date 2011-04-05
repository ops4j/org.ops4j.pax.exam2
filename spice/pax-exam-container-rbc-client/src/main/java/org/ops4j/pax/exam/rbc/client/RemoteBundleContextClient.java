/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.ops4j.pax.exam.rbc.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Stack;
import java.util.UUID;

import org.ops4j.io.StreamUtils;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.rbc.Constants;
import org.ops4j.pax.exam.rbc.internal.RemoteBundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link RemoteBundleContext} client, that takes away RMI handling.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 15, 2008
 */
public interface RemoteBundleContextClient {

    public long install( InputStream stream );

    public void cleanup();

    /**
     * {@inheritDoc}
     */
    public void setBundleStartLevel( final long bundleId, final int startLevel );

    /**
     * {@inheritDoc}
     */
    public void start();

    /**
     * {@inheritDoc}
     */
    public void stop();

    /**
     * {@inheritDoc}
     */
    public void waitForState( final long bundleId,
                              final int state,
                              final long timeoutInMillis );

    public void call( TestAddress address , Object... args)
        throws InvocationTargetException, ClassNotFoundException, IllegalAccessException, InstantiationException;
}
