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
package org.ops4j.pax.exam.it;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.Inject;

/**
 * This is a sample that is being configured to give max. insight into pax exam by
 * - having log service installed
 * - setting low log levels.
 *
 * This test is mostly being used manually to track problems if they show up.
 *
 * @author Toni Menzel (tonit)
 * @since Mar 3, 2009
 */
public abstract class DefaultTrace
{

    public Log logger = LogFactory.getLog( DefaultTrace.class );

    @Test
    public void test1( BundleContext bundleContext )
    {
        logger.trace( "******** This a trace from OSGi" );
        logger.debug( "******** This a debug from OSGi" );
        logger.info( "******** This a info from OSGi" );
        logger.warn( "******** This a warn from OSGi" );
        logger.error( "******** This a errory from OSGi" );

        logger.info( "This is running inside OSGi. With all configuration set up like you specified. " );
        for( Bundle b : bundleContext.getBundles() )
        {
            System.out.println( "--> Bundle " + b.getBundleId() + " : " + b.getSymbolicName() + " : " + b.getState() );
        }

    }
}
