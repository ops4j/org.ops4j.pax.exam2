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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

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
public class ForkedTestContainerFactory implements TestContainerFactory
{

    public TestContainer[] create( final ExamSystem system )
    {
        List<TestContainer> containers = new ArrayList<TestContainer>();
        Iterator<FrameworkFactory> factories =
            ServiceLoader.load( FrameworkFactory.class ).iterator();
        boolean factoryFound = false;
        while ( factories.hasNext() )
        {
            containers.add( new ForkedTestContainer( system, factories.next() ) );
            factoryFound = true;
        }
        if( !factoryFound )
        {
            throw new TestContainerException(
                "No service org.osgi.framework.launch.FrameworkFactory found in META-INF/services on classpath" );
        }
        return containers.toArray( new TestContainer[containers.size()] );
    }
}
