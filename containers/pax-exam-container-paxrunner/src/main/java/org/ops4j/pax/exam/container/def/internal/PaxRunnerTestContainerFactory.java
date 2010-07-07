/*
 * Copyright 2008 Alin Dreghiciu.
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

import java.util.HashMap;
import java.util.Map;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.container.remote.RBCRemoteTarget;
import org.ops4j.pax.runner.platform.DefaultJavaRunner;

/**
 * Factory for {@link PaxRunnerTestContainer}.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 09, 2008
 */
public class PaxRunnerTestContainerFactory
    implements TestContainerFactory
{

    final private Map<OptionDescription, TestContainer> m_registry = new HashMap<OptionDescription, TestContainer>();

    /**
     * {@inheritDoc}
     */
    public OptionDescription[] parse( final Option... options )
    {
        // Parser for the PaxRunnerTestContainer
        ArgumentsBuilder argBuilder = new ArgumentsBuilder( 9999, options );

        // constructor of PaxRunnerTestContainer should be as explicit as possible.
        // So no Option[] and also no argBuilder in the end.

        // TODO tbd: make this more explicit
        
        RBCRemoteTarget target = new RBCRemoteTarget( options);

        TestContainer container = new PaxRunnerTestContainer( new DefaultJavaRunner( false ),
                                                              argBuilder.getArguments(),
                                                              argBuilder.getWorkingFolder(),
                                                              argBuilder.getStartTimeout(),
                                                              target,

                                                              argBuilder.getCustomizers()
        );

        OptionDescription descr = argBuilder.getOptionDescription();
        m_registry.put( descr, container );
        return new OptionDescription[]{
            descr
        };
    }

    public TestContainer createContainer( OptionDescription option )
    {
        return m_registry.get( option );
    }

}
