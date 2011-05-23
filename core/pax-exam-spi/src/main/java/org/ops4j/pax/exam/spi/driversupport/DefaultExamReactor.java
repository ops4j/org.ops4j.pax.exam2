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
package org.ops4j.pax.exam.spi.driversupport;

import static org.ops4j.pax.exam.CoreOptions.options;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.ExxamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reactor decouples {@link org.ops4j.pax.exam.TestContainer} state from the observer. It is also
 * in control to map probes to their configurations or vice versa. In essence,
 * this implements the Container re-start/re-use policy topic by collecting relevant tests and configurations and passing them to a (user selected factory (see stage()).
 *
 * @author tonit
 */
public class DefaultExamReactor implements ExxamReactor {

    private static Logger LOG = LoggerFactory.getLogger( DefaultExamReactor.class );

    final private List<ExamSystem> m_configurations;
    final private List<TestProbeProvider> m_probes;
    final private TestContainerFactory m_factory;

    public DefaultExamReactor( TestContainerFactory factory )
    {
        m_configurations = new ArrayList<ExamSystem>();
        m_probes = new ArrayList<TestProbeProvider>();
        m_factory = factory;
    }

    synchronized public void addConfiguration( ExamSystem configuration )
    {
        m_configurations.add( configuration );
    }

    synchronized public void addProbe( TestProbeProvider addTest )
    {
        m_probes.add( addTest );
    }

    synchronized public StagedExamReactor stage( StagedExamReactorFactory factory ) throws IOException
    {
        LOG.info( "Staging reactor with probes: " + m_probes.size() + " using strategy: " + factory );
        List<TestContainer> containers = new ArrayList<TestContainer>();

        if( m_configurations.isEmpty() ) {
            LOG.info( "No configuration given. Setting an empty one." );
            m_configurations.add( PaxExamRuntime.createSystem( options() ) );
        }
        for( ExamSystem system : m_configurations ) {
            containers.addAll( Arrays.asList( m_factory.create( system ) ) );
        }

        return factory.create( containers, m_probes );
    }

}
