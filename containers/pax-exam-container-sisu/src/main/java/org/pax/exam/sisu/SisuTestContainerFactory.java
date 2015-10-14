package org.pax.exam.sisu;

import org.kohsuke.MetaInfServices;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;

/**
 * @author rolandhauser
 *
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
