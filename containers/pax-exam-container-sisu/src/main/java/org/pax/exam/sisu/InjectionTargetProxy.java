package org.pax.exam.sisu;

import static java.lang.reflect.Modifier.isStatic;
import static org.pax.exam.sisu.SisuTestContainer.getInjector;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.inject.Inject;

/**
 * @author rolandhauser
 *
 * @param <X>
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
				if (!isStatic(field.getModifiers()) && field.isAnnotationPresent(Inject.class)) {
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
				final Object obj =  getInjector().getInstance(field.getType());
				field.setAccessible(true);
				field.set(args[0], obj);
			}
		}
		return null;
	}

}
