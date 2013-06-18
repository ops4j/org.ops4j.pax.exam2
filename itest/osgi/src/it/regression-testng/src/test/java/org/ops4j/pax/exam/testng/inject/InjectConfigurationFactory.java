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

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;

import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

public class InjectConfigurationFactory implements ConfigurationFactory {

    @Override
    public Option[] createConfiguration() {
        return options(
            
            mavenBundle("org.testng", "testng", "6.8.5"),
            bundle("mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.hamcrest.core/1.3.0.1"),
            bundle("mvn:org.ops4j.pax.tipi/org.ops4j.pax.tipi.junit/4.11.0.1"),
            url("reference:file:" + PathUtils.getBaseDir() + "/target/pax-exam-sample9-pde/"),

            systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level").value("WARN"),
            systemProperty("osgi.console").value("6666"));
    }
}
