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

import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;

import java.io.File;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;

import org.ops4j.base.exec.DefaultJavaRunner;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.Constants;
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
    private File storage;
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
    public ForkedFrameworkFactory( FrameworkFactory frameworkFactory, File storage )
    {
        this.frameworkFactory = frameworkFactory;
        this.storage = storage;
    }

    public FrameworkFactory getFrameworkFactory()
    {
        return frameworkFactory;
    }

    public void setFrameworkFactory( FrameworkFactory frameworkFactory )
    {
        this.frameworkFactory = frameworkFactory;
    }

    public File getStorage()
    {
        return storage;
    }

    public void setStorage( File storage )
    {
        this.storage = storage;
    }

    /**
     * Forks a Java VM process running an OSGi framework and returns a {@link RemoteFramework}
     * handle to it.
     * <p>
     * TODO add VM properties
     * 
     * @param systemProperties system properties for the forked Java VM
     * @param frameworkProperties framework properties for the remote framework
     * @return
     * @throws BundleException
     * @throws IOException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    public RemoteFramework fork( Map<String, String> systemProperties,
            Map<String, Object> frameworkProperties ) throws BundleException, IOException,
        InterruptedException, NotBoundException
    {

        FreePort freePort = new FreePort( 21000, 21099 );
        port = freePort.getPort();

        registry = LocateRegistry.createRegistry( port );

        String[] vmOptions = buildSystemProperties( systemProperties );
        String[] args = buildFrameworkProperties( frameworkProperties );
        javaRunner = new DefaultJavaRunner( false );
        javaRunner.exec( vmOptions, buildClasspath(), RemoteFrameworkImpl.class.getName(),
            args, getJavaHome(), null );
        return findRemoteFramework();
    }

    private String[] buildSystemProperties( Map<String, String> systemProperties )
    {
        String[] vmOptions = new String[systemProperties.size() + 2];
        int i = 0;
        for ( Map.Entry<String, String> entry : systemProperties.entrySet() )
        {
            vmOptions[i++] = String.format( "-D%s=%s", entry.getKey(), entry.getValue() );
        }
        vmOptions[i++] = "-Dpax.swissbox.framework.rmi.port=" + port;
        vmOptions[i++] = "-Dpax.swissbox.framework.rmi.name=ExamRemoteFramework";
        return vmOptions;
    }

    private String[] buildFrameworkProperties( Map<String, Object> frameworkProperties )
    {
        String[] args = new String[2*frameworkProperties.size() + 2];
        int i = 0;
        args[i++] = FRAMEWORK_STORAGE;
        args[i++] = storage.getAbsolutePath();
        for ( Map.Entry<String, Object> entry : frameworkProperties.entrySet() )
        {

            args[i++] = entry.getKey();
            args[i++] = entry.getValue().toString();
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
    {
        String frameworkPath =
            frameworkFactory.getClass().getProtectionDomain().getCodeSource().getLocation()
                .toString();
        String launcherPath =
            RemoteFrameworkImpl.class.getProtectionDomain().getCodeSource().getLocation()
                .toString();
        return new String[]{ frameworkPath, launcherPath };
    }

    private RemoteFramework findRemoteFramework()
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
                    framework = (RemoteFramework) reg.lookup( "ExamRemoteFramework" );
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
            javaRunner.waitForExit();
        }
        catch ( NoSuchObjectException exc )
        {
            throw new TestContainerException( exc );
        }
    }
}
