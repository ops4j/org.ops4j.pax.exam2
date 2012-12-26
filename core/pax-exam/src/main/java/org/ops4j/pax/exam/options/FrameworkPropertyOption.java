/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.ops4j.pax.exam.options;

import static org.ops4j.lang.NullArgumentException.validateNotNull;

/**
 * Option specifying a framework property.
 * 
 * @author Harald Wellmann
 * @since 2.3.0, August 2011
 */
public class FrameworkPropertyOption implements ValueOption<Object> {

    /**
     * System property key (cannot be null or empty).
     */
    private final String key;
    /**
     * System property value (can be null or empty).
     */
    private Object value;

    /**
     * Constructor.
     * 
     * @param key
     *            system property key (cannot be null or empty)
     * 
     * @throws IllegalArgumentException
     *             - If key is null or empty
     */
    public FrameworkPropertyOption(final String key) {
        this.key = key;
        value = "";
    }

    /**
     * Sets the system property value.
     * 
     * @param _value
     *            system property value (cannot be null, can be empty)
     * 
     * @return itself, for fluent api usage
     * 
     * @throws IllegalArgumentException
     *             - If value is null
     */
    public FrameworkPropertyOption value(final Object _value) {
        validateNotNull(_value, "Value");
        this.value = _value;
        return this;
    }

    /**
     * Getter.
     * 
     * @return system property key (cannot be null or empty)
     */
    public String getKey() {
        return key;
    }

    /**
     * Getter.
     * 
     * @return system property value (cannot be null, can be empty)
     */
    public Object getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FrameworkPropertyOption");
        sb.append("{key='").append(key).append('\'');
        sb.append(", value='").append(value).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
