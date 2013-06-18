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
package org.ops4j.pax.exam.karaf.container.internal;

import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.karaf.container.internal.runner.KarafJavaRunner;
import org.ops4j.pax.exam.karaf.container.internal.runner.NixRunner;
import org.ops4j.pax.exam.karaf.container.internal.runner.WindowsRunner;
import org.ops4j.pax.exam.karaf.options.KarafDistributionBaseConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionKitConfigurationOption;
import org.ops4j.pax.exam.karaf.options.KarafDistributionKitConfigurationOption.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KarafTestContainerFactory implements TestContainerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(KarafTestContainer.class);
    private static final int DEFAULTPORT = 21412;
    private static final boolean IS_WINDOWS_OS = System.getProperty("os.name").toLowerCase().contains("windows");

    private RMIRegistry rmiRegistry;

    public KarafTestContainerFactory() {
        rmiRegistry = new RMIRegistry(DEFAULTPORT, DEFAULTPORT + 1, DEFAULTPORT + 99).selectGracefully();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestContainer[] create(ExamSystem system) {
        List<TestContainer> containers = new ArrayList<TestContainer>();
        KarafDistributionKitConfigurationOption[] kitOptions =
                system.getOptions(KarafDistributionKitConfigurationOption.class);
        for (KarafDistributionKitConfigurationOption kitOption : kitOptions) {
            if (kitOption.getPlatform().equals(Platform.WINDOWS)) {
                if (IS_WINDOWS_OS) {
                    containers.add(new KarafTestContainer(system, rmiRegistry, kitOption, new WindowsRunner(kitOption
                            .getMakeExec(), kitOption.getExec())));
                    continue;
                }
                LOGGER.info("Ignore windows settings on non windows platforms");
            } 
            else {
                if (!IS_WINDOWS_OS) {
                    containers.add(new KarafTestContainer(system, rmiRegistry, kitOption, new NixRunner(kitOption
                            .getMakeExec(), kitOption.getExec())));
                    continue;
                }
                LOGGER.info("Ignore non windows settings on windows platforms");
            }
        }
        KarafDistributionBaseConfigurationOption[] options =
                system.getOptions(KarafDistributionConfigurationOption.class);
        for (KarafDistributionBaseConfigurationOption testContainer : options) {
            containers.add(new KarafTestContainer(system, rmiRegistry, testContainer, new KarafJavaRunner()));
        }
        return containers.toArray(new TestContainer[containers.size()]);
    }

}
