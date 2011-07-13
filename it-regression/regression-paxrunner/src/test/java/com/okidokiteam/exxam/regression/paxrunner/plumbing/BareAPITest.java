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
package com.okidokiteam.exxam.regression.paxrunner.plumbing;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.PaxExamRuntime;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.spi.PaxExamRuntime.*;

/**
 *
 */
public class BareAPITest {

	@Test
	public void bareRunTest() throws Exception {
		Option[] options = new Option[] {
				//felix(),
				felix(),
				junitBundles(),
				easyMockBundles(),
				systemProperty("org.ops4j.pax.logging.DefaultServiceLog.level")
						.value("DEBUG"),
				// mavenBundle().groupId( "org.ops4j.pax.logging" ).artifactId(
				// "pax-logging-service" ).version( "1.6.1" ),
				rawPaxRunnerOption("envo", "mike=blue,foo=bar"),
				//rawPaxRunnerOption( "keepOriginalUrls" ),
				//mavenBundle().groupId( "org.ops4j.pax.url").artifactId("pax-url-link" ).version( "1.3.2" ).startLevel( 0 )
				 
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

	// @Test
	public void singleStepTest() throws Exception {
		Option[] options = new Option[] {
				junitBundles(),
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
		TestProbeBuilder probe = system.createProbe(new Properties());
		probe.addTests(SingleTestProbe.class,
				getAllMethods(SingleTestProbe.class));
		return probe.build();
	}

	private Method[] getAllMethods(Class c) {
		List<Method> methods = new ArrayList<Method>();
		for (Method m : c.getDeclaredMethods()) {
			if (m.getModifiers() == Modifier.PUBLIC) {
				methods.add(m);
			}
		}
		return methods.toArray(new Method[methods.size()]);

	}
}
