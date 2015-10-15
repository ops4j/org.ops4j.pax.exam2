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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.pax.exam.sisu.ProxyFactory.createProxy;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

import org.junit.Test;

/**
 *
 */
public class BeanManagerProxyTest {
	private final BeanManagerProxy handler = new BeanManagerProxy();
	private final BeanManager bman = createProxy(BeanManager.class, handler);

	/**
	 * 
	 */
	@Test
	public void invokeCreateAnnotatedType() {
		@SuppressWarnings("rawtypes")
		final AnnotatedType type = bman.createAnnotatedType(Object.class);
		assertNotNull(type);
	}
	
	/**
	 * 
	 */
	@Test
	public void invokeCreateInjectionTarget() {
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final InjectionTarget target = bman.createInjectionTarget(mock(AnnotatedType.class));
		assertNotNull(target);
	}
	
	/**
	 * 
	 */
	@Test
	public void invokeCreateCreationalContext() {
		// Should not throw an exception
		assertNull(bman.createCreationalContext(null));
	}
	
	/**
	 * 
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void invokeAnyOtherMethod() {
		bman.getBeans("");
	}
}
