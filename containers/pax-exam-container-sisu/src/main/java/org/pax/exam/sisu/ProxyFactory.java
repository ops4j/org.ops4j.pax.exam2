package org.pax.exam.sisu;

import static java.lang.reflect.Proxy.newProxyInstance;

import java.lang.reflect.InvocationHandler;

/**
 * @author rolandhauser
 *
 */
public class ProxyFactory {

	@SuppressWarnings("unchecked")
	public static <T> T createProxy(Class<T> type, InvocationHandler handler) {
		return (T) newProxyInstance(ProxyFactory.class.getClassLoader(), new Class<?>[] { type }, handler);
	}
}
