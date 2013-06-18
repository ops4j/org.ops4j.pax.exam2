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
package org.ops4j.pax.exam.regression.multi.inject;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;

import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;

public class InjectConfigurationFactory implements ConfigurationFactory {

    @Override
    public Option[] createConfiguration() {
        String rmiPort = System.getProperty("pax.exam.regression.rmi", "");

        return options(
            regressionDefaults(),
            url("reference:file:" + PathUtils.getBaseDir() + "/target/pax-exam-sample9-pde.jar"),
            systemProperty("osgi.console").value("6666"),
            when(!rmiPort.isEmpty()).useOptions(
                systemProperty("pax.exam.regression.rmi").value(rmiPort)), junitBundles());
    }
}
