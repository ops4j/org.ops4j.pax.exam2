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
package org.ops4j.pax.exam.karaf.options;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption.LogLevel;
import org.ops4j.pax.exam.options.UrlReference;
import org.ops4j.pax.exam.options.extra.VMOption;

/**
 * Final class to provide an easy and intuitive way to configure the specific karaf distribution
 * options.
 */
public final class KarafDistributionOption {

    /**
     * Hidden utility class constructor
     */
    private KarafDistributionOption() {
    }

    /**
     * Per default the folder pax-exam is deleting the test directories after a test is over. If you
     * want to keep those directories (for later evaluation) simply set this option.
     * 
     * @return keep runtime folder option
     */
    public static Option keepRuntimeFolder() {
        return new KeepRuntimeFolderOption();
    }

    /**
     * Provides an option to configure the internals of the PaxExamKaraf subsystem runner.
     * 
     * @param invoker
     *            probe invoker name
     * @return option
     */
    public static Option useOwnKarafExamSystemConfiguration(String invoker) {
        return new KarafExamSystemConfigurationOption(invoker);
    }

    public static Option waitForServiceOption(String serviceClassName, long timeout){
        return new WaitForServiceOption(serviceClassName, timeout);
    }

    /**
     * The karaf pax-logging configuration is typically not a file manipulated very often. Therefore
     * we take the freedom of adding a console logger and changing the log level directly. IF you
     * like to configure the file manually (or had so in your distribution) add this option to avoid
     * any automatic modifications to this file!
     * 
     * @return option
     */
    public static Option doNotModifyLogConfiguration() {
        return new DoNotModifyLogOption();
    }

    /**
     * This option allows to configure the start level of the bundles in the exam features
     * descriptor.
     * 
     * @param startLevel
     *            bundle start level
     * @return option
     */
    public static Option useOwnExamBundlesStartLevel(int startLevel) {
        return new ExamBundlesStartLevel(startLevel);
    }

    /**
     * Returns an option object which can be used to configure the -Dkaraf.startLocalConsole and
     * -Dkaraf.startRemoteShell options. By default both are true.
     * 
     * @return option
     */
    public static KarafDistributionConfigurationConsoleOption configureConsole() {
        return new KarafDistributionConfigurationConsoleOption(null, null);
    }

    /**
     * Returns an option object which can be used to configure the security, especially the
     * MBeanServerBuilder. By default, it's disabled.
     * 
     * @return option
     */
    public static KarafDistributionConfigurationSecurityOption configureSecurity() {
        return new KarafDistributionConfigurationSecurityOption(null);
    }

    /**
     * Configures which distribution options to use. Relevant are the frameworkURL, the
     * frameworkName and the Karaf version since all of those params are relevant to decide which
     * wrapper configurations to use.
     * 
     * @param frameworkURL
     *            frameworkURL
     * @param name
     *            framework name
     * @param karafVersion
     *            Karaf version
     * @return option
     */
    public static KarafDistributionBaseConfigurationOption karafDistributionConfiguration(
        String frameworkURL, String name, String karafVersion) {
        return new KarafDistributionConfigurationOption(frameworkURL, name, karafVersion);
    }

    /**
     * Configures which distribution options to use. Relevant are the frameworkURL, the
     * frameworkName and the Karaf version since all of those params are relevant to decide which
     * wrapper configurations to use.
     * 
     * @return option
     */
    public static KarafDistributionBaseConfigurationOption karafDistributionConfiguration() {
        return new KarafDistributionConfigurationOption();
    }

    /**
     * This option allows to configure each configuration file based on the karaf.home location. The
     * value is "put". Which means it is either replaced or added.
     * <p>
     * If you like to extend an option (e.g. make a=b to a=b,c) please make use of the
     * {@link KarafDistributionConfigurationFileExtendOption}.
     * 
     * @param configurationFilePath
     *            configuration file path
     * @param key
     *            property key
     * @param value
     *            property value
     * @return option
     */
    public static Option editConfigurationFilePut(String configurationFilePath, String key,
        String value) {
        return new KarafDistributionConfigurationFilePutOption(configurationFilePath, key, value);
    }

    /**
     * This option allows to configure each configuration file based on the karaf.home location. The
     * value is "put". Which means it is either replaced or added.
     * <p>
     * If you like to extend an option (e.g. make a=b to a=b,c) please make use of the
     * {@link KarafDistributionConfigurationFileExtendOption}.
     * 
     * @param configurationPointer
     *            reference to configuration file
     * @param value
     *            new value
     * @return option
     */
    public static Option editConfigurationFilePut(ConfigurationPointer configurationPointer,
        String value) {
        return new KarafDistributionConfigurationFilePutOption(configurationPointer, value);
    }

    /**
     * This option allows to configure each configuration file based on the karaf.home location. The
     * value is "put" which means it is either replaced or added. For simpler configuration you can
     * add a file source. If you want to put all values from this file do not configure any
     * keysToUseFromSource; otherwise define them to use only those specific values.
     * 
     * @param configurationFilePath
     *            configuration file path
     * @param source
     *            configuration source file
     * @param keysToUseFromSource
     *            configuration keys to be used
     * @return option array
     */
    public static Option[] editConfigurationFilePut(final String configurationFilePath,
        File source, String... keysToUseFromSource) {
        return createOptionListFromFile(source, new FileOptionFactory() {

            @Override
            public Option createOption(String key, String value) {
                return new KarafDistributionConfigurationFilePutOption(configurationFilePath, key,
                    value);
            }
        }, keysToUseFromSource);
    }

