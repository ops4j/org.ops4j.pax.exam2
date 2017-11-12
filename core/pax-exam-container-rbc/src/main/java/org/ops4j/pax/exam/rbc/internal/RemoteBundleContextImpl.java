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

import static org.ops4j.lang.NullArgumentException.validateNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Dictionary;

import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.RerunTestException;
import org.ops4j.pax.exam.TimeoutException;
import org.ops4j.pax.exam.util.Exceptions;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RemoteBundleContext} implementaton.
 *
 * @author Toni Menzel (tonit)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.1.0, June 10, 2008
 */
public class RemoteBundleContextImpl implements RemoteBundleContext, Serializable {

    private static final long serialVersionUID = 2520051681589147139L;

    /**
     * JCL Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RemoteBundleContextImpl.class);
    /**
     * Bundle context (cannot be null).
     */
    private final transient BundleContext bundleContext;

    /**
     * Constructor.
     *
     * @param bundleContext
     *            bundle context (cannot be null)
     *
     * @throws IllegalArgumentException
     *             - If bundle context is null
     */
    public RemoteBundleContextImpl(final BundleContext bundleContext) {
        validateNotNull(bundleContext, "Bundle context");
        this.bundleContext = bundleContext;
    }

    @Override
    public Object remoteCall(final Class<?> serviceType, final String methodName,
        final Class<?>[] methodParams, String filter, final RelativeTimeout timeout,
        final Object... actualParams) throws NoSuchServiceException, NoSuchMethodException,
        IllegalAccessException, InvocationTargetException {
        LOG.trace("Remote call of [" + serviceType.getName() + "." + methodName + "]");
        Object service = ServiceLookup.getService(bundleContext, serviceType, timeout.getValue(),
            filter);
        Object obj = null;
        try {
            obj = serviceType.getMethod(methodName, methodParams).invoke(service, actualParams);
        }
        catch (InvocationTargetException t) {
            if (t.getTargetException().getCause() instanceof RerunTestException) {
                LOG.debug("rerun the test");
                service = ServiceLookup.getService(bundleContext, serviceType, timeout.getValue(),
                    filter);
                obj = serviceType.getMethod(methodName, methodParams).invoke(service, actualParams);
            }
            else {
                throw t;
            }
        }
        return obj;
    }

    @Override
    public long installBundle(final String bundleUrl) throws BundleException {
        LOG.trace("Install bundle from URL [" + bundleUrl + "]");
        return bundleContext.installBundle(bundleUrl).getBundleId();
    }

    @Override
    public long installBundle(final String bundleLocation, final byte[] bundle)
        throws BundleException {
        LOG.trace("Install bundle [ location=" + bundleLocation + "] from byte array");
        try (ByteArrayInputStream inp = new ByteArrayInputStream(bundle)) {
            return bundleContext.installBundle(bundleLocation, inp).getBundleId();
        }
        catch (IOException exc) {
            throw Exceptions.unchecked(exc);
        }
    }

    @Override
    public void uninstallBundle(long id) throws BundleException {
        LOG.trace("Uninstall bundle [" + id + "] ");
        try {
            bundleContext.getBundle(id).uninstall();
        }
        catch (BundleException e) {
            LOG.error("Problem uninstalling " + id, e);
        }
    }

    @Override
    public void startBundle(long bundleId) throws BundleException {
        startBundle(bundleContext.getBundle(bundleId));
    }

    @Override
    public void stopBundle(long bundleId) throws BundleException {
        bundleContext.getBundle(bundleId).stop();

    }

    @Override
    public void setBundleStartLevel(long bundleId, int startLevel) throws RemoteException,
        BundleException {
        Bundle bundle = bundleContext.getBundle(bundleId);
        bundle.adapt(BundleStartLevel.class).setStartLevel(startLevel);
    }

    @Override
    public void waitForState(final long bundleId, final int state, final RelativeTimeout timeout) {
        Bundle bundle = bundleContext.getBundle(bundleId);
        if (bundle == null || (timeout.isNoWait() && (bundle == null || bundle.getState() < state))) {
            throw new TimeoutException("There is no waiting timeout set and bundle has state '"
                + bundleStateToString(bundle) + "' not '" + bundleStateToString(state)
                + "' as expected");
        }
        long startedTrying = System.currentTimeMillis();
        do {
            bundle = bundleContext.getBundle(bundleId);
            try {
                Thread.sleep(50);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        while ((bundle == null || bundle.getState() < state)
            && (timeout.isNoTimeout() || System.currentTimeMillis() < startedTrying
                + timeout.getValue()));

        if (bundle == null || bundle.getState() < state) {
            throw new TimeoutException("Timeout passed and bundle has state '"
                + bundleStateToString(bundle.getState()) + "' not '" + bundleStateToString(state)
                + "' as expected");
        }
    }

    /**
     * Starts a bundle.
     *
     * @param bundle
     *            bundle to be started
     *
     * @throws BundleException
     *             - If bundle cannot be started
     */
    private void startBundle(final Bundle bundle) throws BundleException {
        // Don't start if bundle already active
        int bundleState = bundle.getState();
        if (bundleState == Bundle.ACTIVE) {
            return;
        }

        // Don't start if bundle is a fragment bundle
        Dictionary<?, ?> bundleHeaders = bundle.getHeaders();
        if (bundleHeaders.get(Constants.FRAGMENT_HOST) != null) {
            return;
        }

        // Start bundle
        bundle.start();
        waitForState(bundle.getBundleId(), Bundle.ACTIVE, RelativeTimeout.TIMEOUT_DEFAULT);

        bundleState = bundle.getState();
        if (bundleState != Bundle.ACTIVE) {
            long bundleId = bundle.getBundleId();
            String bundleName = bundle.getSymbolicName();
            String bundleStateStr = bundleStateToString(bundleState);
            throw new BundleException("Bundle (" + bundleId + ", " + bundleName
                + ") not started (still " + bundleStateStr + ")");
        }
    }

    /**
     * Coverts a bundle state to its string form.
     *
     * @param bundle
     *            bundle
     *
     * @return bundle state as string
     */
    private static String bundleStateToString(Bundle bundle) {
        if (bundle == null) {
            return "not installed";
        }
        else {
            return bundleStateToString(bundle.getState());
        }
    }

    private static String bundleStateToString(int bundleState) {
        switch (bundleState) {
            case Bundle.ACTIVE:
                return "active";
            case Bundle.INSTALLED:
                return "installed";
            case Bundle.RESOLVED:
                return "resolved";
            case Bundle.STARTING:
                return "starting";
            case Bundle.STOPPING:
                return "stopping";
            case Bundle.UNINSTALLED:
                return "uninstalled";
            default:
                return "unknown (" + bundleState + ")";
        }
    }

}
