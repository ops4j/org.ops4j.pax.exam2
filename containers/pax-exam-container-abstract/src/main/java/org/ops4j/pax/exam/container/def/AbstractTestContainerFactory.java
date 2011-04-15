/*
 * Copyright 2009 Alin Dreghiciu.
 * Copyright 2011 Toni Menzel.
 * Copyright 2011 Stephane Chomat.
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
package org.ops4j.pax.exam.container.def;

import java.util.ArrayList;
import java.util.List;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.container.def.util.RMIRegistry;
import org.ops4j.pax.exam.rbc.Constants;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.*;
import static org.ops4j.pax.exam.Constants.*;

/**
 * Factory for {@link PaxRunnerTestContainer}.
 *
 * @author Toni Menzel
 * @since 2.0, March 09, 2011
 */
public abstract class AbstractTestContainerFactory
    implements TestContainerFactory {

    protected RMIRegistry m_rmiRegistry;
    private static final int DEFAULTPORT = 21412;

    public AbstractTestContainerFactory()

    {
            m_rmiRegistry = new RMIRegistry( DEFAULTPORT, DEFAULTPORT + 1, DEFAULTPORT + 99 ).selectGracefully();
    }

    /**
     * {@inheritDoc}
     */
    public TestContainer[] parse( Option... options )
    {

        options = expand( combine( options, setDefaultOptions() ) );

        List<TestContainer> containers = new ArrayList<TestContainer>();
        
        createTestContainers(containers, options);

        return containers.toArray( new TestContainer[ containers.size() ] );
    }

	protected abstract void createTestContainers(List<TestContainer> containers,
			Option... options) ;

    public void shutdown()
    {
        System.gc();

    }
    // overide add RMI_PORT_PROPERTY and add start level
    protected Option[] setDefaultOptions()
    {
        return new Option[]{
            // remote bundle context bundle

            // rmi communication port
            systemProperty(Constants.RMI_PORT_PROPERTY).value(Integer.toString(m_rmiRegistry.getPort())),
            //,
            // boot delegation for sun.*. This seems only necessary in Knopflerfish version > 2.0.0
            bootDelegationPackage( "sun.*" ),
            
            url( "link:classpath:META-INF/links/org.ops4j.pax.exam.rbc.link" ).startLevel(START_LEVEL_SYSTEM_BUNDLES),
            url( "link:classpath:META-INF/links/org.ops4j.pax.extender.service.link" ).startLevel(START_LEVEL_SYSTEM_BUNDLES)
            
        };
    }


}
