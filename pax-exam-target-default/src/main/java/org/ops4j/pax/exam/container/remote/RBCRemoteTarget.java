/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.container.remote;

import java.io.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.remote.options.RBCLookupTimeoutOption;
import org.ops4j.pax.exam.container.remote.options.RBCPortOption;
import org.ops4j.pax.exam.options.TestContainerStartTimeoutOption;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.ops4j.pax.exam.spi.container.TestContainerException;
import org.ops4j.pax.exam.spi.container.TestTarget;

import static org.ops4j.pax.exam.OptionUtils.*;

/**
 * @author Toni Menzel
 * @since Jan 25, 2010
 */
public class RBCRemoteTarget implements TestTarget
{

    private static final Log LOG = LogFactory.getLog( RBCRemoteTarget.class );

    private RemoteBundleContextClient m_remoteBundleContextClient;

    private static final int AMOUNT_OF_PORTS_TO_CHECK = 100;

    private static final Integer DEFAULTPORT = 1412;

    /**
     * @param options user configuration
     */
    public RBCRemoteTarget( Option[] options )

    {
        m_remoteBundleContextClient =
            new RemoteBundleContextClient( getPort( options ), getRMITimeout( options ) );
    }

    /**
     * This access is kind of sneaky. Need to improve here. Usually this kind of raw access should not be allowed.
     *
     * @return underlying access
     */
    public RemoteBundleContextClient getClientRBC()
    {
        return m_remoteBundleContextClient;
    }

    public <T> T getService( Class<T> serviceType, String filter, long timeoutInMillis )
        throws TestContainerException
    {
        LOG.debug( "Lookup a [" + serviceType.getName() + "]" );
        return m_remoteBundleContextClient.getService( serviceType, filter, timeoutInMillis );
    }

    public long installBundle( InputStream probe )
        throws TestContainerException
    {
        LOG.debug( "Preparing and Installing bundle (from stream ).." );

        long id = 0;
        id = m_remoteBundleContextClient.installBundle( probe );
        LOG.debug( "Installed bundle (from stream)" + " as ID: " + id );
        return id;
    }

    public void uninstallBundle( long id )
        throws TestContainerException
    {
        LOG.debug( "Preparing and Uninstalling bundle " + id );

        m_remoteBundleContextClient.uninstallBundle( id );
    }

    private Integer getPort( Option[] options )
    {
        final RBCPortOption[] ports = filter( RBCPortOption.class, options );
        if( ports.length > 0 )
        {
            return ports[ 0 ].getPort();
        }
        else
        {
            return DEFAULTPORT;
        }

    }

    /**
     * Determine the rmi lookup timeout.<br/>
     * Timeout is dermined by first looking for a {@link RBCLookupTimeoutOption} in the user options. If not specified a
     * default is used.
     *
     * @param options user options
     *
     * @return rmi lookup timeout
     */
    private static long getRMITimeout( final Option... options )
    {
        final RBCLookupTimeoutOption[] timeoutOptions = filter( RBCLookupTimeoutOption.class, options );
        if( timeoutOptions.length > 0 )
        {
            return timeoutOptions[ 0 ].getTimeout();
        }
        return getTestContainerStartTimeout( options );
    }

    /**
     * Determine the timeout while starting the osgi framework.<br/>
     * Timeout is dermined by first looking for a {@link org.ops4j.pax.exam.options.TestContainerStartTimeoutOption} in the user options. If not
     * specified a default is used.
     *
     * @param options user options
     *
     * @return rmi lookup timeout
     */
    private static long getTestContainerStartTimeout( final Option... options )
    {
        final TestContainerStartTimeoutOption[] timeoutOptions =
            filter( TestContainerStartTimeoutOption.class, options );
        if( timeoutOptions.length > 0 )
        {
            return timeoutOptions[ 0 ].getTimeout();
        }
        return CoreOptions.waitForFrameworkStartup().getTimeout();
    }
}
