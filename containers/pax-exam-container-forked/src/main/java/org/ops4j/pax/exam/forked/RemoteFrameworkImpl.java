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

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Implements the {@link RemoteFramework} interface by instantiating a local {@link Framework},
 * exporting it via an RMI registry and delegating all remote calls to the local framework.
 * 
 * @author Harald Wellmann
 */
public class RemoteFrameworkImpl implements RemoteFramework
{    
    private Framework framework;
    private Registry registry;
    private String name;
    
    public RemoteFrameworkImpl(Map<String, String> frameworkProperties) throws RemoteException, AlreadyBoundException, BundleException
    {
        FrameworkFactory frameworkFactory = findFrameworkFactory();
        this.framework = frameworkFactory.newFramework( frameworkProperties );

        export();
    }

    private void export() throws RemoteException, AccessException
    {
        String port = System.getProperty( "org.ops4j.pax.exam.rmi.port", "1099" );
        name = System.getProperty( "org.ops4j.pax.exam.rmi.name");
        registry = LocateRegistry.getRegistry( Integer.parseInt( port ) );
        URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        URL location2 = Bundle.class.getProtectionDomain().getCodeSource().getLocation();
        System.setProperty("java.rmi.server.codebase", location.toString() + " " +location2);
        Remote remote = UnicastRemoteObject.exportObject( this, 0 );
        registry.rebind( name, remote );
    }

    public void init() throws RemoteException, BundleException
    {
        framework.init();
    }

    public void start() throws RemoteException, BundleException
    {
        framework.start();
    }

    public void stop() throws RemoteException, BundleException
    {
        framework.stop();
        try
        {
            registry.unbind( name );
        }
        catch ( NotBoundException exc )
        {
            throw new IllegalStateException( exc );
        }
        UnicastRemoteObject.unexportObject( this, true );        
    }

    public long installBundle( String bundleUrl ) throws RemoteException, BundleException
    {
        Bundle bundle = framework.getBundleContext().installBundle( bundleUrl );
        return bundle.getBundleId();
    }

    public long installBundle( String bundleLocation, byte[] bundleData ) throws RemoteException,
        BundleException
    {
        Bundle bundle = framework.getBundleContext().installBundle( bundleLocation, new ByteArrayInputStream( bundleData ) );
        return bundle.getBundleId();
    }

    public void startBundle( long bundleId ) throws RemoteException, BundleException
    {
        framework.getBundleContext().getBundle( bundleId ).start();
    }

    public void stopBundle( long bundleId ) throws RemoteException, BundleException
    {
        framework.getBundleContext().getBundle( bundleId ).stop();
    }

    public void setBundleStartLevel( long bundleId, int startLevel ) throws RemoteException,
        BundleException
    {
        BundleContext bc = framework.getBundleContext();
        StartLevel sl = getService( bc, StartLevel.class, 3000 );
        Bundle bundle = bc.getBundle( bundleId );
        sl.setBundleStartLevel( bundle, startLevel );
    }

    public void uninstallBundle( long id ) throws RemoteException, BundleException
    {
        framework.getBundleContext().getBundle( id ).uninstall();
    }

    
    public FrameworkFactory findFrameworkFactory() {
        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load( FrameworkFactory.class );
        FrameworkFactory factory = loader.iterator().next();
        return factory;        
    }
    
    private static Map<String, String> buildFrameworkProperties( String[] args )
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
    
    public void callService( String filter, String methodName ) throws RemoteException, BundleException
    {
        try
        {
            System.out.println("acquiring service " + filter);
            BundleContext bc = framework.getBundleContext();
            Filter parsedFilter = bc.createFilter( filter );
            ServiceTracker<?, ?> tracker = new ServiceTracker<Object, Object>(bc, parsedFilter, null);
            tracker.open();
            tracker.waitForService( 30000 );
            Object service = tracker.getService();
            if (service == null) {
                throw new IllegalStateException("could not acquire ProbeInvoker service");
            }
            Class<? extends Object> klass = service.getClass();
            Method method;
            try
            {
                method = klass.getMethod( methodName, Object[].class );
                System.out.println("method = " + method);
                method.invoke( service, (Object) new Object[] {} );
            }
            catch ( NoSuchMethodException e )
            {
                method = klass.getMethod( methodName);
                System.out.println("method = " + method);
                method.invoke( service );
            }            
            tracker.close();
        }
        catch ( InvalidSyntaxException exc )
        {
            throw new IllegalStateException(exc);
        }
        catch ( InterruptedException exc )
        {
            throw new IllegalStateException(exc);
        }
        catch ( SecurityException exc )
        {
            throw new IllegalStateException(exc);
        }
        catch ( NoSuchMethodException exc )
        {
            throw new IllegalStateException(exc);
        }
        catch ( IllegalArgumentException exc )
        {
            throw new IllegalStateException(exc);
        }
        catch ( IllegalAccessException exc )
        {
            throw new IllegalStateException(exc);
        }
        catch ( InvocationTargetException exc )
        {
            throw new IllegalStateException(exc);
        }        
    }

    @SuppressWarnings( "unchecked" )
    public static <T> T getService( BundleContext context, Class<T> klass, int timeout )
    {
        ServiceTracker<T, T> tracker = new ServiceTracker<T, T>( context, klass.getName(), null );
        try
        {
            tracker.open();
            Object svc = tracker.waitForService( timeout );
            if( svc == null )
            {
                throw new RuntimeException( "gave up waiting for service " + klass.getName() );
            }
            return (T) svc;
        }
        catch ( InterruptedException exc )
        {
            throw new RuntimeException( exc );
        }
        finally
        {
            tracker.close();
        }
    }    

    public void setFrameworkStartLevel( int startLevel )
    {
        BundleContext bc = framework.getBundleContext();
        StartLevel sl = getService( bc, StartLevel.class, 3000 );
        sl.setStartLevel( startLevel );
    }

    public void waitForState( long bundleId, int state, long timeoutInMillis )
        throws RemoteException, BundleException
    {
        throw new UnsupportedOperationException( "not yet implemented" );
    }
    
    public static void main( String[] args ) throws RemoteException, AlreadyBoundException, BundleException, InterruptedException
    {
        Map<String,String> props = buildFrameworkProperties(args);
        RemoteFrameworkImpl impl = new RemoteFrameworkImpl( props );
        impl.start();
    }
}
