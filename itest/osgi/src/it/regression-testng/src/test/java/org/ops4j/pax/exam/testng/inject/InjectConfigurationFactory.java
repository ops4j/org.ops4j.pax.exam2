/*
 * Copyright 2012 Harald Wellmann.
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
package org.ops4j.pax.exam.testng.inject;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.propagateSystemProperty;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;

import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

public class InjectConfigurationFactory implements ConfigurationFactory {

    @Override
    public Option[] createConfiguration() {
        return options(
            regressionDefaults(),
            propagateSystemProperty("pax.exam.regression.rmi"),
            mavenBundle("org.testng", "testng", "6.9.10"),
            mavenBundle("com.beust", "jcommander", "1.48"),
            mavenBundle("org.ops4j.pax.exam", "pax-exam-invoker-testng", Info.getPaxExamVersion()),
            url("reference:file:" + PathUtils.getBaseDir() + "/target/pax-exam-sample9-pde/"),

            systemProperty("pax.exam.invoker").value("testng"),
            systemProperty("osgi.console").value("6666"));
    }

    public static Option regressionDefaults() {
        return composite(

            cleanCaches(),

            // we're running with pax.exam.logging = none, so add SLF4J and logback bundles
            bundle("link:classpath:META-INF/links/org.slf4j.api.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
            bundle("link:classpath:META-INF/links/ch.qos.logback.classic.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),
            bundle("link:classpath:META-INF/links/ch.qos.logback.core.link").startLevel(START_LEVEL_SYSTEM_BUNDLES),

            // Set logback configuration via system property.
            // This way, both the driver and the container use the same configuration
            systemProperty("logback.configurationFile").value(
                "file:" + PathUtils.getBaseDir() + "/src/test/resources/logback.xml"),

            frameworkStartLevel(START_LEVEL_TEST_BUNDLE));
    }
}
