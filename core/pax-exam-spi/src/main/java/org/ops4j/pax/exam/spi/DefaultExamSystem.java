/*
 * Copyright 2010 Toni Menzel.
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
package org.ops4j.pax.exam.spi;

import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.OptionUtils.expand;
import static org.ops4j.pax.exam.OptionUtils.filter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.RelativeTimeout;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.options.TimeoutOption;
import org.ops4j.pax.exam.options.WarProbeOption;
import org.ops4j.pax.exam.options.extra.CleanCachesOption;
import org.ops4j.pax.exam.options.extra.WorkingDirectoryOption;
import org.ops4j.pax.exam.spi.intern.TestProbeBuilderImpl;
import org.ops4j.pax.exam.spi.war.WarTestProbeBuilderImpl;
import org.ops4j.spi.ServiceProviderFinder;
import org.ops4j.store.Store;
import org.ops4j.store.intern.TemporaryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@literal DefaultExamSystem} represents the default implementation of {@link ExamSystem}.
 *
 * It takes care of options (including implicit defaults), temporary folders (and their cleanup) and
 * cross cutting parameters that are frequently used like "timeout" values.
 */
public class DefaultExamSystem implements ExamSystem {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExamSystem.class);

    /** Maximum loop count when creating temp directories. */
    private static final int TEMP_DIR_ATTEMPTS = 10000;

    private final Store<InputStream> store;
    private final File configDirectory;
    private final Stack<ExamSystem> subsystems;
    private final RelativeTimeout timeout;
    private final Set<Class<?>> requestedOptionTypes = new HashSet<Class<?>>();
    private final CleanCachesOption clean;
    private final File cache;

    private Option[] combinedOptions;

    /**
     * Creates a fresh ExamSystem. Your options will be combined with internal defaults. If you need
     * to change the system after it has been created, you fork() it. Forking will not add default
     * options again.
     *
     * @param options
     *            options to be used to define the new system.
     *
     * @throws IOException
     *             in case of an instantiation problem. (IO related)
     */
    private DefaultExamSystem(Option[] options) throws IOException {
        subsystems = new Stack<ExamSystem>();
        combinedOptions = expand(options);
        configDirectory = new File(System.getProperty("user.home") + "/.pax/exam/");
        configDirectory.mkdirs();

        WorkingDirectoryOption work = getSingleOption(WorkingDirectoryOption.class);
        // make sure that working directory gets propagated to forked systems
        if (work == null) {
            work = new WorkingDirectoryOption(createTemp(null).getAbsolutePath());
            combinedOptions = combine(combinedOptions, work);
        }

        cache = createTemp(new File(work.getWorkingDirectory()));
        store = new TemporaryStore(cache, false);

        TimeoutOption timeoutOption = getSingleOption(TimeoutOption.class);
        if (timeoutOption != null) {
            timeout = new RelativeTimeout(timeoutOption.getTimeout());
        }
        else {
            timeout = RelativeTimeout.TIMEOUT_DEFAULT;
        }
        clean = getSingleOption(CleanCachesOption.class);
    }

    /**
     * Creates a fresh ExamSystem. Your options will be combined with internal defaults. If you need
     * to change the system after it has been created, you fork() it. Forking will not add default
     * options again.
     *
     * @param options
     *            options to be used to define the new system.
     *
     * @return a fresh instance of {@literal DefaultExamSystem}
     *
     * @throws IOException
     *             in case of an instantiation problem. (IO related)
     */
    public static ExamSystem create(Option[] options) throws IOException {
        LOG.info("Pax Exam System (Version: " + Info.getPaxExamVersion() + ") created.");
        return new DefaultExamSystem(options);

    }

    /**
     * Create a new system based on *this*. The forked System remembers the forked instances in
     * order to clear resources up (if desired).
     */
    @Override
    public ExamSystem fork(Option[] options) {
        try {
            ExamSystem sys = new DefaultExamSystem(combine(combinedOptions, options));
            subsystems.add(sys);
            return sys;
        }
        catch (IOException exc) {
            throw new TestContainerException(exc);
        }
    }

    /**
     * Creates a fresh temp folder under the mentioned working folder. If workingFolder is null,
     * system wide temp location will be used.
     *
     * @param workingDirectory
     */
    private synchronized File createTemp(File workingDirectory) throws IOException {
        if (workingDirectory == null) {
            return createTempDir();
        }
        else {
            workingDirectory.mkdirs();
            return workingDirectory;
        }
    }

    /**
     * Helper method for single options. Last occurence has precedence. (support overwrite).
     *
     */
    @Override
    public <T extends Option> T getSingleOption(final Class<T> optionType) {
        requestedOptionTypes.add(optionType);
        T[] filter = filter(optionType, combinedOptions);

        if (filter.length > 0) {
            return filter[filter.length - 1];
        }
        else {
            return null;
        }
    }

    public <T extends Option> T getSingleOption(final Class<T> optionType, Option[] options) {
        T[] filter = filter(optionType, options);

        if (filter.length > 0) {
            return filter[filter.length - 1];
        }
        else {
            return null;
        }
    }

    @Override
    public <T extends Option> T[] getOptions(final Class<T> optionType) {
        requestedOptionTypes.add(optionType);
        return filter(optionType, combinedOptions);
    }

    /**
     * @return the basic directory that Exam should use to look at user-defaults.
     */
    @Override
    public File getConfigFolder() {
        return configDirectory;
    }

    /**
     * @return the basic directory that Exam should use for all IO write activities.
     */
    @Override
    public File getTempFolder() {
        return cache;
    }

    /**
     * @return a relative indication of how to deal with timeouts.
     */
    @Override
    public RelativeTimeout getTimeout() {
        return timeout;
    }

    /**
     * Clears up resources taken by system (like temporary files).
     */
    @Override
    public void clear() {
        try {
            warnUnusedOptions();

            if (clean == null || clean.getValue() == Boolean.TRUE) {
                for (ExamSystem sys : subsystems) {
                    sys.clear();
                }
                FileUtils.delete(cache.getCanonicalFile());
            }
        }
        catch (IOException e) {
            // LOG.info("Clearing " + this.toString() + " failed." ,e);
        }
    }

    private void warnUnusedOptions() {
        if (subsystems.isEmpty()) {
            for (String skipped : findOptionTypes()) {
                LOG.warn("Option " + skipped + " has not been recognized.");
            }
        }
    }

    private Set<String> findOptionTypes() {
        Set<String> missing = new HashSet<String>();
        for (Option option : combinedOptions) {
            boolean found = false;
            for (Class<?> c : requestedOptionTypes) {
                if (c.isAssignableFrom(option.getClass())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                missing.add(option.getClass().getCanonicalName());
            }
        }
        return missing;
    }

    @Override
    public TestProbeBuilder createProbe() throws IOException {

        WarProbeOption warProbeOption = getSingleOption(WarProbeOption.class);
        if (warProbeOption == null) {
            LOG.debug("creating default probe");
            TestProbeBuilderImpl testProbeBuilder = new TestProbeBuilderImpl(cache, store);
            testProbeBuilder.setHeader("Bundle-SymbolicName", "PAXEXAM-PROBE-"
                + createID("created probe"));
            return testProbeBuilder;
        }
        else {
            ConfigurationFactory configurationFactory = ServiceProviderFinder
                .findAnyServiceProvider(ConfigurationFactory.class);
            LOG.debug("creating WAR probe");
            if (configurationFactory == null) {
                return new WarTestProbeBuilderImpl(getTempFolder(), this);
            }
            else {
                Option[] configuration = configurationFactory.createConfiguration();
                Option[] tempOptions = combine(combinedOptions, configuration);
                warProbeOption = getSingleOption(WarProbeOption.class, tempOptions);
                return new WarTestProbeBuilderImpl(getTempFolder(), warProbeOption);
            }
        }
    }

    @Override
    public String createID(String purposeText) {
        return UUID.randomUUID().toString();
    }

    @Override
    public String toString() {
        return "ExamSystem:options=" + combinedOptions.length + ";queried="
            + requestedOptionTypes.size();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(combinedOptions);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DefaultExamSystem other = (DefaultExamSystem) obj;
        if (!Arrays.equals(combinedOptions, other.combinedOptions)) {
            return false;
        }
        return true;
    }

    public static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException("Failed to create directory within " + TEMP_DIR_ATTEMPTS
            + " attempts (tried " + baseName + "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');

    }

    public WarProbeOption getLatestWarProbeOption() {
        try {
            return subsystems.peek().getOption(WarProbeOption.class);
        }
        catch (ExamConfigurationException e) {
            throw new TestContainerException(e.getMessage(), e);
        }

    }
}
