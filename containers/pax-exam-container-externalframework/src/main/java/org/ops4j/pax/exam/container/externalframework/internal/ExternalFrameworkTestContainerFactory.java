/*
 * Copyright 2011.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.container.externalframework.internal;

import static org.ops4j.pax.exam.OptionUtils.filter;
import static org.ops4j.pax.exam.OptionUtils.remove;

import java.util.List;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.container.def.AbstractTestContainerFactory;
import org.ops4j.pax.exam.container.externalframework.options.ExternalFrameworkConfigurationOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for {@link ExternalFrameworkTestContainer}.
 * 
 */
public class ExternalFrameworkTestContainerFactory  extends AbstractTestContainerFactory {

	private static final Logger LOG = LoggerFactory
			.getLogger(ExternalFrameworkTestContainerFactory.class);

	
	@Override
	protected void createTestContainers(List<TestContainer> containers,
			Option... options) {
		ExternalFrameworkConfigurationOption<?>[] frameworks = getFrameworks(options);
		options = remove(ExternalFrameworkConfigurationOption.class, options);

		for (ExternalFrameworkConfigurationOption<?> framework : frameworks) {
			containers.add(new ExternalFrameworkTestContainer(
					 m_rmiRegistry.getHost(),
                     m_rmiRegistry.getPort(),
                     framework,
                     options));
		}
	}
	
	private ExternalFrameworkConfigurationOption<?>[] getFrameworks(Option[] options) {
		ExternalFrameworkConfigurationOption<?>[] frameworks = (filter(
				ExternalFrameworkConfigurationOption.class, options));
		if (frameworks.length == 0) {
			//TODO add default solution
			LOG.error("Cannot found an option to configure the external framework");
			throw new UnsupportedOperationException(
					"Cannot found any configuration of type ExternalConfigurationOption");
		}
		return frameworks;
	}

	

//	protected Option[] setDefaultOptions() {
//		return new Option[] {
//				// remote bundle context bundle
//
//				// rmi communication port
//
//				// ,
//				// boot delegation for sun.*. This seems only necessary in
//				// Knopflerfish version > 2.0.0
//				//bootDelegationPackage("sun.*"),
//
//				//url("link:classpath:META-INF/links/org.ops4j.pax.exam.rbc.link"),
//				//url("link:classpath:META-INF/links/org.ops4j.pax.extender.service.link"),
//				//url("link:classpath:META-INF/links/org.osgi.compendium.link"),
//
//				//url("link:classpath:META-INF/links/org.ops4j.pax.logging.api.link") 
//				
//				mavenBundle().groupId( "org.ops4j.pax.exam" ).artifactId( "pax-exam-container-rbc" ).version(
//                        Info.getPaxExamVersion() ).update(
//                                                           Info.isPaxExamSnapshotVersion() ).startLevel(
//                                                                                                         START_LEVEL_SYSTEM_BUNDLES ),
//				// rmi communication port
//				systemProperty( Constants.RMI_PORT_PROPERTY ).value( Integer.toString(m_rmiRegistry.getPort()) ),
//				// boot delegation for sun.*. This seems only necessary in Knopflerfish version > 2.0.0
//				bootDelegationPackage( "sun.*" ) 
//		};
//	}

}
