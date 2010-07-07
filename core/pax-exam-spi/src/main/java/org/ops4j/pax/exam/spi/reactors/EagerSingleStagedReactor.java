/*
 * Copyright 2010 Toni Menzel.
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
package org.ops4j.pax.exam.spi.reactors;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.ops4j.pax.exam.spi.container.CompositeOptionDescription;
import org.ops4j.pax.exam.spi.container.CompositeTestContainer;
import org.ops4j.pax.exam.spi.container.DefaultRaw;

/**
 * One target only reactor implementation (simpliest and fastest)
 * *
 *
 * @author tonit
 */
public class EagerSingleStagedReactor implements StagedExamReactor
{

    private static Logger LOG = LoggerFactory.getLogger( EagerSingleStagedReactor.class );

    final private OptionDescription m_target;
    private TestContainer m_targetContainer;

    /**
     * @param mConfigurations that are already "deflattened" and reflect single container instances
     * @param mProbes
     */
    public EagerSingleStagedReactor( TestContainerFactory factory, List<Option[]> mConfigurations, List<TestProbeBuilder> mProbes )
    {
        List<OptionDescription> m_targets = new ArrayList<OptionDescription>();
        if( mConfigurations.size() < 1 )
        {
            // fill in a default config
            mConfigurations.add( new Option[0] );
        }

        for( Option[] option : mConfigurations )
        {
            m_targets.addAll( Arrays.asList( factory.parse( option ) ) );
        }

        m_target = new CompositeOptionDescription( m_targets );

        m_targetContainer = null; // m_target.getContainer();
        m_targetContainer.start();

        for( TestProbeBuilder builder : mProbes )
        {
            LOG.debug( "installing probe " + builder );
            m_targetContainer.install( builder.getStream() );
        }
    }

    public void invoke( ProbeCall call )
        throws Exception
    {
        LOG.debug( "Trying to invoke signature: " + call.signature() );

        DefaultRaw.execute( print( findMatchingTargetInstance( call ) ), call );
    }

    private TestContainer findMatchingTargetInstance( ProbeCall call )
    {
        return m_targetContainer;
    }

    public TestContainer print( final TestContainer container )
    {
        OptionDescription options = null;//container.getOptionDescription();
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
        return container;
    }

    public void tearDown()
    {
        m_targetContainer.stop();
    }


}
