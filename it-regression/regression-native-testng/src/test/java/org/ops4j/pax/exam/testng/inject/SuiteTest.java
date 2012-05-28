package org.ops4j.pax.exam.testng.inject;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.ops4j.net.FreePort;
import org.ops4j.pax.exam.regression.pde.Notifier;
import org.testng.TestNG;
import org.testng.annotations.Test;

public class SuiteTest implements Notifier, Remote
{
    private List<String> messages = new ArrayList<String>();
    
    @Override
    public void send( String msg ) throws RemoteException
    {
        System.out.println("received: " + msg);
        messages.add( msg );
    }
    
    @Test
    public void runSuite()
    {
        FreePort freePort = new FreePort(20000, 21000);
        int rmiPort = freePort.getPort();
        System.setProperty("pax.exam.regression.rmi", Integer.toString( rmiPort ));
        try
        {
            Registry registry = LocateRegistry.createRegistry( rmiPort );
            Remote remote = UnicastRemoteObject.exportObject( this, 0 );
            registry.rebind( "PaxExamNotifier", remote );
            
            TestNG testNG = new TestNG();
            testNG.setTestClasses( new Class[] {FilterTest.class, InjectTest.class} );
            testNG.run();
            
            registry.unbind ("PaxExamNotifier");
            UnicastRemoteObject.unexportObject( this, true );
            UnicastRemoteObject.unexportObject( registry, true );
            
            assertThat( messages.size(), is( 2));
        }
        catch ( AccessException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( RemoteException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch ( NotBoundException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
