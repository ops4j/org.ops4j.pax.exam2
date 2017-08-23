/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam;

import java.io.File;
import java.io.IOException;

/**
 * An instance that drives cross cutting concerns when using Pax Exam. - Provides System Resource
 * Locations, - Cross Cutting default properties, - access to API entry (building probes, getting
 * TestContainerFactory etc.)
 * 
 * @author Toni Menzel ( toni@okidokiteam.com )
 * @author Christoph Läubrich
 */
public interface ExamSystem {

    /**
     * A shortcut to {@link ExamSystem#getOptions(Class)} when expecting a single value.
     * 
     * @param optionType
     *            type of option to be retrieved.
     * @param <T>
     *            option type
     * @return option matching the parameter type T or null if no option was found. Implementations
     *         may rule of their own how to react when multiple values are found (check their
     *         javadoc).
     *         @deprecated use the more well-defined methods {@link #getRequiredOption(Class)} and {@link #getOption(Class)}
     */
    @Deprecated
    <T extends Option> T getSingleOption(final Class<T> optionType);
    
    /**
     * Getter for expecting a single or no value to be present in the {@link ExamSystem}.
     * 
     * @param optionType
     *            type of option to be retrieved.
     * @param <T>
     *            option type
     * @return option matching the parameter type T or null if not found
     * @throws ExamConfigurationException thrown if more than one Option of this type are specified
     */
    default <T extends Option> T getOption(final Class<T> optionType) throws ExamConfigurationException {
        T[] options = getOptions(optionType);
        if (options.length == 0) {
            return null;
        }
        else if (options.length == 1) {
            return options[0];
        }
        else {
            throw new ExamConfigurationException("Option of type " + optionType.getName() + " can only be specifed once!");
        }
    }
    
    /**
     * Getter for expecting a single value to be present in the {@link ExamSystem}.
     * 
     * @param optionType
     *            type of option to be retrieved.
     * @param <T>
     *            option type
     * @return option matching the parameter type T
     * @throws ExamConfigurationException thrown if the Option can't be found, or more than one Option of this type are specified
     */
    default <T extends Option> T getRequiredOption(final Class<T> optionType) throws ExamConfigurationException {
        T[] options = getOptions(optionType);
        if (options.length == 0) {
            throw new ExamConfigurationException("Can't find required Option of type " + optionType.getName());
        }
        else if (options.length == 1) {
            return options[0];
        }
        else {
            throw new ExamConfigurationException("Option of type " + optionType.getName() + " can only be specifed once!");
        }
    }
    
    /**
     * Getting at least one option of the desired type
     * 
     * @param optionType
     *            type of option to be retrieved.
     * @param <T>
     *            option type
     * @return options matching the parameter type T. 
     * @throws ExamConfigurationException thrown if the Option can't be found
     */
    default <T extends Option> T[] getRequiredOptions(final Class<T> optionType) throws ExamConfigurationException {
        T[] options = getOptions(optionType);
        if (options.length == 0) {
            throw new ExamConfigurationException("Can't find required Option of type " + optionType.getName());
        }
        else {
            return options;
        }
    }

    /**
     * Getting options of this "system". This is the prime method on accessing options of a giving
     * system.
     * 
     * @param optionType
     *            type of option to be retrieved.
     * @param <T>
     *            option type
     * @return options matching the parameter type T. If none was found, this method returns an
     *         empty array.
     */
    <T extends Option> T[] getOptions(final Class<T> optionType);

    /**
     * 
     * @param options
     *            options to be used additionally in this fork
     * @return a forked {@link ExamSystem} instance (new instance). The parent ExamSysten (the one
     *         you called fork on) will notice and take care to bring down forked instances on
     *         cleanup (for example).
     */
    ExamSystem fork(Option[] options);

    /**
     * @return the basic directory that Exam should use to look at user-defaults.
     */
    File getConfigFolder();

    /**
     * Each call to this method might create a new folder that is being cleared on clear().
     * 
     * @return the basic directory that Exam should use for all IO write activities.
     */
    File getTempFolder();

    /**
     * @return a relative indication of how to deal with timeouts.
     */
    RelativeTimeout getTimeout();

    /**
     * New Probe creator using this systems "caches".
     * 
     * @return a new {@link TestProbeBuilder} that can be used to create the actual probe.
     * @throws IOException
     *             in case of an IO problem.
     */
    TestProbeBuilder createProbe() throws IOException;

    /**
     * Exam relies on uniquely used identifiers per system. This method gives you one UUID.
     * 
     * @param purposeText
     *            Human readable (short) text that can be used to backtrack the created identifier
     *            (for debugging and logging purposes)
     * @return a new and unqiue identifier.
     */
    String createID(String purposeText);

    /**
     * Clears up resources taken by system (like temporary files)
     */
    void clear();
}
