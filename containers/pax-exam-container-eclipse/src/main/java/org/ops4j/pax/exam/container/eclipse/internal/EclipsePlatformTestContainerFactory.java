/*
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
package org.ops4j.pax.exam.container.eclipse.internal;

import org.kohsuke.MetaInfServices;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.container.eclipse.EclipsePlatformOption;

/**
 * Creates the {@link EclipsePlatformTestContainer}
 * 
 * @author Christoph Läubrich
 * @since Jun 2017
 * 
 */
@MetaInfServices
public class EclipsePlatformTestContainerFactory implements TestContainerFactory {

    public TestContainer create(final ExamSystem system) {
        EclipsePlatformOption[] options = system.getOptions(EclipsePlatformOption.class);
        if (options.length == 0) {
            throw new TestContainerException(
                "You must specify a EclipsePlatformOption to run this Container!");
        }
        else if (options.length > 1) {
            throw new TestContainerException("Only one EclipsePlatformOption is allowed");
        }
        EclipsePlatformOption option = options[0];
        if (option.getLauncher().isForked()) {
            throw new UnsupportedOperationException("Sorry not supported yet :-(");
            // return new RemoteEclipsePlatformTestContainer(system);
        }
        else {
            return new EclipsePlatformTestContainer(system);
        }
    }
}
