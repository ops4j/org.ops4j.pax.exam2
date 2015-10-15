/*
 * Copyright 2015 Roland Hauser
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.pax.exam.sisu;

import static java.lang.System.getProperties;
import static java.lang.System.setProperty;
import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.ops4j.pax.exam.CoreOptions.jarProbe;
import static org.pax.exam.sisu.SisuTestContainer.CONTAINER_NAME;
import static org.pax.exam.sisu.SisuTestContainer.NOOP;
import static org.pax.exam.sisu.SisuTestContainer.getInjector;

import java.io.File;
import java.net.URLClassLoader;

import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.options.JarProbeOption;

/**
 *
 */
public class SisuContainerTest {
	private final ExamSystem system = mock(ExamSystem.class);
	private final SisuTestContainer container = new SisuTestContainer(system);

	/**
	 * 
	 */
	@Before
	public void setup() {
		when(system.getTempFolder()).thenReturn(new File("target"));
	}

	/**
	 * 
	 */
	@Test
	public void call() {
		container.call(null);
		verifyZeroInteractions(system);
	}

	/**
	 * 
	 */
	@Test
	public void installWithLocation() {
		assertEquals(NOOP, container.install(null, null));
		verifyZeroInteractions(system);
	}

	/**
	 * 
	 */
	@Test
	public void install() {
		assertEquals(NOOP, container.install(null));
		verifyZeroInteractions(system);
	}

	/**
	 * 
	 */
	@Test
	public void installProbe() {
		assertEquals(NOOP, container.installProbe(null));
		verifyZeroInteractions(system);
	}

	/**
	 * 
	 */
	@Test
	public void uninstallProbe() {
		container.uninstallProbe();
		verifyZeroInteractions(system);
	}

	/**
	 * 
	 */
	@Test(expected = TestContainerException.class)
	public void startExamSystemIsNotCdi() {
		setProperty(Constants.EXAM_CONFIGURATION_KEY,
				new File("src/test/resources/systemIsNotCdi.properties").toURI().toString());
		try {
			container.start();
		} finally {
			getProperties().remove(Constants.EXAM_CONFIGURATION_KEY);
		}
	}

	/**
	 * 
	 */
	@Test
	public void startStopWithCurrentContextClassLoader() {
		assertSame(container, container.start());
		assertNotNull(getInjector());
		assertSame(container, container.stop());
		assertNull(getInjector());
	}

	/**
	 * 
	 */
	@Test
	public void startWithSystemClassLoader() {
		currentThread().setContextClassLoader(null);
		assertSame(container, container.start());
		assertNotNull(getInjector());
		assertNull(currentThread().getContextClassLoader());
		assertSame(container, container.stop());
		assertNull(getInjector());
		assertNull(currentThread().getContextClassLoader());
	}

	/**
	 * 
	 */
	@Test
	public void startStopWithPropeOption() {
		ClassLoader ctxLdr = mock(ClassLoader.class);
		currentThread().setContextClassLoader(ctxLdr);
		JarProbeOption option = jarProbe().resources("exam.properties");
		when(system.getSingleOption(JarProbeOption.class)).thenReturn(option);
		assertSame(container, container.start());
		final ClassLoader ldr = currentThread().getContextClassLoader();
		assertTrue(ldr instanceof URLClassLoader);
		assertSame(container, container.stop());
		assertNull(getInjector());
		assertSame(ctxLdr, currentThread().getContextClassLoader());
	}

	/**
	 * 
	 */
	@Test
	public void stopWhenNotStarted() {
		assertSame(container, container.stop());
	}

	/**
	 * 
	 */
	@Test
	public void verifyToString() {
		assertEquals(CONTAINER_NAME, container.toString());
	}
}
