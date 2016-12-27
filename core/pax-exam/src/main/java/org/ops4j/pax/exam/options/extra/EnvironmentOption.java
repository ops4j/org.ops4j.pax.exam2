/*
 * Copyright 2016 Zoran Regvart
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
package org.ops4j.pax.exam.options.extra;

import org.ops4j.pax.exam.Option;

import static org.ops4j.lang.NullArgumentException.validateNotEmpty;

/**
 * Specify process environment.
 */
public final class EnvironmentOption implements Option {

    /**
     * Process environment option. Cannot be null or empty.
     */
    private final String environment;

    /**
     * Creates new {@link EnvironmentOption}.
     *
     * @param environment
     *            process environment option (cannot be null or empty)
     *
     * @throws IllegalArgumentException
     *             - If environment is null or empty
     */
    public EnvironmentOption(final String environment) {
        validateNotEmpty(environment, true, "Environment option");
        this.environment = environment;
    }

    public String getEnvironment() {
        return environment;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("EnvironmentOption");
        sb.append("{environment='").append(environment).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
