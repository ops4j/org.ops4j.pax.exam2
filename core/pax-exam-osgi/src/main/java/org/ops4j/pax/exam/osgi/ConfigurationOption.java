/*
 * Copyright 2013 Christoph Läubrich
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
package org.ops4j.pax.exam.osgi;

import java.util.Map;

import org.ops4j.pax.exam.Option;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * The {@link ConfigurationOption} allows to specify a Map of properties that are injected into the
 * container. You have the choice of either create (the default) or override an existing
 * configuration Configuration Options e.g. in ConfigAdmin
 */
public interface ConfigurationOption {

    /**
     * @return the current value of id
     */
    String getId();

    /**
     * Add the key/value pair to the overriden options. Only the following "primary property types"
     * are supported by the {@link ConfigurationAdmin}
     * <ul>
     * <li>primitive types
     * <li/>
     * <li>object variants of primitive types (like Integer, Boolean, and so on)</li>
     * <li>arrays or collections (must be serializable!) of all types</li>
     * </ul>
     * Also make sure not to contain case variants, because they are also not allowed and keep in
     * mind that some properties can't be overriden
     * 
     * @param key
     * @param value
     * @return <code>this</code> for chaining
     */
    ConfigurationOption put(String key, Object value);

    /**
     * Putt all properties from the given map to this configuration object, see
     * {@link #put(String, Object)} for more details
     * 
     * @param properties
     * @return <code>this</code> for chaining
     */
    ConfigurationOption putAll(Map<String, ?> properties);

    /**
     * @return the current value of overrideValues
     */
    Map<String, ?> getProperties();

    /**
     * Specify if the configuration should be created if it does not yet exits
     * 
     * @param create
     * @return <code>this</code> for chaining
     */
    ConfigurationOption create(boolean create);

    /**
     * Specify if the properties of an exiting configuration should be overriden/extended by the
     * ones given here, <b>this does not work with factories!</b>
     * 
     * @param override
     * @return <code>this</code> for chaining
     */
    ConfigurationOption override(boolean override);

    /**
     * Specify if the target of the properties are a factory configuration
     * 
     * @param factory
     * @return <code>this</code> for chaining
     */
    ConfigurationOption factory(boolean factory);

    /**
     * @return <code>true</code> if this configuration should be created if not exits,
     *         <code>false</code> otherwhise
     */
    boolean isCreate();

    /**
     * @return <code>true</code> if existing configurations should be extended/ovewritten
     *         <code>false</code> otherwhise
     */
    boolean isOverride();

    /**
     * @return <code>true</code> if a factory configuration should be created <code>false</code>
     *         otherwhise
     */
    boolean isFactory();

    /**
     * Creates an Option from the current settings that could be used in Configure methods
     * 
     * @return this configuration as an Option
     */
    Option asOption();
}
