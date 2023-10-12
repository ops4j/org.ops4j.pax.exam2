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
package org.ops4j.pax.exam.cm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.cm.internal.ConfigurationOptionActivator;
import org.ops4j.pax.exam.cm.internal.ConfigurationOptionConfigurationListener;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.tinybundles.TinyBundle;
import org.ops4j.pax.tinybundles.TinyBundles;
import org.osgi.framework.Constants;

/**
 * Implementation of the {@link ConfigurationOption} interface used internally when construction
 * such options
 */
public class ConfigurationProvisionOption implements org.ops4j.pax.exam.cm.ConfigurationOption {

    private final String id;
    private final Map<String, Object> properties;

    private boolean create = true;
    private boolean override;
    private boolean factory;

    /**
     * Creates a new {@link ConfigurationOption} for the given id and the given property values
     * 
     * @param id configuration PID
     * @param properties configuration properties
     */
    public ConfigurationProvisionOption(String id, Map<String, Object> properties) {
        this.id = id;
        this.properties = properties;
    }

    /**
     * @return the current value of id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Add the key/value pair to the overriden options
     * 
     * @param key property key
     * @param value property value
     * @return <code>this</code> for chaining
     */
    @Override
    public ConfigurationOption put(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    @Override
    public ConfigurationOption putAll(Map<String, ?> _properties) {
        this.properties.putAll(_properties);
        return this;
    }

    /**
     * @return the current value of overrideValues
     */
    @Override
    public Map<String, ?> getProperties() {
        return properties;
    }

    /**
     * Specify if the configuration should be created if it does not yet exits
     * 
     * @param _create should this option be created?
     * @return <code>this</code> for chaining
     */
    @Override
    public ConfigurationOption create(boolean _create) {
        this.create = _create;
        return this;
    }

    /**
     * Specify if the properties of an existing configuration should be overriden/extended by the
     * ones given here, <b>this does not work with factories!</b>
     * 
     * @param _override should existing properties be overridden
     * @return <code>this</code> for chaining
     */
    @Override
    public ConfigurationOption override(boolean _override) {
        this.override = _override;
        return this;
    }

    /**
     * Specify if the target of the properties are a factory configuration
     * 
     * @param _factory is this a factory configuration?
     * @return <code>this</code> for chaining
     */
    @Override
    public ConfigurationOption factory(boolean _factory) {
        this.factory = _factory;
        return this;
    }

    /**
     * @return <code>true</code> if this configuration should be created if not exits,
     *         <code>false</code> otherwhise
     */
    @Override
    public boolean isCreate() {
        return create;
    }

    /**
     * @return <code>true</code> if existing configurations should be extended/ovewritten
     *         <code>false</code> otherwhise
     */
    @Override
    public boolean isOverride() {
        return override;
    }

    /**
     * @return <code>true</code> if a factory configuration should be created <code>false</code>
     *         otherwhise
     */
    @Override
    public boolean isFactory() {
        return factory;
    }

    @Override
    public ProvisionOption<?> asOption() {
        return createProvisionOption(this);
    }

    private static ProvisionOption<?> createProvisionOption(ConfigurationOption configOption) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream os;
        try {
            os = new ObjectOutputStream(outputStream);
        }
        catch (IOException e) {
            throw new TestContainerException("can't write stream headers", e);
        }
        String id = configOption.getId();
        if (id == null) {
            throw new TestContainerException("ConfigurationOption id can't be null");
        }
        try {
            os.writeObject(id);
            os.writeBoolean(configOption.isCreate());
            os.writeBoolean(configOption.isOverride());
            os.writeBoolean(configOption.isFactory());
            Map<String, ?> properties = configOption.getProperties();
            if (properties == null) {
                throw new TestContainerException("ConfigurationOption properties can't be null");
            }
            try {
                os.writeObject(new HashMap<String, Object>(properties));
            }
            catch (NotSerializableException e) {
                throw new TestContainerException(
                    "One of the values of the ConfigurationOption properties are not serializable",
                    e);
            }
            os.flush();
            os.close();
        }
        catch (IOException e) {
            throw new TestContainerException("Writing object data failed", e);
        }
        ByteArrayInputStream stream = new ByteArrayInputStream(outputStream.toByteArray());
        TinyBundle bundle = TinyBundles.bundle();
        bundle.addClass(ConfigurationOptionConfigurationListener.class);
        bundle.addClass(ConfigurationOptionActivator.class).addResource("override.obj", stream);
        bundle
            .setHeader(Constants.BUNDLE_SYMBOLICNAME, "PAXExamConfigurationOption-" + UUID.randomUUID());
        bundle.setHeader(Constants.IMPORT_PACKAGE,
            "org.osgi.framework,org.osgi.service.cm,org.osgi.util.tracker,org.slf4j");
        bundle.setHeader(Constants.BUNDLE_ACTIVATOR, ConfigurationOptionActivator.class.getName());
        bundle.setHeader(Constants.BUNDLE_MANIFESTVERSION, "2");
        return CoreOptions.streamBundle(bundle.build()).startLevel(1).start(true).update(false);
    }

}
