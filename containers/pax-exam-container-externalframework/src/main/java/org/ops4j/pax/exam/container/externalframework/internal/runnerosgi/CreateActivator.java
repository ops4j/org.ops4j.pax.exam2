package org.ops4j.pax.exam.container.externalframework.internal.runnerosgi;

import org.osgi.framework.BundleContext;

public interface CreateActivator {

	/**
	 * Activator factory method.
	 *
	 * @param bundleName     name of the bundle to be created
	 * @param activatorClazz class name of the activator
	 * @param context        the running context
	 *
	 * @return activator related bundle context
	 */
	public abstract BundleContext createActivator(final String bundleName,
			final String activatorClazz);

}