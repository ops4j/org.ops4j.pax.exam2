/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.container.def.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.net.FreePort;

/**
 * Graceful RMI registry creation/reuse.
 * Tries to reuse an existing one but is fine with creating one on another port.
 */
public class RMIRegistry {

    private static final Logger LOG = LoggerFactory.getLogger( RMIRegistry.class );

    private final Integer m_defaultPort;

    private static final int UNSELECTED = -1;

    final private String m_host;
    private Integer m_port = UNSELECTED;
    private Integer m_altMin;
    private Integer m_altTo;

    public RMIRegistry( Integer defaultPort, Integer alternativeRangeFrom, Integer alternativeRangeTo )

    {
        try {
            m_host = InetAddress.getLocalHost().getHostName();
        } catch( UnknownHostException e ) {
            throw new IllegalStateException( "Cannot select localhost. That usually not a good sign for networking.." );
        }
        m_defaultPort = defaultPort;
        m_altMin = alternativeRangeFrom;
        m_altTo = alternativeRangeTo;
    }

    /**
     * This will make sure a registry exists and is valid m_port.
     * If its not available or does not work for some reason, it will select another port.
     * This should really not happen usually. But it can.
     *
     * @return this for fluent API. Or IllegalStateException if a port has not been detected successfully.
     */
    public synchronized RMIRegistry selectGracefully()
    {
        if( ( m_port = select( m_defaultPort ) ) == UNSELECTED ) {
            FreePort alternativePort = new FreePort( m_altMin, m_altTo );
            if( ( m_port = select( alternativePort.getPort() ) ) == UNSELECTED ) {
                throw new IllegalStateException( "No port found for RMI at all. Thats.. not. good. at. all." );
            }
        }

        return this;
    }

    /**
     * This contains basically two paths:
     * 1. check if the given port already is valid rmi registry. Use that one if possible
     * 2. make a new one at that port otherwise. Must also be validated.
     *
     * @param port to select.
     *
     * @return input port if successful or UNSELECTED
     */
    private Integer select( int port )
    {
        if( reuseRegistry( port ) || createNewRegistry( port ) ) {
            LOG.info( "Registry on " + port );
            return port;
        }
        // fail
        return UNSELECTED;

    }

    private boolean createNewRegistry( int port )
    {
        try {
            Registry registry = LocateRegistry.createRegistry( port );
            LOG.info( "Try new Registry on " + port );

            return verifyRegistry( registry );

        } catch( RemoteException e ) {
            //
        }

        return false;
    }

    private boolean reuseRegistry( int port )
    {
        Registry reg = null;
        try {
            reg = LocateRegistry.getRegistry( port );
            return verifyRegistry( reg );

        } catch( RemoteException e ) {
            //
        }
        return false;

    }

    private boolean verifyRegistry( Registry reg )
    {
        if( reg != null ) {
            // test:
            try {
                String[] objectsRemote = reg.list();

                for( String r : objectsRemote ) {
                    LOG.info( "-- Remotely available already: " + r );
                }
                return true;

            } catch( Exception ex ) {
                // exception? then its not a fine registry.
            }
        }
        return false;
    }

    public String getHost()
    {
        return m_host;
    }

    public int getPort()
    {
        return m_port;
    }
}
