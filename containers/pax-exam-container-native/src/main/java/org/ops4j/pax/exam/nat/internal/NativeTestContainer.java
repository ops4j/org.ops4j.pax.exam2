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

import static java.util.stream.Collectors.joining;
import static org.ops4j.pax.exam.Constants.EXAM_FAIL_ON_UNRESOLVED_KEY;
import static org.ops4j.pax.exam.Constants.EXAM_SERVICE_TIMEOUT_DEFAULT;
import static org.ops4j.pax.exam.Constants.EXAM_SERVICE_TIMEOUT_KEY;
import static org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.osgi.framework.Bundle.INSTALLED;
import static org.osgi.framework.Constants.FRAMEWORK_BOOTDELEGATION;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.FrameworkEventUtils;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
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
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.osgi.framework.startlevel.FrameworkStartLevel;
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
    private final Stack<Bundle> installed = new Stack<>();

    private final FrameworkFactory frameworkFactory;
    private ExamSystem system;
    private volatile Framework framework;

    public NativeTestContainer(ExamSystem system, FrameworkFactory frameworkFactory)
        throws IOException {
        this.frameworkFactory = frameworkFactory;
        this.system = system;
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) {
        BundleContext bundleContext = framework.getBundleContext();
        ProbeInvoker probeInvokerService = ServiceLookup.getService(bundleContext, ProbeInvoker.class,
            determineExamServiceTimeout());
        probeInvokerService.runTest(description, listener);
    }

    private long determineExamServiceTimeout() {
        String timeoutProp = System.getProperty(EXAM_SERVICE_TIMEOUT_KEY,
            EXAM_SERVICE_TIMEOUT_DEFAULT);

        try {
            return Long.valueOf(timeoutProp);
        }
        catch (NumberFormatException exc) {
            LOG.warn("Invalid timeout value {}, falling back to default");
            return Long.valueOf(EXAM_SERVICE_TIMEOUT_DEFAULT);
        }
    }

    private synchronized long install(String location, InputStream stream) {
        try {
            Bundle b = framework.getBundleContext().installBundle(location, stream);
            installed.push(b);
            LOG.debug("Installed bundle {} as Bundle ID {}", b.getSymbolicName(), b.getBundleId());
            setBundleStartLevel(b.getBundleId(), Constants.START_LEVEL_TEST_BUNDLE);
            b.start();
            return b.getBundleId();
        }
        catch (BundleException exc) {
            throw new TestContainerException(exc);
        }
    }

    private synchronized long install(InputStream stream) {
        return install("local", stream);
    }

    public synchronized void cleanup() {
        while (!installed.isEmpty()) {
            Bundle bundle = installed.pop();
            try {
                bundle.uninstall();
                LOG.debug("Uninstalled bundle [{}]", bundle);
            }
            catch (BundleException exc) {
                LOG.debug("Cannot uninstall bundle " + bundle, exc);
            }
        }
    }

    public void setBundleStartLevel(long bundleId, int startLevel) {
        Bundle bundle = framework.getBundleContext().getBundle(bundleId);
        BundleStartLevel sl = bundle.adapt(BundleStartLevel.class);
        sl.setStartLevel(startLevel);
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
            setSystemProperties();
            createFramework(createFrameworkProperties());
            installAndStartBundles();
            startFramework();
        }
        catch (BundleException | IOException exc) {
            throw new TestContainerException("Problem starting test container.", exc);
        }
        return this;
    }

    private void createFramework(Map<String, String> p) throws BundleException {
        framework = frameworkFactory.newFramework(p);
        framework.init();
        framework.getBundleContext().addFrameworkListener(this::logFrameworkEvent);
    }

    private void startFramework() throws BundleException {
        framework.start();
        setFrameworkStartLevel();
        verifyThatBundlesAreResolved();
    }

    private void logFrameworkEvent(FrameworkEvent frameworkEvent) {
        if (frameworkEvent.getType() == FrameworkEvent.ERROR) {
            LOG.error(
                String.format("Framework ERROR event %s", frameworkEvent.toString()),
                frameworkEvent.getThrowable());
        }
        else {
            LOG.debug(String.format("Framework event type %s: %s",
                FrameworkEventUtils.getFrameworkEventString(frameworkEvent.getType()),
                frameworkEvent.toString()), frameworkEvent.getThrowable());
        }
    }

    private void logFrameworkProperties(Map<String, String> map) {
        LOG.debug("==== Framework properties:");
        map.forEach((k, v) -> LOG.debug("{} = {}", k, v));
    }

    private void logSystemProperties() {
        LOG.debug("==== System properties:");
        SortedMap<Object, Object> map = new TreeMap<>(System.getProperties());
        map.forEach((k, v) -> LOG.debug("{} = {}", k, v));
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
            catch (BundleException exc) {
                LOG.warn("Problem during stopping fw.", exc);
            }
            catch (InterruptedException exc) {
                LOG.warn("InterruptedException during stopping fw.", exc);
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

        if (framework.getState() != Bundle.RESOLVED) {
            String message = "Framework has not yet stopped after " + timeout
                + " ms. waitForStop did not return";
            throw new TestContainerException(message);
        }
    }

    private Map<String, String> createFrameworkProperties() throws IOException {
        final Map<String, String> p = new HashMap<>();
        CleanCachesOption cleanCaches = system.getSingleOption(CleanCachesOption.class);
        if (cleanCaches != null && cleanCaches.getValue() != null && cleanCaches.getValue()) {
            p.put(FRAMEWORK_STORAGE_CLEAN, FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }

        p.put(FRAMEWORK_STORAGE, system.getTempFolder().getAbsolutePath());
        p.put(FRAMEWORK_SYSTEMPACKAGES_EXTRA,
            buildString(system.getOptions(SystemPackageOption.class)));
        p.put(FRAMEWORK_BOOTDELEGATION, buildString(system.getOptions(BootDelegationOption.class)));

        for (FrameworkPropertyOption option : system.getOptions(FrameworkPropertyOption.class)) {
            p.put(option.getKey(), (String) option.getValue());
        }

        if (LOG.isDebugEnabled()) {
            logFrameworkProperties(p);
        }
        return p;
    }

    private void setSystemProperties() {
        for (SystemPropertyOption option : system.getOptions(SystemPropertyOption.class)) {
            System.setProperty(option.getKey(), option.getValue());
        }

        String repositories = buildString(system.getOptions(RepositoryOption.class));
        if (!repositories.isEmpty()) {
            System.setProperty("org.ops4j.pax.url.mvn.repositories", repositories);
        }
        if (LOG.isDebugEnabled()) {
            logSystemProperties();
        }
    }

    private String buildString(ValueOption<?>[] options) {
        return Arrays.stream(options).map(o -> o.getValue().toString()).collect(joining(","));
    }

    private void installAndStartBundles() throws BundleException {
        Arrays.stream(system.getOptions(ProvisionOption.class))
            .forEach(this::installAndStartBundle);
    }

    private void installAndStartBundle(ProvisionOption<?> bundle) {
        try {
            Bundle b = framework.getBundleContext().installBundle(bundle.getURL());
            int startLevel = getStartLevel(bundle);
            BundleStartLevel sl = b.adapt(BundleStartLevel.class);
            sl.setStartLevel(startLevel);
            if (bundle.shouldStart()) {
                b.start();
                LOG.debug("+ Install (start@{}) {}", startLevel, bundle);
            }
            else {
                LOG.debug("+ Install (no start) {}", bundle);
            }
        }
        catch (BundleException exc) {
            throw new TestContainerException("Error starting bundle " + bundle.getURL(), exc);
        }
    }

    private void setFrameworkStartLevel() {
        FrameworkStartLevel sl = framework.adapt(FrameworkStartLevel.class);
        FrameworkStartLevelOption startLevelOption = system
            .getSingleOption(FrameworkStartLevelOption.class);
        final int startLevel = startLevelOption == null ? START_LEVEL_TEST_BUNDLE
            : startLevelOption.getStartLevel();
        LOG.debug("Jump to startlevel: " + startLevel);
        final CountDownLatch latch = new CountDownLatch(1);
        framework.getBundleContext().addFrameworkListener(frameworkEvent -> {
            if (frameworkEvent.getType() == FrameworkEvent.STARTLEVEL_CHANGED) {
                if (sl.getStartLevel() == startLevel) {
                    latch.countDown();
                }
            }});
        sl.setStartLevel(startLevel);

        // Check the current start level before starting to wait.
        if (sl.getStartLevel() == startLevel) {
            LOG.debug("requested start level reached");
            return;
        }
        else {
            LOG.debug("start level {} requested, current start level is {}", startLevel,
                sl.getStartLevel());
        }

        try {
            long timeout = system.getTimeout().getValue();
            if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                // Before throwing an exception, do a last check
                if (startLevel != sl.getStartLevel()) {
                    String msg = String.format("start level %d has not been reached within %d ms",
                        startLevel, timeout);
                    throw new TestContainerException(msg);
                }
                else {
                    LOG.debug("requested start level reached");
                }

            }
        }
        catch (InterruptedException exc) {
            throw new TestContainerException(exc);
        }
    }

    private void verifyThatBundlesAreResolved() {
        boolean hasUnresolvedBundles = Arrays.stream(framework.getBundleContext().getBundles())
            .filter(b -> b.getState() == INSTALLED)
            .peek(b -> LOG.error("Bundle [{}] is not resolved", b)).count() > 0;

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

    @Override
    public synchronized void installProbe(InputStream stream) throws IOException {
        install(stream);
    }

}
