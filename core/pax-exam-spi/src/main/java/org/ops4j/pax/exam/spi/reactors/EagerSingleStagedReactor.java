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

    private TestContainer[] m_targetContainer;

    /**
     * @param factory         to be used to instantiate container(s).
     * @param mConfigurations that are already "deflattened" and reflect single container instances
     * @param mProbes
     */
    public EagerSingleStagedReactor( TestContainerFactory factory, List<Option[]> mConfigurations, List<TestProbeBuilder> mProbes )
    {
        OptionPrinter printer = new OptionPrinter();
        if( mConfigurations.size() < 1 )
        {
            // fill in a default config
            mConfigurations.add( new Option[0] );
        }

        List<OptionDescription> m_targets = new ArrayList<OptionDescription>();
        for( Option[] option : mConfigurations )
        {
            m_targets.addAll( Arrays.asList( factory.parse( option ) ) );
        }

        List<TestContainer> containers = new ArrayList<TestContainer>();

        for( OptionDescription description : m_targets )
        {
            TestContainer container = factory.createContainer( description );
            printer.print( this.getClass().getName(), description, container.getClass() );

            containers.add( container );
            container.start();

            for( TestProbeBuilder builder : mProbes )
            {
                LOG.debug( "installing probe " + builder );
                container.install( builder.getStream() );
            }
        }
        m_targetContainer = containers.toArray( new TestContainer[containers.size()] );
    }

    public void invoke( ProbeCall call )
        throws Exception
    {
        LOG.debug( "Trying to invoke signature: " + call.signature() );
        for( TestContainer container : m_targetContainer )
        {

            DefaultRaw.execute( container, call );
        }
    }

    public void tearDown()
    {
        for( TestContainer container : m_targetContainer )
        {
            container.stop();
        }
    }


}
