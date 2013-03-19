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
package org.ops4j.pax.exam.nat.internal;

import static org.ops4j.pax.exam.Constants.EXAM_FAIL_ON_UNRESOLVED_KEY;
import static org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.osgi.framework.Constants.FRAMEWORK_BOOTDELEGATION;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;
import org.ops4j.pax.exam.options.FrameworkStartLevelOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.ValueOption;
import org.ops4j.pax.exam.options.extra.CleanCachesOption;
import org.ops4j.pax.exam.options.extra.RepositoryOption;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Native Test Container starts an OSGi framework using {@link FrameworkFactory} and provisions
 * the bundles configured in the Exam system.
 * <p>
 * When the framework has reached the configured start level, the container checks that all bundles
 * are resolved and throws an exception otherwise.
 * 
 * @author Toni Menzel
 * @author Harald Wellmann
 * @since Jan 7, 2010
 */
public class NativeTestContainer implements TestContainer {

    private static final Logger LOG = LoggerFactory.getLogger(NativeTestContainer.class);
    private static final String PROBE_SIGNATURE_KEY = "Probe-Signature";
    private final Stack<Long> installed = new Stack<Long>();

    private final FrameworkFactory frameworkFactory;
    private ExamSystem system;

    private volatile Framework framework;

    public NativeTestContainer(ExamSystem system, FrameworkFactory frameworkFactory)
        throws IOException {
        this.frameworkFactory = frameworkFactory;
        this.system = system;
    }

    @Override
    public synchronized void call(TestAddress address) {
        Map<String, String> props = new HashMap<String, String>();
        props.put(PROBE_SIGNATURE_KEY, address.root().identifier());
        BundleContext bundleContext = framework.getBundleContext();
        ProbeInvoker probeInvokerService = ServiceLookup.getService(bundleContext,
            ProbeInvoker.class, props);
        waitForBarriers(bundleContext);
        probeInvokerService.call(address.arguments());
    }

    private void waitForBarriers(BundleContext bundleContext) {
        try {
            // We try to fetch barriers, these must be present already, if none are found now, none
            // are considered!
            ServiceReference[] serviceReferences = bundleContext.getServiceReferences(
                CountDownLatch.class.getName(), "(target=paxexam.barrier)");
            if (serviceReferences != null) {
                LOG.info("{} barrier(s) found...", serviceReferences.length);
                for (ServiceReference serviceReference : serviceReferences) {
                    Object serviceObject = bundleContext.getService(serviceReference);
                    try {
                        if (serviceObject instanceof CountDownLatch) {
                            Object propertyValue = serviceReference
                                .getProperty("barrier.timeout.value");
                            Object propertyUnit = serviceReference
                                .getProperty("barrier.timeout.unit");
                            long timeout = 60;
                            TimeUnit unit = TimeUnit.SECONDS;
                            if (propertyValue != null) {
                                try {
                                    timeout = Long.parseLong(propertyValue.toString());
                                }
                                catch (NumberFormatException e) {
                                    LOG.warn(
                                        "can't parse timeout value {}, will use default value",
                                        propertyValue, e);
                                }
                            }
                            if (propertyUnit != null) {
                                try {
                                    unit = TimeUnit.valueOf(propertyUnit.toString());
                                }
                                catch (IllegalArgumentException e) {
                                    LOG.warn("can't parse timeout unit {}, will use default value",
                                        propertyUnit, e);
                                }
                            }
                            CountDownLatch barrier = (CountDownLatch) serviceObject;
                            try {
                                LOG.info("Await barrier with timeout = {} {}", timeout, unit);
                                boolean success = barrier.await(timeout, unit);
                                if (!success) {
                                    throw new TestContainerException(
                                        "Timeout while waiting for barrier");
                                }
                                LOG.info("barrier passed with success!");
                            }
                            catch (InterruptedException e) {
                                throw new TestContainerException(
                                    "Interrupted while waiting at the barrier", e);
                            }
                        }
                    }
                    finally {
                        bundleContext.ungetService(serviceReference);
                    }
                }
            }
            else {
                LOG.info("No barrier(s) found.");
            }
        }
        catch (InvalidSyntaxException e) {
            throw new AssertionError("should never happen: " + e);
        }
    }

