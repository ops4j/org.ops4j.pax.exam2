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

import org.ops4j.pax.exam.Option;

/**
 * Abstract configuration file option. This one should not used directly but rather via
 * {@link KarafDistributionConfigurationFileExtendOption} or {@link KarafDistributionConfigurationFilePutOption}.
 */
public abstract class KarafDistributionConfigurationFileOption implements Option {

    private String configurationFilePath;
    private String key;
    private Object value;

    public KarafDistributionConfigurationFileOption(ConfigurationPointer pointer, Object value) {
        this(pointer.getConfigurationFilePath(), pointer.getKey(), value);
    }

    public KarafDistributionConfigurationFileOption(String configurationFilePath, String key, Object value) {
        this.configurationFilePath = configurationFilePath;
        this.key = key;
        this.value = value;
    }

    public KarafDistributionConfigurationFileOption(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    public String getConfigurationFilePath() {
        return configurationFilePath;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

}
