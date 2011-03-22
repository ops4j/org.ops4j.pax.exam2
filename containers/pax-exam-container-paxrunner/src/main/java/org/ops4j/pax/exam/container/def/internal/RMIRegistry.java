package org.ops4j.pax.exam.container.def.internal;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RMIRegistry {

    private static final Logger LOG = LoggerFactory.getLogger( RMIRegistry.class );

    final private String m_host;
    final private int m_port;

    public RMIRegistry( int port )
        throws UnknownHostException
    {
        m_port = port;
        m_host = InetAddress.getLocalHost().getHostName();
    }

    public RMIRegistry register()
        throws UnknownHostException, RemoteException
    {

        // try to locate one first:
        Registry reg = LocateRegistry.getRegistry( m_port );
        boolean valid = false;
        if( reg != null ) {
            // test:
            try {
                String[] objectsRemote = reg.list();

                for( String r : objectsRemote ) {
                    LOG.warn( "-- Remotely available already: " + r );
                }
                valid = true;
            } catch( Exception ex ) {
                //
            }
        }

        if( !valid ) {
            LOG.info( "Create new registry: " + m_port );
            LocateRegistry.createRegistry( m_port );
        }
        else {
            LOG.info( "Reuse registry: " + m_port );
        }
        return this;
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
