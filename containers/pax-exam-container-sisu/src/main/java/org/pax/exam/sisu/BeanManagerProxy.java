package org.pax.exam.sisu;

import static org.pax.exam.sisu.ProxyFactory.createProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;

/**
 * @author rolandhauser
 *
 */
public class BeanManagerProxy implements InvocationHandler {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if ("createAnnotatedType".equals(method.getName())) {
			return createProxy(AnnotatedType.class, new AnnotatedTypeProxy((Class<?>)args[0]));
		}
		if ("createInjectionTarget".equals(method.getName())) {
			return createProxy(InjectionTarget.class, new InjectionTargetProxy<>((AnnotatedType)args[0]));
		}
		
		return null;
	}
}
