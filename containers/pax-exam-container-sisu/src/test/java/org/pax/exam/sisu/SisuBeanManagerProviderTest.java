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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.cdi.impl.CdiInjector;



/**
 * @author rolandhauser
 *
 */
public class SisuBeanManagerProviderTest {
	
	/**
	 *
	 */
	private static class ValidTest {
		@Inject
		private TestService service;
		
		/**
		 * @return
		 */
		public TestService getService() {
			return service;
		}
	}
	
	/**
	 *
	 */
	private static class StaticFieldTest {
		@Inject
		private static TestService service;
		
	}
	
	/**
	 *
	 */
	private static class FinalFieldTest {
		@Inject
		private final TestService service = new TestService();
		
	}
	
	private final CdiInjector injector = new CdiInjector();
	private final SisuTestContainer container = new SisuTestContainer(mock(ExamSystem.class));
	
	/**
	 * 
	 */
	@Before
	public void setup() {
		container.start();
	}
	
	/**
	 * 
	 */
	@After
	public void tearDown() {
		container.stop();
	}
	
	/**
	 * 
	 */
	@Test
	public void injectService() {
		final ValidTest test = new ValidTest();
		injector.injectFields(test);
		assertNotNull(test.getService());
	}
	
	/**
	 * 
	 */
	@Test(expected = TestContainerException.class)
	public void failWhenInjectFieldIsStatic() {
		injector.injectFields(new StaticFieldTest());
	}
	
	/**
	 * 
	 */
	@Test(expected = TestContainerException.class)
	public void failWhenInjectFieldIsFinal() {
		injector.injectFields(new FinalFieldTest());
	}
}
