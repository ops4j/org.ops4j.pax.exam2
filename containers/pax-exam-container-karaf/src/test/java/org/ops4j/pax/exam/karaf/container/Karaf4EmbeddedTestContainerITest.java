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

package org.ops4j.pax.exam.karaf.container;

import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;

import java.util.stream.Stream;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.KarafDistributionBaseConfigurationOption;

public class Karaf4EmbeddedTestContainerITest extends AbstractKarafTestContainerITest {

    @Override
    protected String getDefaultKarafVersion() {
        return "4.1.1";
    }

    @Override
    protected KarafDistributionBaseConfigurationOption distribution() {
        return super.distribution().runEmbedded(true);
    }

    @Override
    protected Option[] doConfig() {
        return Stream.concat(Stream.of(super.config()), Stream.of(keepRuntimeFolder())).toArray(Option[]::new);
    }
}
