package org.pax.exam.sisu;

import static org.pax.exam.sisu.ProxyFactory.createProxy;

import javax.enterprise.inject.spi.BeanManager;

import org.kohsuke.MetaInfServices;
import org.ops4j.pax.exam.cdi.spi.BeanManagerProvider;

/**
 * @author rolandhauser
 *
 */
@MetaInfServices
public class SisuBeanManagerProvider implements BeanManagerProvider{

	/* (non-Javadoc)
	 * @see org.ops4j.pax.exam.cdi.spi.BeanManagerProvider#getBeanManager()
	 */
	@Override
	public BeanManager getBeanManager() {
		return createProxy(BeanManager.class, new BeanManagerProxy());
	}

}
