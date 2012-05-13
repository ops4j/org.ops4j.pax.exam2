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
package org.ops4j.pax.exam.spi;

import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ops4j.pax.exam.ConfigurationFactory;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.spi.ServiceProviderFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reactor decouples {@link org.ops4j.pax.exam.TestContainer} state from the observer. It is also in
 * control to map probes to their configurations or vice versa. In essence, this implements the
 * Container re-start/re-use policy topic by collecting relevant tests and configurations and
 * passing them to a (user selected factory (see stage()).
 * 
 * @author tonit
 */
public class DefaultExamReactor implements ExamReactor
{

    private static Logger LOG = LoggerFactory.getLogger( DefaultExamReactor.class );

    final private List<Option[]> m_configurations;
    final private List<TestProbeProvider> m_probes;
    final private TestContainerFactory m_factory;

    final private ExamSystem m_system;

    public DefaultExamReactor( ExamSystem system, TestContainerFactory factory )
    {
        m_system = system;
        m_configurations = new ArrayList<Option[]>();
        m_probes = new ArrayList<TestProbeProvider>();
        m_factory = factory;
    }

    synchronized public void addConfiguration( Option[] configuration )
    {
        m_configurations.add( configuration );
    }

    synchronized public void addProbe( TestProbeProvider addTest )
    {
        m_probes.add( addTest );
    }

    synchronized public StagedExamReactor stage( StagedExamReactorFactory factory )
        throws IOException
    {
        LOG.debug( "Staging reactor with probes: " + m_probes.size() + " using strategy: "
                + factory );
        List<TestContainer> containers = new ArrayList<TestContainer>();

        if( m_configurations.isEmpty() )
        {
            List<ConfigurationFactory> configurationFactories =
                ServiceProviderFinder.findServiceProviders( ConfigurationFactory.class );
            for( ConfigurationFactory cf : configurationFactories )
            {
                Option[] configuration = cf.createConfiguration();
                addConfiguration( configuration );
            }
        }
        if( m_configurations.isEmpty() )
        {
            LOG.debug( "No configuration given. Setting an empty one." );
            m_configurations.add( options() );
        }
        for( Option[] config : m_configurations )
        {
            containers.addAll( Arrays.asList( m_factory.create( m_system.fork( config ) ) ) );
        }

        return factory.create( containers, m_probes );
    }

}
