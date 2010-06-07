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
package org.ops4j.pax.exam.junit;

import java.util.ArrayList;
import java.util.List;

import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.container.TestProbeBuilder;

/**
 * Reactor decouples {@link org.ops4j.pax.exam.spi.container.TestContainer} state from the observer. It is also
 * in control to map probes to their configurations or vice versa. In essence,
 * this implements the Container re-start/re-use policy topic.
 *
 * @author tonit
 */
public class ExamReactor
{

    private List<Option[]> m_configurations;
    private List<TestProbeBuilder> m_probes;

    public ExamReactor()
    {
        m_configurations = new ArrayList<Option[]>();
        m_probes = new ArrayList<TestProbeBuilder>();
    }

    synchronized public void addConfiguration( Option[] options )
    {
        m_configurations.add( options );
    }

    synchronized public void addProbe( TestProbeBuilder addTest )
    {
        m_probes.add( addTest );
    }

    synchronized public StagedExamReactor stage()
    {
        // do some fancy stuff and create the staged reactor

        // 1. Cut out the framework options.

        // for now we don't care

        return new EagerSingleStagedReactor( m_configurations, m_probes );
    }

}
