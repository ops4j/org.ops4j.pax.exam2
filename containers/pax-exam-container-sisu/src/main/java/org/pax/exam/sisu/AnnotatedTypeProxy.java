package org.pax.exam.sisu;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AnnotatedTypeProxy implements InvocationHandler {
	private final Class<?> targetType;

	public AnnotatedTypeProxy(final Class<?> targetType) {
		this.targetType = targetType;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("getJavaClass".equals(method.getName())) {
			
			
			return targetType;
		}
		return null;
	}

}
