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
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.Test;
import org.osgi.framework.BundleException;

public class RemoteFrameworkImplTest
{

    @Test
    public void forkEquinox() throws BundleException, IOException, InterruptedException, NotBoundException
    { 
        Registry registry = LocateRegistry.getRegistry();
        RemoteFramework framework = (RemoteFramework) registry.lookup( "Exam" );
        framework.start();

        long commonsIoId = framework.installBundle( "file:/home/hwellmann/.m2/repository/commons-io/commons-io/2.1/commons-io-2.1.jar" );
        framework.startBundle( commonsIoId );

        
        framework.stop();
    }

}
