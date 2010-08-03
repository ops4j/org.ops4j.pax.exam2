/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.container.remote;

import java.util.HashMap;
import java.util.Map;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;

/**
 * @author Toni Menzel
 * @since Jan 26, 2010
 */
public class RBCRemoteContainerFactory implements TestContainerFactory
{

    final private Map<OptionDescription, TestContainer> m_registry = new HashMap<OptionDescription, TestContainer>();

    /**
     * {@inheritDoc}
     */
    public OptionDescription[] parse( final Option... options )
    {
        Parser p = new Parser( options );
        OptionDescription descr = p.getOptionDescription();
        TestContainer container = new RBCRemoteContainer( new RBCRemoteTarget( p.getHost(), p.getRMIPort(), p.getRMILookupTimpout() ) );
        m_registry.put( descr, container );
        return new OptionDescription[]{
            descr
        };
    }

    public TestContainer createContainer( OptionDescription option )
    {
        return m_registry.get( option );
    }

}