    private interface FileOptionFactory {

        Option createOption(String key, String value);
    }

    private static Option[] createOptionListFromFile(File source, FileOptionFactory optionFactory,
        String... keysToUseFromSource) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(source));
        }
        catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
        List<Option> options = new ArrayList<Option>();
        if (keysToUseFromSource == null || keysToUseFromSource.length == 0) {
            Set<Object> keySet = props.keySet();
            for (Object key : keySet) {
                Object value = props.get(key);
                options.add(optionFactory.createOption((String) key, (String) value));
            }
        }
        else {
            for (String key : keysToUseFromSource) {
                Object value = props.get(key);
                options.add(optionFactory.createOption(key, (String) value));
            }
        }
        return options.toArray(new Option[] {});
    }

    /**
     * This option allows to extend configurations in each configuration file based on the
     * karaf.home location. The value extends the current value (e.g. a=b to a=a,b) instead of
     * replacing it. If there is no current value it is added.
     * <p>
     * If you would like to have add or replace functionality please use the
     * {@link KarafDistributionConfigurationFilePutOption} instead.
     * 
     * @param configurationFilePath
     *            configuration file path
     * @param key
     *            property key
     * @param value
     *            property value
     * @return option
     */
    public static Option editConfigurationFileExtend(String configurationFilePath, String key,
        String value) {
        return new KarafDistributionConfigurationFileExtendOption(configurationFilePath, key, value);
    }

    /**
     * This option allows to extend configurations in each configuration file based on the
     * karaf.home location. The value extends the current value (e.g. a=b to a=a,b) instead of
     * replacing it. If there is no current value it is added.
     * <p>
     * If you would like to have add or replace functionality please use the
     * {@link KarafDistributionConfigurationFilePutOption} instead.
     * 
     * @param configurationPointer
     *            reference to configuration key
     * @param value
     *            additional configuration value
     * 
     * @return option
     */
    public static Option editConfigurationFileExtend(ConfigurationPointer configurationPointer,
        String value) {
        return new KarafDistributionConfigurationFileExtendOption(configurationPointer, value);
    }

    /**
     * This option allows to configure each configuration file based on the karaf.home location. The
     * value is "extend" which means it is either replaced or added. For simpler configuration you
     * can add a file source. If you want to put all values from this file do not configure any
     * keysToUseFromSource; otherwise define them to use only those specific values.
     * 
     * @param configurationFilePath
     *            configuration file path
     * @param source
     *            property key
     * @param keysToUseFromSource
     *            property keys to be used
     * @return option array
     */
    public static Option[] editConfigurationFileExtend(final String configurationFilePath,
        File source, String... keysToUseFromSource) {
        return createOptionListFromFile(source, new FileOptionFactory() {

            @Override
            public Option createOption(String key, String value) {
                return new KarafDistributionConfigurationFileExtendOption(configurationFilePath,
                    key, value);
            }
        }, keysToUseFromSource);
    }

    /**
     * This option allows to simply replace an entire configuration file with your own one. Simply
     * point to the configuration file you would like to have replaced and add the source file which
     * should replace it.
     * 
     * @param configurationFilePath
     *            path to configuration file
     * @param source
     *            configuration file replacement
     * 
     * @return option
     */
    public static Option replaceConfigurationFile(String configurationFilePath, File source) {
        return new KarafDistributionConfigurationFileReplacementOption(configurationFilePath,
            source);
    }

    /**
     * Activates debugging on the embedded Karaf container using the standard 5005 port and holds
     * the vm till you've attached the debugger.
     * 
     * @return option
     */
    public static Option debugConfiguration() {
        return debugConfiguration("5005", true);
    }

    /**
     * A very simple and convinient method to set a specific log level without the need of configure
     * the specific option itself.
     * 
     * @param logLevel
     *            log level
     * @return option
     */
    public static Option logLevel(LogLevel logLevel) {
        return new LogLevelOption(logLevel);
    }

    /**
     * A very simple and convinient method to set a specific log level without the need of configure
     * the specific option itself.
     * 
     * @return option
     */
    public static LogLevelOption logLevel() {
        return new LogLevelOption();
    }

    /**
     * Returns an easy option to activate and configure remote debugging for the Karaf container.
     * 
     * @param port
     *            remote debugger port
     * @param hold
     *            should the VM wait for the debugger?
     * @return option
     */
    public static Option debugConfiguration(String port, boolean hold) {
        return new VMOption(format("-Xrunjdwp:transport=dt_socket,server=y,suspend=%s,address=%s",
            hold ? "y" : "n", port));
    }

    /**
     * Creates a {@link KarafFeaturesOption}.
     * 
     * @param repositoryUrl
     *            url of features respository to be scanned
     * @param features
     *            features to be scanned
     * 
     * @return features option
     */
    public static KarafFeaturesOption features(final String repositoryUrl, final String... features) {
        return new KarafFeaturesOption(repositoryUrl, features);
    }

    /**
     * Creates a {@link KarafFeaturesOption}.
     * 
     * @param repositoryUrl
     *            url of features respository to be scanned
     * @param features
     *            features to be scanned
     * 
     * @return features option
     */
    public static KarafFeaturesOption features(final UrlReference repositoryUrl,
        final String... features) {
        return new KarafFeaturesOption(repositoryUrl, features);
    }
}
