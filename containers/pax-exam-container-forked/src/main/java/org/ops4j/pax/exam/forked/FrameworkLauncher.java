/*
 * Copyright 2011 Harald Wellmann.
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
package org.ops4j.pax.exam.forked;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;


public class FrameworkLauncher
{
    private Map<String, String> buildFrameworkProperties( String[] args )
    {
        Map<String,String> props = new HashMap<String, String>();
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException( "even number of arguments required" );
        }
        for (int i = 0; i < args.length; i += 2) {
            System.out.println(args[i]);
            System.out.println(args[i+1]);
            props.put( args[i], args[i+1] );
        }
        return props;
    }

    public FrameworkFactory findFrameworkFactory() {
        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load( FrameworkFactory.class );
        FrameworkFactory factory = loader.iterator().next();
        return factory;        
    }
    
    private void launch( String[] args ) throws BundleException
    {
        System.setProperty( "org.ops4j.pax.exam.rbc.rmi.host", "localhost" );
        System.setProperty( "org.ops4j.pax.exam.rbc.rmi.name", "Exam" );
        System.setProperty( "org.ops4j.pax.exam.rbc.rmi.port", "1099" );
        //System.setProperty( "osgi.console", "6666" );
        
        Map<String, String> props = buildFrameworkProperties( args );
        FrameworkFactory factory = findFrameworkFactory();
        Framework framework = factory.newFramework( props );
        System.out.println("starting framework");
        framework.start();
        System.out.println("framework stopped");
        //framework.stop();
    }

    public static void main( String[] args ) throws BundleException
    {
        FrameworkLauncher launcher = new FrameworkLauncher();
        launcher.launch(args);        
    }

}
