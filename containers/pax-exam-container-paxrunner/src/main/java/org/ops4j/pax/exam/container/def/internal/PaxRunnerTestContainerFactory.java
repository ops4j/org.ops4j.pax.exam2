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

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.container.remote.Parser;
import org.ops4j.pax.exam.container.remote.RBCRemoteTarget;
import org.ops4j.pax.exam.options.FrameworkOption;
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


    /**
     * {@inheritDoc}
     */
    public TestContainer[] parse( final Option... options )
    {
        final FrameworkOption[] frameworkOptions = OptionUtils.filter(FrameworkOption.class, options);
        final Option[] filteredOptions = OptionUtils.remove( FrameworkOption.class, options );

        if( frameworkOptions.length == 0 )
        {
            return new TestContainer[] {
                    createContainer( filteredOptions )
            };
        }
        else
        {
            TestContainer[] containers = new TestContainer[ frameworkOptions.length ];
            for( int i = 0; i < containers.length; i++ )
            {
                Option[] opts = OptionUtils.combine( filteredOptions, frameworkOptions[ i ] );
                containers[i] = createContainer( opts );
            }
            return containers;
        }
    }

    private TestContainer createContainer( final Option... options )
    {
        // Parser for the PaxRunnerTestContainer
        RBCRemoteTarget target = createRemoteTarget(options);
        ArgumentsBuilder argBuilder = new ArgumentsBuilder( target.getClientRBC().getRmiPort(), options );

        // constructor of PaxRunnerTestContainer should be as explicit as possible.
        // So no Option[] and also no argBuilder in the end.

        TestContainer container = new PaxRunnerTestContainer( new DefaultJavaRunner( false ),
                                                              argBuilder.getArguments(),
                                                              argBuilder.getWorkingFolder(),
                                                              argBuilder.getStartTimeout(),
                                                              target,
                                                              argBuilder.getCustomizers()
        );

        return container;
    }

    private RBCRemoteTarget createRemoteTarget(Option[] options) {
        Parser p = new Parser( options);
        return new RBCRemoteTarget(p.getHost(),p.getRMIPort(),p.getRMILookupTimpout() );
    }

}
