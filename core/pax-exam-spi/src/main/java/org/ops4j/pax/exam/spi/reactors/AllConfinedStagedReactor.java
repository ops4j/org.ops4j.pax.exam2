/*
 * Copyright (C) 2010 Toni Menzel
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

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.intern.DefaultTestAddress;

/**
 * This will use new containers for any regression (hence confined)
 */
public class AllConfinedStagedReactor implements StagedExamReactor
{

    final private List<TestProbeProvider> m_probes;
    final private HashMap<TestAddress, TestContainer> m_map;

    /**
     * @param containers to be used
     * @param mProbes probes to be installed
     */
    public AllConfinedStagedReactor( List<TestContainer> containers, List<TestProbeProvider> mProbes )
    {
        m_probes = mProbes;
        m_map = new HashMap<TestAddress, TestContainer>();
        // todo: don't do this here.
        for ( TestContainer container : containers )
        {
            for ( TestProbeProvider builder : m_probes )
            {
                for ( TestAddress a : builder.getTests() )
                {
                    m_map.put( new DefaultTestAddress( a, container.toString() ), container );
                }
            }
        }
    }
    
    public void execute()
    {
        
    }

    public void invoke( TestAddress address )
            throws Exception
    {
        assert (address != null) : "TestAddress must not be null.";
        // you can directly invoke:
        TestContainer container = m_map.get( address );
        if ( container == null )
        {
            throw new IllegalArgumentException( "TestAddress " + address + " not from this reactor? Got it from getTargets() really?" );
        }
        container.start(  );
        try
        {
            for ( TestProbeProvider builder : m_probes )
            {
                container.install( builder.getStream() );
            }
            container.call( address );
        } finally
        {
            container.stop();
        }

    }

    public Set<TestAddress> getTargets()
    {
        return m_map.keySet();
    }

    public void tearDown()
    {
        // does not do anything.
    }
}
