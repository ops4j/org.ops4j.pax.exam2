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

import org.kohsuke.MetaInfServices;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;

/**
 * @author Roland Hauser
 * @since 4.7.0
 */
@MetaInfServices
public class SisuTestContainerFactory implements TestContainerFactory {

	/* (non-Javadoc)
	 * @see org.ops4j.pax.exam.TestContainerFactory#create(org.ops4j.pax.exam.ExamSystem)
	 */
	@Override
	public TestContainer[] create(ExamSystem system) {
		return new TestContainer[] { new SisuTestContainer(system) };
	}

}
