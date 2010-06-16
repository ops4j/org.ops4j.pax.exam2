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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.container.DefaultRaw;
import org.ops4j.pax.exam.spi.container.ProbeCall;
import org.ops4j.pax.exam.spi.container.TestProbeBuilder;
import org.ops4j.pax.exam.spi.container.internal.PaxExamRuntime;

/**
 * This will use new containers for any test (hence confined)
 *
 * TODO: Should be moved out of driver layer.
 */
public class AllConfinedStagedReactor implements StagedExamReactor
{

    private static Log LOG = LogFactory.getLog( EagerSingleStagedReactor.class );

    final private List<Option[]> m_configs;
    final private List<TestProbeBuilder> m_probes;

    /**
     * @param mConfigurations
     * @param mProbes
     */
    public AllConfinedStagedReactor( List<Option[]> mConfigurations, List<TestProbeBuilder> mProbes )
    {
        m_configs = mConfigurations;
        m_probes = mProbes;

        if( mConfigurations.size() < 1 )
        {
            // fill in a default config
            mConfigurations.add( new Option[0] );
        }
    }

    public void invoke( ProbeCall call )
        throws Exception
    {
        LOG.debug( "Trying to invoke signature: " + call.signature() );
        // create a container for each call:
        for( Option[] option : m_configs )
        {
            TestContainer runtime = PaxExamRuntime.getTestContainerFactory().newInstance( option );
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

    public void tearDown()
    {

    }
}
