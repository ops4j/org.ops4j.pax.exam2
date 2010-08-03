/*
 * Copyright (C) 2010 Okidokiteam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.spi.reactors;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.spi.ProbeCall;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.TestProbeBuilder;
import org.ops4j.pax.exam.spi.container.DefaultRaw;

/**
 * This will use new containers for any test (hence confined)
 *
 */
public class AllConfinedStagedReactor implements StagedExamReactor
{

    private static Logger LOG = LoggerFactory.getLogger( AllConfinedStagedReactor.class );

    final private List<Option[]> m_configs;
    final private List<TestProbeBuilder> m_probes;
    final private TestContainerFactory m_factory;

    /**
     * @param mConfigurations
     * @param mProbes
     */
    public AllConfinedStagedReactor( TestContainerFactory factory, List<Option[]> mConfigurations, List<TestProbeBuilder> mProbes )
    {
        m_configs = mConfigurations;
        m_probes = mProbes;
        m_factory = factory;

        if( m_configs.size() < 1 )
        {
            // fill in a default config
            m_configs.add( new Option[0] );
        }
    }

    public void invoke( ProbeCall call )
        throws Exception
    {
        LOG.debug( "Trying to invoke signature: " + call.signature() );
        // create a container for each call:
        for( Option[] option : m_configs )
        {
            OptionDescription[] runtimes = m_factory.parse( option );
            for( OptionDescription s : runtimes )
            {
                print(s);
                TestContainer runtime = m_factory.createContainer( s );
                runtime.start();
                try
                {
                    for( TestProbeBuilder builder : m_probes )
                    {
                        LOG.debug( "installing probe " + builder );
                        runtime.install( builder.getStream() );
                    }
                    DefaultRaw.execute( runtime, call );

                } finally
                {
                    runtime.stop();
                }
            }

        }
    }

    public OptionDescription print( final OptionDescription options )
    {
        if( options.getIgnoredOptions().length + options.getUsedOptions().length == 0 )
        {
            LOG.debug( "! Possible problem: No options discovered. " );

        }
        LOG.debug( "Option statistics: " );
        for( Option s : options.getUsedOptions() )
        {
            LOG.debug( "+ : " + s );

        }

        for( Option s : options.getIgnoredOptions() )
        {
            LOG.debug( "- : " + s );

        }
        return options;
    }

    public void tearDown()
    {

    }
}
