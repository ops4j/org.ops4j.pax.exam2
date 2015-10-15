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

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static org.pax.exam.sisu.SisuTestContainer.getInjector;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.LinkedList;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Inject;

import org.ops4j.pax.exam.TestContainerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Roland Hauser
 * @since 4.7.0
 */
public class InjectionTargetProxy<X> implements InvocationHandler {
	private final AnnotatedType<X> annotatedType;

	public InjectionTargetProxy(final AnnotatedType<X> annotatedType) {
		this.annotatedType = annotatedType;
	}

	/**
	 * 
	 */
	private Collection<Field> findFields(Class<?> type, Collection<Field> fields) {
		if (type != null) {
			for (final Field field : type.getDeclaredFields()) {
				if (field.isAnnotationPresent(Inject.class)) {
					if (isStatic(field.getModifiers())) {
						throw new TestContainerException("Cannot inject into static field "+field.getName());
					}

					if (isFinal(field.getModifiers())) {
						throw new TestContainerException("Cannot inject into final field "+ field.getName());
					}
					fields.add(field);
				}
			}
			findFields(type.getSuperclass(), fields);
		}
		return fields;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("inject".equals(method.getName())) {
			for (final Field field : findFields(annotatedType.getJavaClass(), new LinkedList<Field>())) {
				final Object obj = getInjector().getInstance(field.getType());
				field.setAccessible(true);
				field.set(args[0], obj);
			}
			return null;
		}
		throw new UnsupportedOperationException("Sisu InjectionTarget proxy only supports inject()");
	}

}
