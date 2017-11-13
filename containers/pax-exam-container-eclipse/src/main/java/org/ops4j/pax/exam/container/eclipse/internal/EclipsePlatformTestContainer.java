/*
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
package org.ops4j.pax.exam.container.eclipse.internal;

import static org.ops4j.pax.exam.Constants.EXAM_SERVICE_TIMEOUT_DEFAULT;
import static org.ops4j.pax.exam.Constants.EXAM_SERVICE_TIMEOUT_KEY;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.osgi.framework.Constants.FRAMEWORK_BOOTDELEGATION;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.osgi.internal.location.EquinoxLocations;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.container.eclipse.CopyFilesOption;
import org.ops4j.pax.exam.container.eclipse.EclipseApplicationOption;
import org.ops4j.pax.exam.container.eclipse.EclipseDirectoryLayout;
import org.ops4j.pax.exam.container.eclipse.IgnoreItems;
import org.ops4j.pax.exam.container.eclipse.impl.DefaultEclipseDirectoryLayout;
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.FrameworkPropertyOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.ValueOption;
import org.ops4j.pax.exam.options.extra.CleanCachesOption;
import org.ops4j.pax.exam.options.extra.RepositoryOption;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container that supports the EclipsePlatform it uses {@link EclipseStarter} to run
 * EclipseApllications or PDE OSGi-Application
 * 
 * @author Christoph Läubrich
 * @since Jun 2017
 */
public class EclipsePlatformTestContainer implements TestContainer {

    private static final String DEFAULTSTARTLEVEL = "4";

    private static final Logger LOG = LoggerFactory.getLogger(EclipsePlatformTestContainer.class);

    private final ExamSystem system;

    private BundleContext frameworkContext;

    private final ExecutorService appExecutor = Executors.newSingleThreadExecutor();

    private final AtomicReference<Future<Object>> applicationResult = new AtomicReference<Future<Object>>(
        null);

    private ServiceTracker<ProbeInvoker, ProbeInvoker> probeInvoker;

    private final EclipseApplicationOption application;

    private final CleanCachesOption cleanCaches;

    private final IgnoreItems ignoreItems;

    public EclipsePlatformTestContainer(ExamSystem system) throws ExamConfigurationException {
        this.system = system;
        application = system.getRequiredOption(EclipseApplicationOption.class);
        cleanCaches = system.getOption(CleanCachesOption.class);
        ignoreItems = system.getOption(IgnoreItems.class);
    }

