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
package org.ops4j.pax.exam.rbc.internal;

import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.ops4j.pax.exam.RelativeTimeout;
import org.osgi.framework.BundleException;

/**
 * Remote BundleContext like access.
 * <p>
 * TODO: this is probably somewhat too fluffy (too shaky interface) - improve here
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 10, 2008
 */
public interface RemoteBundleContext extends Remote {

    /**
     * Timeout specifing that there should be no waiting.
     */
    long NO_WAIT = 0;
    /**
     * Timeout specifing that it should wait forever.
     */
    long WAIT_FOREVER = Long.MAX_VALUE;

    /**
     * Makes a remote call on a service.
     * 
     * @param serviceType
     *            service class of the remote service
     * @param methodName
     *            method name
     * @param methodParamTypes
     *            method parameters types
     * @param filter
     *            service filter           
     * @param timeout
     *            timeout for looking up the service
     * @param actualParams
     *            actual parameters (must match the given method params)
     * @return remote call rsult
     * @throws NoSuchServiceException
     *             - If a service of the specified type cannot be located
     * @throws NoSuchMethodException
     *             - If the given method cannot be found
     * @throws IllegalAccessException
     *             - Specified method cannot be accessed
     * @throws InvocationTargetException
     *             - Wraps an eventual exception occured during method invocation
     * @throws RemoteException
     *             - Remote communication related exception (mandatory by RMI)
     */
    Object remoteCall(Class<?> serviceType, String methodName, Class<?>[] methodParamTypes,
        String filter, RelativeTimeout timeout, Object... actualParams) throws RemoteException,
        NoSuchServiceException, NoSuchMethodException, IllegalAccessException,
        InvocationTargetException;

    /**
     * Installs a bundle remotly.
     * 
     * @param bundleUrl
     *            url of the bundle to be installed. The url must be accessible from the remote OSGi
     *            container.
     * @return bundle id of the installed bundle
     * @throws RemoteException
     *             - Remote communication related exception (mandatory by RMI)
     * @throws BundleException
     *             - Re-thrown from installing the bundle
     */
    long installBundle(String bundleUrl) throws RemoteException, BundleException;

    /**
     * Installs a bundle remotly given the bundle content.
     * 
     * @param bundleLocation
     *            bundle location
     * @param bundle
     *            bundle content as a byte array
     * @return bundle id of the installed bundle
     * @throws RemoteException
     *             - Remote communication related exception (mandatory by RMI)
     * @throws BundleException
     *             - Re-thrown from installing the bundle
     */
    long installBundle(String bundleLocation, byte[] bundle) throws RemoteException,
        BundleException;

    /**
     * Starts a bundle.
     * 
     * @param bundleId
     *            id of the bundle to be started
     * @throws RemoteException
     *             - Remote communication related exception (mandatory by RMI)
     * @throws BundleException
     *             - Re-thrown from starting the bundle
     */
    void startBundle(long bundleId) throws RemoteException, BundleException;

    /**
     * Stops a bundle.
     * 
     * @param bundleId
     *            id of the bundle to be stopped
     * @throws RemoteException
     *             - Remote communication related exception (mandatory by RMI)
     * @throws BundleException
     *             - Re-thrown from stopping the bundle
     */
    void stopBundle(long bundleId) throws RemoteException, BundleException;

    /**
     * Sets bundle start level.
     * 
     * @param bundleId
     *            id of the bundle to which the start level should be set
     * @param startLevel
     *            bundle start level
     * @throws RemoteException
     *             - Remote communication related exception (mandatory by RMI)
     * @throws BundleException
     *             - If bundle level cannot be set
     */
    void setBundleStartLevel(long bundleId, int startLevel) throws RemoteException, BundleException;

    /**
     * Waits for a bundle to be in a certain state and returns.
     * 
     * @param bundleId
     *            bundle id
     * @param state
     *            expected state
     * @param timeout
     *            max time to wait for state
     * @throws RemoteException
     *             - Remote communication related exception (mandatory by RMI)
     * @throws BundleException
     *             - If bundle cannot be found
     * @throws org.ops4j.pax.exam.TimeoutException
     *             - if timeout occured and expected state has not being reached
     */
    void waitForState(long bundleId, int state, RelativeTimeout timeout) throws RemoteException,
        BundleException;

    /**
     * @param id
     *            of bundle to uninstall
     * @throws RemoteException
     *             - Remote communication related exception (mandatory by RMI)
     * @throws BundleException
     *             - If bundle cannot be found
     */
    void uninstallBundle(long id) throws RemoteException, BundleException;
}
