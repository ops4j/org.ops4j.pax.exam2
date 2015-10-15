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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * @author Roland Hauser
 * @since 4.7.0
 */
public class BeanManagerProxy implements InvocationHandler {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("createAnnotatedType".equals(method.getName())) {
			return createProxy(AnnotatedType.class, new AnnotatedTypeProxy((Class<?>) args[0]));
		} else if ("createInjectionTarget".equals(method.getName())) {
			return createProxy(InjectionTarget.class, new InjectionTargetProxy<>((AnnotatedType) args[0]));
		} else if ("createCreationalContext".equals(method.getName())) {
			return null;
		}

		throw new UnsupportedOperationException(
				"Sisu BeanManager proxy only supports createAnnotatedType(), createInjectionTarget() and createCreationalContext()");
	}
}
