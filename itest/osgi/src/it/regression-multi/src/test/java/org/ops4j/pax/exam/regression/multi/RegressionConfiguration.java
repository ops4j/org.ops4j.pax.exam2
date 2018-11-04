/*
 * Copyright 2011 Harald Wellmann.
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
package org.ops4j.pax.exam.regression.multi;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.Constants.START_LEVEL_TEST_BUNDLE;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.frameworkStartLevel;
import static org.ops4j.pax.exam.CoreOptions.repository;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

/**
 * Default configuration for native container regression tests, overriding the default test system
 * configuration.
 * <p>
 * We do not need the Remote Bundle Context for Native Container, and we prefer unified logging with
 * logback.
 * <p>
 * To override the standard options, you need to set the configuration property
 * {@code pax.exam.system = default}.
 * 
 * @author Harald Wellmann
 * @since Dec 2011
 */
public class RegressionConfiguration {

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
            
            repository("https://oss.sonatype.org/content/repositories/ops4j-snapshots").id("ops4j-snapshots").allowSnapshots(),

            frameworkStartLevel(START_LEVEL_TEST_BUNDLE));
    }

    public static boolean isNativeContainer() {
        return "native".equals(System.getProperty("pax.exam.container"));
    }

    public static boolean isEquinox() {
        return "equinox".equals(System.getProperty("pax.exam.framework"));
    }

    public static boolean isFelix() {
        return "felix".equals(System.getProperty("pax.exam.framework"));
    }

    public static boolean isKnopflerfish() {
        return "knopflerfish".equals(System.getProperty("pax.exam.framework"));
    }
}
