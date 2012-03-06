/*
 * Copyright (C) 2010 Toni Menzel
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
package org.ops4j.pax.exam.regression.multi.plumbing;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.PaxExamRuntime;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;
import static org.ops4j.pax.exam.spi.PaxExamRuntime.*;

/**
 *
 */
public class BareAPITest {

	@Test
	public void bareRunTest() throws Exception {
		Option[] options = new Option[] {
                regressionDefaults(),
	            mavenBundle( "org.apache.servicemix.bundles" , "org.apache.servicemix.bundles.junit", "4.9_2" ),
				easyMockBundles(),
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("DEBUG"),
				rawPaxRunnerOption("envo", "mike=blue,foo=bar")
		};

		ExamSystem system = createTestSystem(options);
		TestProbeProvider p = makeProbe(system);

		// the parse will split all single containers
		for (TestContainer testContainer : getTestContainerFactory().create(
				system)) {
			try {
				testContainer.start();
				testContainer.install(p.getStream());
				for (TestAddress test : p.getTests()) {
					testContainer.call(test);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				testContainer.stop();
			}
		}
		system.clear();
	}

	@Test
	public void singleStepTest() throws Exception {
		Option[] options = new Option[] {
            mavenBundle( "org.apache.servicemix.bundles" , "org.apache.servicemix.bundles.junit", "4.9_2" ),
				easyMockBundles(),
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("DEBUG") };
		ExamSystem system = createTestSystem(options);
		TestProbeProvider p = makeProbe(system);

		TestContainer[] containers = PaxExamRuntime.getTestContainerFactory().create(system);

		for (TestContainer testContainer : containers) {
			testContainer.start();
		}
		try {
			for (TestContainer testContainer : containers) {
				testContainer.install(p.getStream());
			}

			for (TestContainer testContainer : containers) {
				for (TestAddress test : p.getTests()) {
					testContainer.call(test);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			for (TestContainer testContainer : containers) {
				testContainer.stop();
			}
		}
	}

	private TestProbeProvider makeProbe(ExamSystem system) throws IOException {
		TestProbeBuilder probe = system.createProbe();
		probe.addTests(SingleTestProbe.class,
				getAllMethods(SingleTestProbe.class));
		return probe.build();
	}

	private Method[] getAllMethods(Class<?> c) {
		List<Method> methods = new ArrayList<Method>();
		for (Method m : c.getDeclaredMethods()) {
			if (m.getModifiers() == Modifier.PUBLIC) {
				methods.add(m);
			}
		}
		return methods.toArray(new Method[methods.size()]);

	}
}
