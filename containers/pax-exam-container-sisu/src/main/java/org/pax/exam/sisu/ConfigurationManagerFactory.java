package org.pax.exam.sisu;

import org.ops4j.pax.exam.ConfigurationManager;

/**
 * @author rolandhauser
 *
 */
class ConfigurationManagerFactory {

	 /**
	 * @return
	 */
	ConfigurationManager createManager() {
		 return new ConfigurationManager();
	 }
}
