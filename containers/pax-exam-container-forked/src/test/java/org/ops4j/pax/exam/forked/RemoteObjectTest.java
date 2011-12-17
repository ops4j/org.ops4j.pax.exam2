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

import java.net.URL;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.junit.Test;

public class RemoteObjectTest
{
    
    public static interface HelloService extends Remote
    {
        String getMessage() throws RemoteException;
    }
    
    public static class HelloServiceImpl implements HelloService, Remote {

        public String getMessage()
        {
            return "Hello Pax!";
        }        
    }
    
    @Test
    public void exportAndUnexport() throws RemoteException, AlreadyBoundException {
        URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
        System.setProperty("java.rmi.server.codebase", location.toString());
        HelloServiceImpl hello = new HelloServiceImpl();
        Registry registry = LocateRegistry.getRegistry();
        UnicastRemoteObject.exportObject( hello, 0 );
        registry.rebind( "hello", hello );
    }
}
