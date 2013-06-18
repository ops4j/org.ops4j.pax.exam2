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
package org.ops4j.pax.exam.regression.multi.inject;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.sample9.pde.HelloService;
import org.ops4j.pax.exam.util.PathUtils;
import org.ops4j.pax.swissbox.tracker.ServiceLookup;
import org.osgi.framework.BundleContext;

@RunWith(PaxExam.class)
public class MultiConfigurationTest {

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] createConfiguration1() {
        return options(regressionDefaults(), url("reference:file:" + PathUtils.getBaseDir()
            + "/target/pax-exam-sample9-pde.jar"),
            systemProperty("pax.exam.language").value("la"), junitBundles());
    }

    @Configuration
    public Option[] createConfiguration2() {
        return options(regressionDefaults(), systemProperty("pax.exam.language").value("en"),
            url("reference:file:" + PathUtils.getBaseDir() + "/target/pax-exam-sample9-pde.jar"),
            junitBundles());
    }

    @Test
    public void getServiceFromInjectedBundleContext() {
        String language = System.getProperty("pax.exam.language");
        String expected = (language.equals("en")) ? "Hello Pax!" : "Pax Vobiscum!";
        assertThat(bundleContext, is(notNullValue()));
        Map<String, String> props = new HashMap<String, String>();
        props.put("language", language);
        HelloService service = ServiceLookup.getService(bundleContext, HelloService.class, props);
        assertThat(service, is(notNullValue()));
        assertThat(service.getMessage(), is(expected));
    }
}
