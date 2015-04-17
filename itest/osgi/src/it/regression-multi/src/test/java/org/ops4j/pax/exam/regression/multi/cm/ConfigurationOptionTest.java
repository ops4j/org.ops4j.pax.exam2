/*
 * Copyright (C) 2011 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.regression.multi.cm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;

import java.io.IOException;
import java.util.Dictionary;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.cm.ConfigurationAdminOptions;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationAdmin;

@RunWith(PaxExam.class)
@ExamReactorStrategy
public class ConfigurationOptionTest {

    private static final String TEST_KEY = "test.key";
    private static final String TEST_PROPERTY = "test.property";

    private static final String TEST_PID = "org.ops4j.pax.exam.regression.multi.cm.ConfigurationOptionTest";
    private static final String TEST_FACTORY_PID = TEST_PID + ".factory";

    @Inject
    private ConfigurationAdmin configAdmin;

    @Configuration()
    public Option[] config() {
        return options(
            regressionDefaults(),
            mavenBundle("org.apache.felix", "org.apache.felix.configadmin").versionAsInProject(),
            junitBundles(),
            // Prepare a new configuration...
            ConfigurationAdminOptions.newConfiguration(TEST_PID)
                .put(TEST_KEY, TEST_PROPERTY)
                .asOption(),
            // Prepare a new factory-configuration...
            ConfigurationAdminOptions.factoryConfiguration(TEST_FACTORY_PID)
                .put(TEST_KEY, TEST_PROPERTY)
                .asOption()
        );
    }

    @Test
    public void testNewConfiguration() throws IOException {
        org.osgi.service.cm.Configuration config = configAdmin.getConfiguration(TEST_PID);
        assertNotNull(config);
        Dictionary<String, ?> dictionary = config.getProperties();
        assertNotNull(dictionary);
        assertEquals(dictionary.get(TEST_KEY), TEST_PROPERTY);
    }

    @Test
    public void testNewFactoryConfiguration() throws IOException, InvalidSyntaxException {
        org.osgi.service.cm.Configuration[] configurations = configAdmin
            .listConfigurations("(service.factoryPid=" + TEST_FACTORY_PID + ")");
        assertNotNull(configurations);
        assertTrue("one configuration expected, but " + configurations.length + " found",
            configurations.length == 1);
        org.osgi.service.cm.Configuration config = configurations[0];
        Dictionary<String, ?> dictionary = config.getProperties();
        assertNotNull(dictionary);
        assertEquals(dictionary.get(TEST_KEY), TEST_PROPERTY);
    }
}