    @Override
    public void start() {
        ExamSystem fork = system.fork(new Option[] {
            systemPackage("org.ops4j.pax.exam.container.eclipse;version="
                + skipSnapshotFlag(Info.getPaxExamVersion())),
            systemPackage(
                "org.ops4j.pax.exam;version=" + skipSnapshotFlag(Info.getPaxExamVersion())),
            systemPackage(
                "org.ops4j.pax.exam.options;version=" + skipSnapshotFlag(Info.getPaxExamVersion())),
            systemPackage(
                "org.ops4j.pax.exam.util;version=" + skipSnapshotFlag(Info.getPaxExamVersion())),
            systemProperty("java.protocol.handler.pkgs").value("org.ops4j.pax.url"),
            application.getProduct().getLauncher().getProvision().asOption()

        });
        DefaultEclipseDirectoryLayout layout = new DefaultEclipseDirectoryLayout(
            fork.getTempFolder());
        Map<String, String> initialProperties = createFrameworkProperties(fork,
            setLocationProperties(createEclipseDefaults(), layout));
        try {
            layout.create();
            for (CopyFilesOption option : fork.getOptions(CopyFilesOption.class)) {
                option.copyTo(layout);
            }
            String bundles = createBundleString(fork);
            initialProperties.put(EclipseStarter.PROP_BUNDLES, bundles);
            initialProperties.put("eclipse.startTime", String.valueOf(System.currentTimeMillis()));
            EclipseStarter.setInitialProperties(initialProperties);
            LOG.info("[ Starting Eclipse Framework ]");
            LOG.info("Storage area is {}", layout.getBaseFolder().getAbsolutePath());
            logProperties("System-Properties", System.getProperties());
            logProperties("Framework-Properties", initialProperties);
            frameworkContext = EclipseStarter.startup(new String[] {}, null);
            LOG.info("Framework is up and running!");
            probeInvoker = new ServiceTracker<>(frameworkContext, ProbeInvoker.class, null);
            probeInvoker.open();
            ServiceTracker<EnvironmentInfo, Object> envInfoTracker = new ServiceTracker<>(
                frameworkContext, EnvironmentInfo.class, null);
            boolean ignoreApp;
            if (application == null) {
                ignoreApp = true;
            }
            else {
                try {
                    envInfoTracker.open();
                    final EnvironmentInfo equinoxConfig = (EnvironmentInfo) envInfoTracker
                        .waitForService(determineExamServiceTimeout());
                    String property = equinoxConfig.getProperty(EclipseStarter.PROP_IGNOREAPP);
                    if (property != null) {
                        ignoreApp = Boolean.valueOf(property);
                    }
                    else {
                        ignoreApp = false;
                    }
                }
                finally {
                    envInfoTracker.close();
                }
            }
            if (ignoreApp) {
                LOG.info("No Eclipse Application selected...");
            }
            else {
                applicationResult.set(appExecutor.submit(new Callable<Object>() {

                    @Override
                    public Object call() throws Exception {
                        LOG.info("Launching Application...");
                        try {
                            Object run = EclipseStarter.run(null);
                            LOG.info("Eclipse Application endend with returncode: {}", run);
                            return run;
                        }
                        catch (Exception e) {
                            LOG.info("Eclipse Application endend with error: {}", e.toString());
                            throw e;
                        }
                    }
                }));

            }
        }
        catch (Exception e) {
            throw new TestContainerException("Problem starting test container.", e);
        }
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

    private void logProperties(String type, Map<?, ?> properties) {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            SortedMap<Object, Object> map = new TreeMap<Object, Object>(properties);
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                sb.append("\r\n\t");
                sb.append(entry.getKey());
                sb.append(" = ");
                sb.append(entry.getValue());
            }
            LOG.debug("=== {} ==={}", type, sb);
        }
    }

    @Override
    public void installProbe(InputStream stream) throws IOException {
        try {
            Bundle b = frameworkContext.installBundle(system.createID("TESTPROBE"), stream);
            b.start();
        }
        catch (BundleException e) {
            throw new IOException("Problem installing test-probe!", e);
        }
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

    private String createBundleString(ExamSystem system) throws IOException {
        int defaultStartLevel = Integer.parseInt(getFrameworkOrSystemProperty(system,
            "osgi.bundles.defaultStartLevel", DEFAULTSTARTLEVEL));
        StringBuilder bundles = new StringBuilder();
        // first check if there is a frameworkproperty set already
        for (FrameworkPropertyOption option : system.getOptions(FrameworkPropertyOption.class)) {
            if ("osgi.bundles".equalsIgnoreCase(option.getKey())) {
                bundles.append(option.getValue());
            }
        }
        if (bundles.length() == 0) {
            // also check systemproperties then...
            for (SystemPropertyOption option : system.getOptions(SystemPropertyOption.class)) {
                if ("osgi.bundles".equalsIgnoreCase(option.getKey())) {
                    bundles.append(option.getValue());
                }
            }
        }
        for (ProvisionOption<?> bundle : system.getOptions(ProvisionOption.class)) {
            if (bundles.length() > 0) {
                bundles.append(',');
            }
            if (ignoreItems != null && ignoreItems.isIgnored(bundle.getURL())) {
                LOG.info("- Ignore ({}) since it is on the ignore list...", bundle.getURL());
                continue;
            }
            bundles.append(bundle.getURL());
            Integer startLevel = bundle.getStartLevel();
            if (startLevel == null) {
                startLevel = defaultStartLevel;
            }
            if (bundle.shouldStart()) {
                bundles.append('@');
                bundles.append(startLevel);
                bundles.append(":start");
                LOG.info("+ Install (start@{}) {}", startLevel, bundle);
            }
            else {
                LOG.info("+ Install (no start) {}", bundle);
            }

        }
        return bundles.toString();
    }

    private static String getFrameworkOrSystemProperty(ExamSystem system, String key,
        String defaultValue) {

        return defaultValue;
    }

    /**
     * here we setup some defaults required by the eclipse starter, this can be overriden by
     * frameworkProperties or systemProperties
     * 
     * @param layout
     * @return
     */
    private static Map<String, String> createEclipseDefaults() {
        HashMap<String, String> initialProperties = new HashMap<String, String>();
        initialProperties.put(EquinoxConfiguration.PROP_COMPATIBILITY_BOOTDELEGATION, "true");
        initialProperties.put(EquinoxConfiguration.PROP_COMPATIBILITY_BOOTDELEGATION
            + EquinoxConfiguration.PROP_DEFAULT_SUFFIX, "true");
        initialProperties.put(EclipseStarter.PROP_CONSOLE_LOG, "true");
        initialProperties.put("osgi.bundles.defaultStartLevel", DEFAULTSTARTLEVEL);
        return initialProperties;
    }

    private Map<String, String> createFrameworkProperties(ExamSystem system,
        final Map<String, String> defaultProperties) {
        if (cleanCaches != null && cleanCaches.getValue() != null && cleanCaches.getValue()) {
            defaultProperties.put(FRAMEWORK_STORAGE_CLEAN, FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        }
        defaultProperties.put(FRAMEWORK_SYSTEMPACKAGES_EXTRA,
            buildString(system.getOptions(SystemPackageOption.class)));
        defaultProperties.put(FRAMEWORK_BOOTDELEGATION,
            buildString(system.getOptions(BootDelegationOption.class)));

        String applicationID = application.applicationID();
        String productID = application.getProduct().productID();
        if (productID != null) {
            defaultProperties.put(EclipseApplicationOption.PROPERTY_PRODUCT_ID, productID);
        }
        if (applicationID != null) {
            defaultProperties.put(EclipseApplicationOption.PROPERTY_APPLICATION_ID, applicationID);
        }
        for (FrameworkPropertyOption option : system.getOptions(FrameworkPropertyOption.class)) {
            Object value = option.getValue();
            if (value != null) {
                defaultProperties.put(option.getKey(), value.toString());
            }
            else {
                defaultProperties.remove(option.getKey());
            }
        }
        for (SystemPropertyOption option : system.getOptions(SystemPropertyOption.class)) {
            System.setProperty(option.getKey(), option.getValue());
        }

        String repositories = buildString(system.getOptions(RepositoryOption.class));
        if (!repositories.isEmpty()) {
            System.setProperty("org.ops4j.pax.url.mvn.repositories", repositories);
        }
        return defaultProperties;
    }

    private static String buildString(ValueOption<?>[] options) {
        return buildString(new String[0], options, new String[0]);
    }

    private static String buildString(String[] prepend, ValueOption<?>[] options, String[] append) {
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

    @Override
    public void stop() {
        if (probeInvoker != null) {
            probeInvoker.close();
        }
        LOG.info("Stopping EclipsePlatform...");
        try {
            shutdownEclipse();
            if (application != null) {
                Future<Object> result = applicationResult.getAndSet(null);
                if (result != null) {
                    application.checkResult(result);
                }
            }
        }
        finally {
            appExecutor.shutdownNow();
            LOG.info("Stopped.");
        }
    }

    private void shutdownEclipse() {
        try {
            Bundle[] bundles = EclipseStarter.getSystemBundleContext().getBundles();
            for (Bundle bundle : bundles) {
                if (bundle.getBundleId() > 0) {
                    String symbolicName = bundle.getSymbolicName();
                    Version version = bundle.getVersion();
                    try {
                        bundle.uninstall();
                    }
                    catch (BundleException e) {
                        LOG.warn("Uninstall bundle {}:{} failed: {}",
                            new Object[] { symbolicName, version, e.toString() });
                    }
                }
            }
            EclipseStarter.shutdown();
        }
        catch (Exception e) {
            LOG.warn("shutting down EclipsePlatform failed, will try to shutdown the OSGi way...",
                e);
        }
    }

    @Override
    public void runTest(TestDescription description, TestListener listener)
        throws InterruptedException {
        long serviceTimeout = determineExamServiceTimeout();
        ProbeInvoker probeInvokerService = probeInvoker.waitForService(serviceTimeout);
        if (probeInvokerService == null) {
            throw new TestContainerException(
                "can't fetch ProbeInvoker within " + serviceTimeout + "ms");
        }
        probeInvokerService.runTest(description, listener);
    }

    /**
     * Set the different locations into the properties required by equinox
     * 
     * @param properties
     */
    private static Map<String, String> setLocationProperties(Map<String, String> properties,
        EclipseDirectoryLayout layout) {
        // These are all given as URIs
        properties.put(EquinoxLocations.PROP_INSTALL_AREA,
            layout.getBaseFolder().toURI().toASCIIString());
        properties.put(EquinoxLocations.PROP_CONFIG_AREA,
            layout.getConfigurationFolder().toURI().toASCIIString());
        properties.put(EquinoxLocations.PROP_INSTANCE_AREA,
            layout.getBaseFolder().toURI().toASCIIString());
        properties.put(EquinoxLocations.PROP_HOME_LOCATION_AREA,
            layout.getPluginFolder().toURI().toASCIIString());
        properties.put(EquinoxLocations.PROP_USER_DIR,
            layout.getBaseFolder().toURI().toASCIIString());
        properties.put(FRAMEWORK_STORAGE, layout.getConfigurationFolder().toURI().toASCIIString());
        // These are all given as file names
        properties.put("osgi.syspath", layout.getPluginFolder().getAbsolutePath());
        properties.put("osgi.logfile",
            new File(layout.getBaseFolder(), "logfile.log").getAbsolutePath());
        properties.put("osgi.tracefile",
            new File(layout.getBaseFolder(), "tracefile.log").getAbsolutePath());
        return properties;
    }

}
