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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.probesupport.intern.DefaultTestAddress;

/**
 * One target only reactor implementation (simpliest and fastest)
 *
 * @author tonit
 */
public class EagerSingleStagedReactor implements StagedExamReactor {

    private static Logger LOG = LoggerFactory.getLogger( EagerSingleStagedReactor.class );

    final private List<TestContainer> m_targetContainer;
    final private HashMap<TestAddress, TestContainer> m_map;

    /**
     * @param containers to be used
     * @param mProbes    to be installed on all probes
     */
    public EagerSingleStagedReactor( List<TestContainer> containers, List<TestProbeProvider> mProbes )
    {
        m_map = new HashMap<TestAddress, TestContainer>();
        m_targetContainer = containers;

        for( TestContainer container : containers ) {
            container.start();

            for( TestProbeProvider builder : mProbes ) {
                LOG.info( "installing probe " + builder );

                try {
                    container.install( builder.getStream() );
                } catch( IOException e ) {
                    throw new TestContainerException( "Unable to build the probe.", e );
                }

                // each probe has addresses.
                for( TestAddress a : builder.getTests() ) {
                    // we need to create a new, because "a" exists for each test container
                    // this new address makes the test (reachable via getTargets() ) reachable directly.
                    m_map.put( new DefaultTestAddress( a, container.toString() ), container );
                }
            }
        }
    }

    public void invoke( TestAddress address )
        throws Exception
    {
        assert ( address != null ) : "TestAddress must not be null.";

        TestContainer testContainer = m_map.get( address );
        if( testContainer == null ) {
            throw new IllegalArgumentException( "TestAddress " + address + " not from this reactor? Got it from getTargets() really?" );
        }
        testContainer.call( address );
    }

    public Set<TestAddress> getTargets()
    {
        return m_map.keySet();
    }

    public void tearDown()
    {
        for( TestContainer container : m_targetContainer ) {
            container.stop();
        }
    }
}