    @Override
    public synchronized long install(String location, InputStream stream) {
        try {
            Bundle b = framework.getBundleContext().installBundle(location, stream);
            installed.push(b.getBundleId());
            LOG.debug("Installed bundle " + b.getSymbolicName() + " as Bundle ID "
                + b.getBundleId());
            setBundleStartLevel(b.getBundleId(), Constants.START_LEVEL_TEST_BUNDLE);
            b.start();
            return b.getBundleId();
        }
        catch (BundleException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public synchronized long install(InputStream stream) {
        return install("local", stream);
    }

    public synchronized void cleanup() {
        while ((!installed.isEmpty())) {
            try {
                Long id = installed.pop();
                Bundle bundle = framework.getBundleContext().getBundle(id);
                bundle.uninstall();
                LOG.debug("Uninstalled bundle " + id);
            }
            catch (BundleException e) {
                // Sometimes bundles go mad when install + uninstall happens too
                // fast.
            }
        }
    }

    public void setBundleStartLevel(long bundleId, int startLevel) {
        StartLevel sl = ServiceLookup.getService(framework.getBundleContext(), StartLevel.class);
        sl.setBundleStartLevel(framework.getBundleContext().getBundle(bundleId), startLevel);
    }

    @Override
    public TestContainer start() {
        try {
            system = system.fork(new Option[] {
                systemPackage("org.ops4j.pax.exam;version="
                    + skipSnapshotFlag(Info.getPaxExamVersion())),
                systemPackage("org.ops4j.pax.exam.options;version="
                    + skipSnapshotFlag(Info.getPaxExamVersion())),
                systemPackage("org.ops4j.pax.exam.util;version="
                    + skipSnapshotFlag(Info.getPaxExamVersion())),
                systemProperty("java.protocol.handler.pkgs").value("org.ops4j.pax.url") });
            Map<String, Object> p = createFrameworkProperties();
            if (LOG.isDebugEnabled()) {
                logFrameworkProperties(p);
                logSystemProperties();
            }
            framework = frameworkFactory.newFramework(p);
            framework.init();
            installAndStartBundles(framework.getBundleContext());
        }
        catch (BundleException e) {
            throw new TestContainerException("Problem starting test container.", e);
        }
        catch (IOException e) {
            throw new TestContainerException("Problem starting test container.", e);
        }
        return this;
    }

    private void logFrameworkProperties(Map<String, Object> p) {
        LOG.debug("==== Framework properties:");
        for (String key : p.keySet()) {
            LOG.debug("{} = {}", key, p.get(key));
        }
    }

    private void logSystemProperties() {
        LOG.debug("==== System properties:");
        SortedMap<Object, Object> map = new TreeMap<Object, Object>(System.getProperties());
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            LOG.debug("{} = {}", entry.getKey(), entry.getValue());
        }
    }

    @Override
    public TestContainer stop() {
        if (framework != null) {
            try {
                cleanup();
                stopOrAbort();
                framework = null;
                system.clear();
            }
            catch (BundleException e) {
                LOG.warn("Problem during stopping fw.", e);
            }
            catch (InterruptedException e) {
                LOG.warn("InterruptedException during stopping fw.", e);
            }
        }
        else {
            LOG.warn("Framework does not exist. Called start() before ? ");
        }
        return this;
    }

    private void stopOrAbort() throws BundleException, InterruptedException {
        framework.stop();
        long timeout = system.getTimeout().getValue();
        Thread stopper = new Stopper(timeout);
        stopper.start();
        stopper.join(timeout + 500);

        // If the framework is not stopped, then we're in trouble anyway, so we do not worry
        // about stopping the worker thread.

        if (framework.getState() != Framework.RESOLVED) {
            String message = "Framework has not yet stopped after " + timeout
                + " ms. waitForStop did not return";
            throw new TestContainerException(message);
        }
    }

    private Map<String, Object> createFrameworkProperties() throws IOException {
        final Map<String, Object> p = new HashMap<String, Object>();
        CleanCachesOption cleanCaches = system.getSingleOption(CleanCachesOption.class);
        if (cleanCaches != null && cleanCaches.getValue() != null && cleanCaches.getValue()) {
            p.put(FRAMEWORK_STORAGE_CLEAN, FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }

        p.put(FRAMEWORK_STORAGE, system.getTempFolder().getAbsolutePath());
        p.put(FRAMEWORK_SYSTEMPACKAGES_EXTRA,
            buildString(system.getOptions(SystemPackageOption.class)));
        p.put(FRAMEWORK_BOOTDELEGATION, buildString(system.getOptions(BootDelegationOption.class)));

        for (FrameworkPropertyOption option : system.getOptions(FrameworkPropertyOption.class)) {
            p.put(option.getKey(), option.getValue());
        }

        for (SystemPropertyOption option : system.getOptions(SystemPropertyOption.class)) {
            System.setProperty(option.getKey(), option.getValue());
        }

        String repositories = buildString(system.getOptions(RepositoryOption.class));
        if (!repositories.isEmpty()) {
            System.setProperty("org.ops4j.pax.url.mvn.repositories", repositories);
        }
        return p;
    }

    private String buildString(ValueOption<?>[] options) {
        return buildString(new String[0], options, new String[0]);
    }

    @SuppressWarnings("unused")
    private String buildString(String[] prepend, ValueOption<?>[] options) {
        return buildString(prepend, options, new String[0]);
    }

    @SuppressWarnings("unused")
    private String buildString(ValueOption<?>[] options, String[] append) {
        return buildString(new String[0], options, append);
    }

    private String buildString(String[] prepend, ValueOption<?>[] options, String[] append) {
        StringBuilder builder = new StringBuilder();
        for (String a : prepend) {
            builder.append(a);
            builder.append(",");
        }
        for (ValueOption<?> option : options) {
            builder.append(option.getValue());
            builder.append(",");
        }
        for (String a : append) {
            builder.append(a);
            builder.append(",");
        }
        if (builder.length() > 0) {
            return builder.substring(0, builder.length() - 1);
        }
        else {
            return "";
        }
    }

    private void installAndStartBundles(BundleContext context) throws BundleException {
        StartLevel sl = ServiceLookup.getService(context, StartLevel.class);
        List<Bundle> bundles = new ArrayList<Bundle>();
        for (ProvisionOption<?> bundle : system.getOptions(ProvisionOption.class)) {
            Bundle b = context.installBundle(bundle.getURL());
            bundles.add(b);
            int startLevel = getStartLevel(bundle);
            sl.setBundleStartLevel(b, startLevel);
            if (bundle.shouldStart()) {
                b.start();
                LOG.debug("+ Install (start@{}) {}", startLevel, bundle);
            }
            else {
                LOG.debug("+ Install (no start) {}", bundle);
            }
        }
        // All bundles are installed, we can now start the framework...
        framework.start();
        setFrameworkStartLevel(context, sl);
        verifyThatBundlesAreResolved(bundles);
    }

    private int getCurrentStartLevel(BundleContext context) {
        // We depends on OSGi 4.2, so this method is not allowed
        //        Bundle systemBundle = context.getBundle(0);
        //        FrameworkStartLevel startLevel = (FrameworkStartLevel) systemBundle.adapt(FrameworkStartLevel.class);
        //        return startLevel.getStartLevel();

        StartLevel sl = ServiceLookup.getService(context, StartLevel.class);
        if (sl == null) {
            // The service lookup failed, either the framework is not yet started, or something terrible happened
            return -1;
        } else {
            return sl.getStartLevel();
        }
    }

    private void setFrameworkStartLevel(BundleContext context, final StartLevel sl) {
        FrameworkStartLevelOption startLevelOption = system
            .getSingleOption(FrameworkStartLevelOption.class);
        final int startLevel = startLevelOption == null ? START_LEVEL_TEST_BUNDLE
            : startLevelOption.getStartLevel();


        LOG.debug("Jump to startlevel: " + startLevel + " / current start level " + getCurrentStartLevel(context));
        final CountDownLatch latch = new CountDownLatch(1);


        context.addFrameworkListener(new FrameworkListener() {

            @Override
            public void frameworkEvent(FrameworkEvent frameworkEvent) {
                if (frameworkEvent.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
                    if (sl.getStartLevel() == startLevel) {
                        latch.countDown();
                    }
                }
            }
        });
        sl.setStartLevel(startLevel);

        // Check the current start level before starting to wait.
        if (getCurrentStartLevel(context) == startLevel) {
            LOG.debug("Requested start level reached");
            return;
        } else {
            LOG.debug("startlevel: " + startLevel + "requested / current start level " + getCurrentStartLevel
                    (context));
        }

        try {
            long timeout = system.getTimeout().getValue();
            if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                // Before throwing an exception, do a last check
                if (startLevel != sl.getStartLevel()) {
                    String msg = String.format("start level %d has not been reached within %d ms",
                            startLevel, timeout);
                    throw new TestContainerException(msg);
                } else {
                    // We reached the requested start level.
                    LOG.debug("Requested start level reached");
                }

            }
        }
        catch (InterruptedException e) {
            throw new TestContainerException(e);
        }
    }

    private void verifyThatBundlesAreResolved(List<Bundle> bundles) {
        boolean hasUnresolvedBundles = false;
        for (Bundle bundle : bundles) {
            if (bundle.getState() == Bundle.INSTALLED) {
                LOG.error("Bundle [{}] is not resolved", bundle);
                hasUnresolvedBundles = true;
            }
        }
        ConfigurationManager cm = new ConfigurationManager();
        boolean failOnUnresolved = Boolean.parseBoolean(cm.getProperty(EXAM_FAIL_ON_UNRESOLVED_KEY,
            "false"));
        if (hasUnresolvedBundles && failOnUnresolved) {
            throw new TestContainerException(
                "There are unresolved bundles. See previous ERROR log messages for details.");
        }
    }

    private int getStartLevel(ProvisionOption<?> bundle) {
        Integer start = bundle.getStartLevel();
        if (start == null) {
            start = Constants.START_LEVEL_DEFAULT_PROVISION;
        }
        return start;
    }

    private String skipSnapshotFlag(String version) {
        int idx = version.indexOf("-");
        if (idx >= 0) {
            return version.substring(0, idx);
        }
        else {
            return version;
        }
    }

    @Override
    public String toString() {
        return "Native:" + frameworkFactory.getClass().getSimpleName();
    }

    /**
     * Worker thread for shutting down the framework. We'd expect Framework.waitForStop(timeout) to
     * return after the given timeout, but this is not the case with Equinox (tested on 3.6.2 and
     * 3.7.0), so we use this worker thread to avoid blocking the main thread.
     * 
     * @author Harald Wellmann
     */
    private class Stopper extends Thread {

        private final long timeout;

        private Stopper(long timeout) {
            this.timeout = timeout;
        }

        @Override
        public void run() {
            try {
                FrameworkEvent frameworkEvent = framework.waitForStop(timeout);
                if (frameworkEvent.getType() != FrameworkEvent.STOPPED) {
                    LOG.error("Framework has not yet stopped after {} ms. "
                        + "waitForStop returned: {}", timeout, frameworkEvent);
                }
            }
            catch (InterruptedException exc) {
                LOG.error("Stopper thread was interrupted");
            }
        }
    }
}
