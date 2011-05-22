/*
 * Copyright 2009 Alin Dreghiciu.
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.container.def.internal;

import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.runner.platform.DefaultJavaRunner;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.*;

/**
 * Factory for {@link PaxRunnerTestContainer}.
 *
 * @author Toni Menzel
 * @since 2.0, March 09, 2011
 */
public class PaxRunnerTestContainerFactory
    implements TestContainerFactory {

    private RMIRegistry m_rmiRegistry;
    private static final int DEFAULTPORT = 21412;
    private static final boolean BLOCKING_RUNNER_INTERNALLY = true;

    public PaxRunnerTestContainerFactory()

    {
            m_rmiRegistry = new RMIRegistry( DEFAULTPORT, DEFAULTPORT + 1, DEFAULTPORT + 99 ).selectGracefully();
    }

    /**
     * {@inheritDoc}
     */
    public TestContainer[] materializeContainers( ExamSystem system )
    {
        FrameworkOption[] frameworks = getFrameworks( system );
        
        List<TestContainer> containers = new ArrayList<TestContainer>();
        for( FrameworkOption framework : frameworks ) {
            containers.add(
                new PaxRunnerTestContainer(
                	system,
                    new AsyncJavaRunner( new DefaultJavaRunner( BLOCKING_RUNNER_INTERNALLY ) ),
                    m_rmiRegistry,
                    framework
                )
              );
        }

        return containers.toArray( new TestContainer[ containers.size() ] );
    }

    private FrameworkOption[] getFrameworks( ExamSystem system )
    {
        FrameworkOption[] frameworks = ( system.getOptions( FrameworkOption.class ) );
        if( frameworks.length == 0 ) {
            frameworks = new FrameworkOption[]{ felix() };
        }
        return frameworks;
    }

}
