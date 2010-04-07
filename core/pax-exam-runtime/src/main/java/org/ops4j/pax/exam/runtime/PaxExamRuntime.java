/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.ops4j.pax.exam.runtime;

import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.exam.spi.container.TestContainerFactory;
import org.ops4j.pax.exam.spi.container.TestTargetFactory;

/**
 * Pax Exam runtime.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 09, 2008
 */
public class PaxExamRuntime
{

    private static final Log LOG = LogFactory.getLog( PaxExamRuntime.class );

    /**
     * Utility class. Ment to be used via the static factory methods.
     */
    private PaxExamRuntime()
    {
        // utility class
    }

    /**
     * Discovers the test container. Discovery is performed via Appache Commons discovery mechanism.
     *
     * @return discovered test container
     */
	public static TestContainerFactory getTestContainerFactory()
    {
        LOG.info( "Pax Exam Runtime: looking for a " + TestContainerFactory.class.getName() );

        return (TestContainerFactory) DiscoverSingleton.find( TestContainerFactory.class );
    }

    /**
     * Select yourself
     *
     * @param select the exact implementation if you dont want to rely on commons util discovery or
     *               change different containers in a single project.
     *
     * @return discovered test container
     */
    public static TestContainerFactory getTestContainerFactory( Class<? extends TestContainerFactory> select )
    {
        try
        {
            return select.newInstance();
        } catch( InstantiationException e )
        {
            throw new IllegalArgumentException( "Class  " + select + "is not a valid Test Container Factory.", e );
        } catch( IllegalAccessException e )
        {
            throw new IllegalArgumentException( "Class  " + select + "is not a valid Test Container Factory.", e );
        }
    }

    /**
     * Discovers the test target. Discovery is performed via Appache Commons discovery mechanism.
     *
     * @return discovered test target
     */
    public static TestTargetFactory getTestTargetFactory()
    {
        LOG.info( "Pax Exam Runtime: looking for a " + TestTargetFactory.class.getName() );

        return (TestTargetFactory) DiscoverSingleton.find( TestTargetFactory.class );
    }

}
