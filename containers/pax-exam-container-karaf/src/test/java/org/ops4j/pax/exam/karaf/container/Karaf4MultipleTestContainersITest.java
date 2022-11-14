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

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import java.io.File;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

@ExamReactorStrategy(PerClass.class)
public class Karaf4MultipleTestContainersITest extends Karaf4TestContainerITest {

    private final MavenArtifactUrlReference KARAF_URL = maven("org.apache.karaf", "apache-karaf").type("zip");

    @Configuration
    public Option[] config() {
        String karafVersion = karafVersion();
        return new Option[] {
            karafDistributionConfiguration()
                .frameworkUrl(KARAF_URL.version(karafVersion))
                .karafVersion(karafVersion)
                .useDeployFolder(false)
                .unpackDirectory(new File("target/paxexam/unpack/")),
            karafDistributionConfiguration()
                .frameworkUrl(KARAF_URL.version(karafVersion))
                .karafVersion(karafVersion)
                .useDeployFolder(false)
                .unpackDirectory(new File("target/paxexam/unpack/second/")),
            configureConsole().startLocalConsole().ignoreRemoteShell(),
            logLevel(LogLevelOption.LogLevel.INFO)
        };
    }
}
