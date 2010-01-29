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
package org.ops4j.pax.exam.samples.probe1.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;
import org.ops4j.pax.exam.raw.extender.ProbeInvoker;
import org.ops4j.pax.exam.samples.probe1.MyHandrolledTest;

/**
 * @author Toni Menzel
 * @since Jan 29, 2010
 */
public class Activator extends DependencyActivatorBase
{

    @Override
    public void init( BundleContext bundleContext, DependencyManager dependencyManager )
        throws Exception
    {
        Dictionary<String, String> dict = new Hashtable<String, String>();
        dict.put( "Probe-Signature", "mytest" );
        dependencyManager.add(
            createService()
                .setInterface( ProbeInvoker.class.getName(), dict )
                .setImplementation( new MyHandrolledTest() )
                .add( createServiceDependency().setService( LogService.class ) )
        );
    }

    @Override
    public void destroy( BundleContext bundleContext, DependencyManager dependencyManager )
        throws Exception
    {

    }
}
