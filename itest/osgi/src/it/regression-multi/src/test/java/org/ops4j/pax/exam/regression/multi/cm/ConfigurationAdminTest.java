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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;

import java.io.IOException;
import java.util.Dictionary;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.osgi.service.cm.ConfigurationAdmin;

@RunWith(PaxExam.class)
@ExamReactorStrategy
public class ConfigurationAdminTest {

    private static final String EQUINOX_MIRROR = "http://archive.eclipse.org/equinox/drops/R-3.7-201106131736/";
    private static final String DEBUG_OPTIONS = "0.org.eclipse.osgi.framework.debug.FrameworkDebugOptions";

    @Inject
    private ConfigurationAdmin configAdmin;

    @Configuration()
    public Option[] config() {
        return options(regressionDefaults(), bundle(EQUINOX_MIRROR
            + "org.eclipse.equinox.cm_1.0.300.v20110502.jar"), bundle(EQUINOX_MIRROR
            + "org.eclipse.osgi.services_3.3.0.v20110513.jar"), junitBundles());
    }

    @Test
    public void testConfigAdmin() throws IOException {
        assertThat(configAdmin, is(notNullValue()));
        org.osgi.service.cm.Configuration config = configAdmin.getConfiguration(DEBUG_OPTIONS);
        assertThat(config, is(notNullValue()));
        config.update();

        @SuppressWarnings("unchecked")
        Dictionary<String, String> dictionary = config.getProperties();

        assertThat(dictionary, is(notNullValue()));
        assertThat(dictionary.get("service.pid"), is(DEBUG_OPTIONS));
    }
}
