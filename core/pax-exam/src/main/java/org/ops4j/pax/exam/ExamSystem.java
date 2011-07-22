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
import java.util.Properties;

/**
 * An instance that drives cross cutting concerns when using Pax Exam.
 * - Provides System Resource Locations,
 * - Cross Cutting default properties,
 * - access to API entry (building probes, getting TestContainerFactory etc.)
 *
 * @author Toni Menzel ( toni@okidokiteam.com )
 */
public interface ExamSystem {

    /**
     * A shortcut to {@link ExamSystem#getOptions(Class)} when expecting a single value.
     *
     * @param optionType type of option to be retrieved.
     *
     * @return option matching the parameter type T or null if no option was found. Implementations may rule of their own how to react when multiple values are found (check their javadoc).
     */
    public <T extends Option> T getSingleOption( final Class<T> optionType );

    /** 
     * Getting options of this "system". This is the prime method on accessing options of a giving system.
     *
     * @param optionType type of option to be retrieved.
     * @return options matching the parameter type T. If none was found, this method returns an empty array.
     */
    public <T extends Option> T[] getOptions( final Class<T> optionType );

    /**
     *
     * @param options options to be used additionally in this fork
     * @return a forked {@link ExamSystem} instance (new instance). The parent ExamSysten (the one you called fork on) will notice and take care to bring down forked instances on cleanup (for example).
     *
     * @throws IOException in case creation of the new {@link ExamSystem} fails. (IO related).
     */
    public ExamSystem fork( Option[] options )
        throws IOException;

    /**
     * @return the basic directory that Exam should use to look at user-defaults.
     */
    public File getConfigFolder();

    /**
     * Each call to this method might create a new folder that is being cleared on clear().
     *
     * @return the basic directory that Exam should use for all IO write activities.
     */
    public File getTempFolder();

    /**
     * @return a relative indication of how to deal with timeouts.
     */
    public RelativeTimeout getTimeout();

    /**
     * New Probe creator using this systems "caches".
     * @param p initial properties
     * @return a new {@link TestProbeBuilder} that can be used to create the actual probe.
     * @throws IOException in case of an IO problem.
     */
    public TestProbeBuilder createProbe( Properties p )
        throws IOException;

    /**
     * Exam relies on uniquely used identifiers per system. This method gives you one UUID.
     *
     * @param purposeText Human readable (short) text that can be used to backtrack the created identifier (for debugging and logging purposes)
     * @return a new and unqiue identifier.
     */
    public String createID( String purposeText );

    /**
     * Clears up resources taken by system (like temporary files)
     */
    public void clear();
}
