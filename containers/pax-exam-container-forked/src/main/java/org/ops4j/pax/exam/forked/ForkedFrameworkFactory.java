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

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ops4j.exec.DefaultJavaRunner;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.swissbox.framework.RemoteFramework;
import org.ops4j.pax.swissbox.framework.RemoteFrameworkImpl;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * Wraps an OSGi {@link FrameworkFactory} to create and launch a framework in a forked Java virtual
 * machine running in a separate process.
 * <p>
 * The framework in the forked process can be controlled via a {@link RemoteFramework} interface.
 * 
 * @author Harald Wellmann
 * 
 */
public class ForkedFrameworkFactory
{
    // TODO make this configurable
    private static final long TIMEOUT = 60 * 1000;
    
    private FrameworkFactory frameworkFactory;
    private Registry registry;

    private int port;

    private DefaultJavaRunner javaRunner;
    

    /**
     * Creates a ForkedFrameworkFactory wrapping a given OSGi FrameworkFactory and a given framework
     * storage directory
     * 
     * @param frameworkFactory OSGi framework factory
     * @param storage framework storage directory for the forked framework
     */
    public ForkedFrameworkFactory( FrameworkFactory frameworkFactory )
    {
        this.frameworkFactory = frameworkFactory;
    }

    public FrameworkFactory getFrameworkFactory()
    {
        return frameworkFactory;
    }

    public void setFrameworkFactory( FrameworkFactory frameworkFactory )
    {
        this.frameworkFactory = frameworkFactory;
    }

    /**
     * Forks a Java VM process running an OSGi framework and returns a {@link RemoteFramework}
     * handle to it.
     * <p>
     * @param vmArgs VM arguments 
     * @param systemProperties system properties for the forked Java VM
     * @param frameworkProperties framework properties for the remote framework
     * @return
     * @throws BundleException
     * @throws IOException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    public RemoteFramework fork( List<String> vmArgs, Map<String, String> systemProperties,
            Map<String, Object> frameworkProperties )
        throws BundleException, IOException,
               InterruptedException, NotBoundException, URISyntaxException
    {
        // TODO make port range configurable
        FreePort freePort = new FreePort( 21000, 21099 );
        port = freePort.getPort();
        
        String rmiName = "ExamRemoteFramework-" + UUID.randomUUID().toString();

        registry = LocateRegistry.createRegistry( port );

        Map<String,String> systemPropsNew = new HashMap<String, String>(systemProperties);
        systemPropsNew.put( RemoteFramework.RMI_PORT_KEY, Integer.toString( port ) );
        systemPropsNew.put( RemoteFramework.RMI_NAME_KEY, rmiName );
        String[] vmOptions = buildSystemProperties( vmArgs, systemPropsNew );
        String[] args = buildFrameworkProperties( frameworkProperties );
        javaRunner = new DefaultJavaRunner( false );
        javaRunner.exec( vmOptions, buildClasspath(), RemoteFrameworkImpl.class.getName(),
            args, getJavaHome(), null );
        return findRemoteFramework(port, rmiName);
    }

    private String[] buildSystemProperties( List<String> vmArgs, Map<String, String> systemProperties )
    {
        String[] vmOptions = new String[vmArgs.size() + systemProperties.size() ];
        int i = 0;
        for (String vmArg : vmArgs)
        {
            vmOptions[i++] = vmArg;
        }
        for ( Map.Entry<String, String> entry : systemProperties.entrySet() )
        {
            vmOptions[i++] = String.format( "-D%s=%s", entry.getKey(), entry.getValue() );
        }
        return vmOptions;
    }

    private String[] buildFrameworkProperties( Map<String, Object> frameworkProperties )
    {
        String[] args = new String[frameworkProperties.size()];
        int i = 0;
        for( Map.Entry<String, Object> entry : frameworkProperties.entrySet() )
        {
            args[i++] = String.format( "-F%s=%s", entry.getKey(), entry.getValue().toString() );
        }
        return args;
    }

    private String getJavaHome()
    {
        String javaHome = System.getenv( "JAVA_HOME" );
        if( javaHome == null )
        {
            javaHome = System.getProperty( "java.home" );
        }
        return javaHome;
    }

    private String[] buildClasspath()
        throws URISyntaxException
    {
        String frameworkPath =
            frameworkFactory.getClass().getProtectionDomain().getCodeSource().getLocation()
                .toURI().getPath();
        String launcherPath =
            RemoteFrameworkImpl.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI().getPath();
        return new String[]{ frameworkPath, launcherPath };
    }

    private RemoteFramework findRemoteFramework(int port, String rmiName )
    {
        RemoteFramework framework = null;
        Throwable reason = null;
        long startedTrying = System.currentTimeMillis();

        try
        {
            do
            {
                try
                {
                    Registry reg = LocateRegistry.getRegistry( port );
                    framework = (RemoteFramework) reg.lookup( rmiName );
                }
                catch ( Exception e )
                {
                    reason = e;
                }
            }
            while ( framework == null && ( System.currentTimeMillis() < startedTrying + TIMEOUT ) );
        }
        catch ( Exception e )
        {
            throw new TestContainerException( "cannot find remote framework in RMI registry", e );
        }
        if( framework == null )
        {
            throw new TestContainerException( "cannot find remote framework in RMI registry",
                reason );
        }
        return framework;

    }

    /**
     * Waits for the remote framework to shutdown and frees all resources.
     */
    public void join()
    {
        try
        {
            UnicastRemoteObject.unexportObject( registry, true );
            /*
             * NOTE: javaRunner.waitForExit() works for Equinox and Felix, but not for Knopflerfish,
             * need to investigate why. OTOH, it may be better to kill the process as we're doing
             * now, just to be on the safe side.
             */
            javaRunner.shutdown();
        }
        catch ( NoSuchObjectException exc )
        {
            throw new TestContainerException( exc );
        }
    }
}
