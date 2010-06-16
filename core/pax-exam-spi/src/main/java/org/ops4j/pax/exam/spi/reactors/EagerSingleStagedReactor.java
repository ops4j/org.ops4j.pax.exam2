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
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestTarget;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.container.CompositeTestContainer;
import org.ops4j.pax.exam.spi.container.DefaultRaw;
import org.ops4j.pax.exam.spi.ProbeCall;
import org.ops4j.pax.exam.spi.TestProbeBuilder;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;

/**
 * One target only reactor implementation (simpliest and fastest)
 *
 * TODO: Should be moved out of driver layer.
 *
 * @author tonit
 */
public class EagerSingleStagedReactor implements StagedExamReactor
{

    private static Log LOG = LogFactory.getLog( EagerSingleStagedReactor.class );

    final private TestContainer m_target;

    /**
     * @param mConfigurations that are already "deflattened" and reflect single container instances
     * @param mProbes
     */
    public EagerSingleStagedReactor( List<Option[]> mConfigurations, List<TestProbeBuilder> mProbes )
    {
        List<TestContainer> m_targets = new ArrayList<TestContainer>();

        if( mConfigurations.size() < 1 )
        {
            // fill in a default config
            mConfigurations.add( new Option[0] );
        }
        else
        {
            for( Option[] option : mConfigurations )
            {
                m_targets.add( PaxExamRuntime.getTestContainerFactory().newInstance( option ) );
            }
        }
        m_target = new CompositeTestContainer( m_targets );
        m_target.start();

        for( TestProbeBuilder builder : mProbes )
        {
            LOG.debug( "installing probe " + builder );
            m_target.install( builder.getStream() );
        }

    }

    public void invoke( ProbeCall call )
        throws Exception
    {
        LOG.debug( "Trying to invoke signature: " + call.signature() );

        DefaultRaw.execute( findMatchingTargetInstance( call ), call );
    }

    private TestTarget findMatchingTargetInstance( ProbeCall call )
    {
        return m_target;
    }

    public void tearDown()
    {
        m_target.stop();
    }

}
