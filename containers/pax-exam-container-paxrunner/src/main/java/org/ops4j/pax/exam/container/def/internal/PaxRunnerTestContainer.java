/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.ops4j.pax.exam.container.def.internal;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.CompositeCustomizer;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestTarget;
import org.ops4j.pax.exam.container.def.options.BundleScannerProvisionOption;
import org.ops4j.pax.exam.container.def.options.Scanner;
import org.ops4j.pax.exam.container.remote.RBCRemoteTarget;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.TestContainerStartTimeoutOption;
import org.ops4j.pax.exam.rbc.Constants;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.ops4j.pax.exam.TimeoutException;
import org.ops4j.pax.runner.Run;
import org.ops4j.pax.runner.handler.internal.URLUtils;
import org.ops4j.pax.runner.platform.DefaultJavaRunner;
import org.ops4j.store.Handle;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;

import static org.ops4j.pax.exam.Constants.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

/**
 * {@link TestContainer} implementation using Pax Runner.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 09, 2008
 */
public class PaxRunnerTestContainer
    implements TestContainer
{

    private static final Log LOG = LogFactory.getLog( PaxRunnerTestContainer.class );

    /**
     * System bundle id.
     */
    private static final int SYSTEM_BUNDLE = 0;

    /**
     * Java runner to be used to start up Pax Runner.
     */
    private final DefaultJavaRunner m_javaRunner;

    /**
     * regression container start timeout.
     */
    private final long m_startTimeout;

    private final Store<InputStream> m_store;

    private final Map<String, Handle> m_cache;

    private final CompositeCustomizer m_customizers;

    /**
     *
     */
    private TestContainerSemaphore m_semaphore;

    private boolean m_started = false;

    /**
     * Underlying Test Target
     */
    private RBCRemoteTarget m_target;
    private File m_workingFolder;
    private String[] m_paxRunnerCmdArgs;

    /**
     * Constructor.
     *
     * @param javaRunner java runner to be used to start up Pax Runner
     */
    public PaxRunnerTestContainer( final DefaultJavaRunner javaRunner, String[] paxRunnerCmdArgs, File workingFolder, long startTimout, RBCRemoteTarget target, Customizer[] customizer )
    {
        m_javaRunner = javaRunner;
        m_startTimeout = startTimout;
        m_workingFolder = workingFolder;
        m_paxRunnerCmdArgs = paxRunnerCmdArgs;

        m_target = target; //new RBCRemoteTarget( options );

        m_customizers = new CompositeCustomizer( customizer );
        m_store = StoreFactory.defaultStore();
        m_cache = new HashMap<String, Handle>();
    }

    public OptionDescription getOptionDescription()
    {
        return null;
    }

    /**
     * {@inheritDoc} Delegates to {@link RemoteBundleContextClient}.
     */
    public void setBundleStartLevel( final long bundleId, final int startLevel )
        throws TestContainerException
    {
        m_target.getClientRBC().setBundleStartLevel( bundleId, startLevel );
    }

    /**
     * {@inheritDoc}
     */
    public TestContainer start()
    {
        LOG.info( "Starting up the regression container (Pax Runner " + Info.getPaxRunnerVersion() + " )" );
        /**
         */
        m_semaphore = new TestContainerSemaphore( m_workingFolder );
        // this makes sure the system is ready to launch a new instance.
        // this could fail, based on what acquire actually checks.
        // this also creates some persistent mark that will be removed by m_semaphore.release()
        if( !m_semaphore.acquire() )
        {
            // here we can react.
            // Prompt user with the fact that there might be another instance running.
            if( !FileUtils.delete( m_workingFolder ) )
            {
                throw new RuntimeException( "There might be another instance of Pax Exam running. Have a look at "
                                            + m_semaphore.getLockFile().getAbsolutePath()
                );
            }
        }

        long startedAt = System.currentTimeMillis();
        URLUtils.resetURLStreamHandlerFactory();
        Run.start( m_javaRunner, m_paxRunnerCmdArgs );
        LOG.info( "Test container (Pax Runner " + Info.getPaxRunnerVersion() + ") started in "
                  + ( System.currentTimeMillis() - startedAt ) + " millis"
        );

        LOG.info( "Wait for regression container to finish its initialization "
                  + ( m_startTimeout == WAIT_FOREVER ? "without timing out" : "for " + m_startTimeout + " millis" )
        );
        try
        {
            waitForState( SYSTEM_BUNDLE, Bundle.ACTIVE, m_startTimeout );
        }
        catch( TimeoutException e )
        {
            throw new TimeoutException( "Test container did not initialize in the expected time of " + m_startTimeout
                                        + " millis"
            );
        }
        m_customizers.customizeEnvironment( m_workingFolder );

        m_started = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public TestContainer stop()
    {
        LOG.info( "Shutting down the regression container (Pax Runner)" );
        try
        {
            if( m_started )
            {
                RemoteBundleContextClient remoteBundleContextClient = m_target.getClientRBC();
                if( remoteBundleContextClient != null )
                {
                    remoteBundleContextClient.stop();
                }
                if( m_javaRunner != null )
                {
                    m_javaRunner.waitForExit();
                }
            }
        }
        finally
        {
            m_semaphore.release();
            m_started = false;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void waitForState( final long bundleId, final int state, final long timeoutInMillis )
        throws TimeoutException
    {
        m_target.getClientRBC().waitForState( bundleId, state, timeoutInMillis );
    }

    /**
     * Return the options required by this container implementation.
     *
     * @return local options
     */

    /**
     * Determine the timeout while starting the osgi framework.<br/>
     * Timeout is dermined by first looking for a {@link TestContainerStartTimeoutOption} in the user options. If not
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

    @Override
    public String toString()
    {
        return "PaxRunnerTestContainer{}";
    }

    private Handle storeAndGetData( String bundleUrl )
    {
        try
        {
            Handle handle = m_cache.get( bundleUrl );
            if( handle == null )
            {
                // new, so getStream, customize and store
                URL url = new URL( bundleUrl );
                InputStream in = url.openStream();

                in = m_customizers.customizeTestProbe( in );

                // store in and overwrite handle
                handle = m_store.store( in );
                m_cache.put( bundleUrl, handle );

            }
            return handle;

        }
        catch( Exception e )
        {
            LOG.error( "problem in preparing probe. ", e );
        }
        return null;
    }

    public <T> T getService( Class<T> serviceType, String filter, long timeoutInMillis )
        throws TestContainerException
    {
        return m_target.getService( serviceType, filter, timeoutInMillis );
    }

    public long install( InputStream stream )
    {
        return m_target.install( stream );
    }

    public void cleanup()
    {
        m_target.cleanup();
    }
}
