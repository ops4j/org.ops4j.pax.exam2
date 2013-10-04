/*
 * Copyright 2011 Harald Wellmann.
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
package org.ops4j.pax.exam.forked;

import static org.ops4j.pax.swissbox.framework.FrameworkFactoryFinder.findFrameworkFactories;

import java.util.List;

import org.kohsuke.MetaInfServices;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * A {@link TestContainerFactory} creating a {@link ForkedTestContainer} for each OSGi
 * {@code FrameworkFactory} found by the JRE ServiceLoader on the classpath.
 * 
 * @author Harald Wellmann
 * 
 */
@MetaInfServices
public class ForkedTestContainerFactory implements TestContainerFactory {

    public TestContainer[] create(final ExamSystem system) {
        List<FrameworkFactory> frameworkFactories = findFrameworkFactories();
        if (frameworkFactories.isEmpty()) {
            throw new TestContainerException(
                "No service org.osgi.framework.launch.FrameworkFactory found in META-INF/services on classpath");
        }
        TestContainer[] containers = new TestContainer[frameworkFactories.size()];
        int index = 0;
        for (FrameworkFactory frameworkFactory : frameworkFactories) {
            containers[index++] = new ForkedTestContainer(system, frameworkFactory);
        }
        return containers;
    }
}
