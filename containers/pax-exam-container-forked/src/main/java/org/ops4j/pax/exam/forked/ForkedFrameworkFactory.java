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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ops4j.io.Pipe;
import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.TestContainerException;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps an OSGi {@link FrameworkFactory} to create and launch a framework in a forked Java
 * virtual machine running in a separate process.
 * <p>
 * The framework in the forked process can be controlled via a {@link RemoteFramework} interface.
 * 
 * @author Harald Wellmann
 *
 */
public class ForkedFrameworkFactory
{
    private static Logger LOG = LoggerFactory.getLogger( ForkedFrameworkFactory.class );

    private FrameworkFactory frameworkFactory;
    private File storage;
    private Registry registry;
    private Process process;

    private int port;

    private Pipe errPipe;

    private Pipe outPipe;

    private Pipe inPipe;

    /**
     * Creates a ForkedFrameworkFactory wrapping a given OSGi FrameworkFactory and a given
     * framework storage directory
     * @param frameworkFactory  OSGi framework factory
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
     * Forks a Java VM process running an OSGi framework and returns a {@link RemoteFramework} handle
     * to it.
     * <p>
     * TODO add VM properties
     * @param systemProperties    system properties for the forked Java VM
     * @param frameworkProperties framework properties for the remote framework
     * @return
     * @throws BundleException
     * @throws IOException
     * @throws InterruptedException
     * @throws NotBoundException
     */
    public RemoteFramework fork(Map<String, String> systemProperties, Map<String, Object> frameworkProperties) throws BundleException, IOException, InterruptedException, NotBoundException
    {
        
        FreePort freePort = new FreePort(21000, 21099);
        port = freePort.getPort();
        
        registry = LocateRegistry.createRegistry( port );

        
        doFork(systemProperties, frameworkProperties);
        return findRemoteFramework();
    }

    private void doFork(Map<String, String> systemProperties, Map<String, Object> frameworkProperties) throws IOException
    {
        ProcessBuilder processBuilder = new ProcessBuilder();
        List<String> args = new ArrayList<String>();
        args.add( getJavaProgram() );
        appendSystemProperties(args, systemProperties);
        
        // FIXME do not use hard-coded property names and values
        args.add( "-Dorg.ops4j.pax.exam.rmi.port=" + port );
        args.add( "-Dorg.ops4j.pax.exam.rmi.name=ExamRemoteFramework"); 
        
        args.add( "-cp" );
        args.add( buildClasspath() );
        
        // main class name
        args.add( RemoteFrameworkImpl.class.getName() );
        
        // program arguments: framework properties in key-value pairs
        args.add( FRAMEWORK_STORAGE );
        args.add( storage.getAbsolutePath() );
        appendFrameworkProperties(args, frameworkProperties);
        
        processBuilder.command( args );
        LOG.info("launching remote framework");
        LOG.info("arguments = {}" , args);
        process = processBuilder.start();
        errPipe = new Pipe( process.getErrorStream(), System.err ).start( "Error pipe" );
        outPipe = new Pipe( process.getInputStream(), System.out ).start( "Out pipe" );
        inPipe = new Pipe( process.getOutputStream(), System.in ).start( "In pipe" );
    }

    private void appendSystemProperties( List<String> args, Map<String, String> systemProperties )
    {
        for (Map.Entry<String, String> entry : systemProperties.entrySet()) {
            args.add( String.format("-D%s=%s", entry.getKey(), entry.getValue() ));
        }
    }

    private void appendFrameworkProperties( List<String> args,
            Map<String, Object> frameworkProperties )
    {
        for (Map.Entry<String, Object> entry : frameworkProperties.entrySet()) {
            args.add( entry.getKey() );
            args.add( entry.getValue().toString() );
        }
    }


    private String getJavaProgram()
    {
        String javaHome = System.getenv( "JAVA_HOME" );
        if (javaHome == null) {
            javaHome = System.getProperty( "java.home" );
        }
        File javaBin = new File(javaHome, "bin");
        File java = new File(javaBin, "java");
        return java.getAbsolutePath();
    }

    private String buildClasspath()
    {
        String frameworkPath = frameworkFactory.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        String launcherPath = RemoteFrameworkImpl.class.getProtectionDomain().getCodeSource().getLocation().toString();
        return frameworkPath + File.pathSeparator + launcherPath;
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
            while ( framework == null && ( System.currentTimeMillis() < startedTrying + 3000 ) );
        }
        catch ( Exception e )
        {
            throw new TestContainerException( "Cannot get the remote bundle context", e );
        }
        if( framework == null )
        {
            throw new TestContainerException( "Cannot get the remote bundle context", reason );
        }
        return framework;

    }

    /**
     * Waits for the remote framework to shutdown and frees all resources.
     * <p>
     * TODO error handling, timeouts etc.
     */
    public void join()
    {
        try
        {
            UnicastRemoteObject.unexportObject( registry, true );
            process.waitFor();
            errPipe.stop();
            outPipe.stop();
            inPipe.stop();
        }
        catch ( NoSuchObjectException exc )
        {
            throw new TestContainerException( exc );
        }
        catch ( InterruptedException exc )
        {
            throw new TestContainerException( exc );
        }
    }
}
