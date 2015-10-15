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

import static org.pax.exam.sisu.ProxyFactory.createProxy;

import javax.enterprise.inject.spi.InjectionTarget;

import org.junit.Test;

/**
 *
 */
public class InjectionTargetProxyTest {
	@SuppressWarnings("rawtypes")
	private final InjectionTargetProxy handler = new InjectionTargetProxy<>();
	@SuppressWarnings("rawtypes")
	private final InjectionTarget target = createProxy(InjectionTarget.class, handler);

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test(expected = UnsupportedOperationException.class)
	public void invokeAnyOtherMethod() {
		target.dispose(null);
	}
}
