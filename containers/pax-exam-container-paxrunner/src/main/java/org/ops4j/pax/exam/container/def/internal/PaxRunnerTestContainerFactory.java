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

import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.OptionUtils.filter;
import static org.ops4j.pax.exam.OptionUtils.remove;

import java.util.List;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.container.def.AbstractTestContainerFactory;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.runner.platform.DefaultJavaRunner;

/**
 * Factory for {@link PaxRunnerTestContainer}.
 *
 * @author Toni Menzel
 * @since 2.0, March 09, 2011
 */
public class PaxRunnerTestContainerFactory
    extends AbstractTestContainerFactory {

    @Override
    protected void createTestContainers(List<TestContainer> containers,
            Option... options) {
        FrameworkOption[] frameworks = getFrameworks( options );
        options = remove( FrameworkOption.class, options );

         for( FrameworkOption framework : frameworks ) {
             containers.add(
                 new PaxRunnerTestContainer(
                     new DefaultJavaRunner( false ),
                     m_rmiRegistry.getHost(),
                     m_rmiRegistry.getPort(),
                     combine( options, framework )
                 )
             );
         }
    }

    private FrameworkOption[] getFrameworks( Option[] options )
    {
        FrameworkOption[] frameworks = ( filter( FrameworkOption.class, options ) );
        if( frameworks.length == 0 ) {
            frameworks = new FrameworkOption[]{ felix() };
        }
        return frameworks;
    }

}
