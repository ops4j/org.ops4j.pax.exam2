/*
 * Copyright 2013 Harald Wellmann
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

package org.ops4j.pax.exam.regression.multi.depend;

import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

/**
 * Regression test for PAXEXAM-474.
 * <p>
 * Checks that bundles are resolved even if they get installed in inverse dependency order.
 * In this test, gogo.command depends on gogo.runtime. Pax Exam 3.0.0 would start gogo.command
 * before installing gogo.runtime, which led to an exception.
 * 
 * @author Harald Wellmann
 */
@RunWith(PaxExam.class)
public class DependencyTest {

    @Configuration
    public Option[] config() {
        return options(
            regressionDefaults(), //
            frameworkProperty("osgi.console").value("6666"), //
            junitBundles(), //
            mavenBundle("org.apache.felix", "org.apache.felix.gogo.command", "0.10.0")
                .startLevel(1).start(true),
            mavenBundle("org.apache.felix", "org.apache.felix.gogo.shell", "0.10.0"),
            mavenBundle("org.apache.felix", "org.apache.felix.gogo.runtime", "0.10.0").start(false));
    }

    @Test
    public void bundlesShouldBeResolved() {
        // in Pax Exam 3.0.0, this test method would not even be executed
        assertTrue(true);
    }
}
