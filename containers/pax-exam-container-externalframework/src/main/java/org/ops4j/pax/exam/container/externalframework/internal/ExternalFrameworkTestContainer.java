/*
 * Copyright 2011 Stephane Chomat.
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
package org.ops4j.pax.exam.container.externalframework.internal;

import static org.ops4j.pax.exam.Constants.START_LEVEL_SYSTEM_BUNDLES;
import static org.ops4j.pax.exam.Constants.WAIT_FOREVER;
import static org.ops4j.pax.exam.CoreOptions.bootDelegationPackage;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.OptionUtils.expand;
import static org.ops4j.pax.exam.OptionUtils.filter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.CompositeCustomizer;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TimeoutException;
import org.ops4j.pax.exam.container.def.util.RMIRegistry;
import org.ops4j.pax.exam.container.def.util.TestContainerSemaphore;
import org.ops4j.pax.exam.container.remote.RBCRemoteTarget;
import org.ops4j.pax.exam.container.remote.options.RBCLookupTimeoutOption;
import org.ops4j.pax.exam.options.TestContainerStartTimeoutOption;
import org.ops4j.pax.exam.rbc.Constants;
import org.ops4j.pax.exam.rbc.client.RemoteBundleContextClient;
import org.ops4j.pax.runner.platform.DefaultJavaRunner;
import org.ops4j.pax.runner.platform.JavaRunner;
import org.ops4j.pax.runner.platform.PlatformException;
import org.ops4j.pax.runner.platform.StoppableJavaRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.ops4j.pax.exam.container.externalframework.options.ExternalFrameworkConfigurationOption;
import org.ops4j.pax.exam.container.externalframework.options.OptionParser;

/**
 * {@link TestContainer} implementation using url connector with pax runner, start a karaf configuration or other.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 09, 2008
 */
class ExternalFrameworkTestContainer
    implements TestContainer
{

    private static final Log LOG = LogFactory.getLog( ExternalFrameworkTestContainer.class );

    /**
     * Remote bundle context client.
     */
    private RBCRemoteTarget m_target;

    /**
     * Java runner to be used to start up Pax Runner.
     */
    private JavaRunner m_javaRunner;

    /**
     * Pax Runner arguments, out of options.
     */
    private  OptionParser m_arguments;

    /**
     * test container start timeout.
     */
    private final long m_startTimeout;

    private  CompositeCustomizer m_customizers;
    
    
    /**
     *
     */
    private TestContainerSemaphore m_semaphore;

    private boolean m_started = false;

	private Option[] m_options;

	private RMIRegistry m_rmiRegistry;

	private ExternalFrameworkConfigurationOption<?> m_config;

    /**
     * Constructor.
     * @param rmiRegistry 
     * 
     * @param javaRunner java runner to be used to start up Pax Runner
     * @param options user startup options
     */
    ExternalFrameworkTestContainer(RMIRegistry rmiRegistry, 
    		ExternalFrameworkConfigurationOption<?> config, final Option... options )
    {
       
    	LOG.info( "New ExternalTestContainer " );
    	m_startTimeout = getTestContainerStartTimeout( options );
        m_options = options;
        m_rmiRegistry = rmiRegistry;
        m_config = config;
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
     * @return 
     */
    public TestContainer start()
    {
        LOG.info( "Starting up the test container (Pax Runner " + Info.getPaxRunnerVersion() + " )" );
        try {
	        String name = UUID.randomUUID().toString();
	        Option[] args = combine( m_options, systemProperty( Constants.RMI_NAME_PROPERTY ).value( name ) );
	        
			m_target =
	        	new RBCRemoteTarget( name, m_rmiRegistry.getPort(), getRMITimeout( args ) );
	        m_options = expand( 
	        		combine( 
	        		combine(m_options, 
	        				systemProperty( Constants.RMI_NAME_PROPERTY ).value( name ) ), 
	        				localOptions() ) );
	        
	        /**
	         */
	        m_arguments = m_config.parseOption(m_options);
	        
	        m_customizers = new CompositeCustomizer( m_arguments.getCustomizers() );
	        
	        m_semaphore = new TestContainerSemaphore( m_arguments.getWorkingFolder() );
	        // this makes sure the system is ready to launch a new instance.
	        // this could fail, based on what acquire actually checks.
	        // this also creates some persistent mark that will be removed by m_semaphore.release()
	        if ( !m_semaphore.acquire() )
	        {
	            // here we can react.
	            // Prompt user with the fact that there might be another instance running.
	            if ( !FileUtils.delete( m_arguments.getWorkingFolder() ) )
	            {
	                throw new RuntimeException( "There might be another instance of Pax Exam running. Have a look at "
	                    + m_semaphore.getLockFile().getAbsolutePath() );
	            }
	        }
	        
	
		     // customize environment
		     m_customizers.customizeEnvironment( m_arguments.getWorkingFolder() );
	
	      //add url handler to mvn: handler.
	        StandoloneOsgiFramework framework = new StandoloneOsgiFramework(m_arguments.getConfig());
			framework.start();
	        
	        m_arguments.addBundleOption(60, m_options);
			
	        startOsgiFramework();
	
	        LOG.info( "Wait for test container to finish its initialization "
	            + ( m_startTimeout == WAIT_FOREVER ? "without timing out" : "for " + m_startTimeout + " millis" ) );
	        try
	        {
	            waitForState( m_arguments.getExternalConfigurationOption().getSystemBundleId(), Bundle.ACTIVE, m_startTimeout );
	        }
	        catch ( TimeoutException e )
	        {
	            throw new TimeoutException( "Test container did not initialize in the expected time of " + m_startTimeout
	                + " millis" );
	        }
	        m_started = true;
        } catch( Throwable e ) {
            throw new RuntimeException( "Problem starting container", e );
        }
        return this;
    }

    private void startOsgiFramework() {
    	m_javaRunner = getJavaRunner();
        Runnable run = new Runnable() {
            public void run() {
                String[] arguments = m_arguments.getArguments();
                
				try {
                    long startedAt = System.currentTimeMillis();
                    m_config.run(
                    		m_arguments.getJavaHome(), 
                    		m_arguments.getWorkingFolder(), 
                    		m_javaRunner, 
                    		m_arguments.getVmOptions(), 
                    		arguments, 
                    		m_arguments.getConfig());
                    LOG.info( "Test container (Pax Runner " + Info.getPaxRunnerVersion() + ") started in "
                             + ( System.currentTimeMillis() - startedAt ) + " millis" );
                } catch (PlatformException ex) {
                    Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException("Cannot run "+ExternalFrameworkTestContainer.toString(m_arguments.getJavaHome(), m_arguments.getWorkingFolder(), m_arguments.getVmOptions(), arguments, m_arguments.getConfig()), ex);
                } catch (NoSuchArchiverException ex) {
                    Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException("Cannot run "+ExternalFrameworkTestContainer.toString(m_arguments.getJavaHome(), m_arguments.getWorkingFolder(), m_arguments.getVmOptions(), arguments, m_arguments.getConfig()), ex);
                } catch (ArchiverException ex) {
                    Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException("Cannot run "+ExternalFrameworkTestContainer.toString(m_arguments.getJavaHome(), m_arguments.getWorkingFolder(), m_arguments.getVmOptions(), arguments, m_arguments.getConfig()), ex);
                } catch (IOException ex) {
                    Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
                    throw new RuntimeException("Cannot run "+ExternalFrameworkTestContainer.toString(m_arguments.getJavaHome(), m_arguments.getWorkingFolder(), m_arguments.getVmOptions(), arguments, m_arguments.getConfig()), ex);
                }
            }
        };
        Thread t = new Thread(run,"Run process");
        t.start();
	}

	protected JavaRunner getJavaRunner() {
		String javaRunnerClass =  m_arguments.getConfig().getProperty("org.apache.karaf.testing.javarunner", "org.ops4j.pax.runner.platform.DefaultJavaRunner");
        JavaRunner javaRunner;
		if ("DefaultJavaRunner".equals(javaRunnerClass) || "org.ops4j.pax.runner.platform.DefaultJavaRunner".equals(javaRunnerClass)) {
            javaRunner = new DefaultJavaRunner( false );
        } else {
            try {
                javaRunner = (JavaRunner) Class.forName(javaRunnerClass).newInstance();
            } catch (InstantiationException ex) {
                Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException("Cannot instanciate "+javaRunnerClass, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException("Cannot instanciate "+javaRunnerClass, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ExternalFrameworkTestContainer.class.getName()).log(Level.SEVERE, null, ex);
                throw new RuntimeException("Cannot instanciate "+javaRunnerClass, ex);
            }
        }
        return javaRunner;
	}

    /**
     * {@inheritDoc}
	 * @return 
     */
    public TestContainer stop()
    {
    	LOG.info( "Shutting down the test container (Pax Runner)" );
        try {
            if( m_started ) {
                cleanup();
                RemoteBundleContextClient remoteBundleContextClient = m_target.getClientRBC();
                if( remoteBundleContextClient != null ) {
                    remoteBundleContextClient.stop();

                }
                if( m_javaRunner != null && m_javaRunner instanceof StoppableJavaRunner) {
                	((StoppableJavaRunner) m_javaRunner).shutdown();
                }

            }
            else {
                throw new RuntimeException( "Container never came up" );
            }
        } finally {

            m_started = false;
            m_target = null;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public void waitForState( final long bundleId, final int state, final long timeoutInMillis )
        throws TimeoutException
    {
        try {
            m_target.getClientRBC().waitForState( bundleId, state, timeoutInMillis );
        } catch( BundleException e ) {
            LOG.error( "Bundle Exception", e );
        } catch( RemoteException e ) {
            throw new TimeoutException( "Remote Exception while waitForState", e );
        }
    }

    /**
     * Return the options required by this container implementation.
     *
     * @return local options
     */
    private Option[] localOptions()
    {
        return new Option[] {
        // remote bundle context bundle
            mavenBundle().groupId( "org.ops4j.pax.exam" ).artifactId( "pax-exam-container-rbc" ).version(
                                                                                                          Info.getPaxExamVersion() ).update(
                                                                                                                                             Info.isPaxExamSnapshotVersion() ).startLevel(
                                                                                                                                                                                           START_LEVEL_SYSTEM_BUNDLES ),
            // rmi communication port
            systemProperty( Constants.RMI_PORT_PROPERTY ).value( Integer.toString(m_rmiRegistry.getPort()) ),
            // boot delegation for sun.*. This seems only necessary in Knopflerfish version > 2.0.0
            bootDelegationPackage( "sun.*" ) };
    }

    
    /**
     * Determine the rmi lookup timeout.<br/>
     * Timeout is dermined by first looking for a {@link RBCLookupTimeoutOption} in the user options. If not specified a
     * default is used.
     * 
     * @param options user options
     * @return rmi lookup timeout
     */
    private static long getRMITimeout( final Option... options )
    {
        final RBCLookupTimeoutOption[] timeoutOptions = filter( RBCLookupTimeoutOption.class, options );
        if ( timeoutOptions.length > 0 )
        {
            return timeoutOptions[0].getTimeout();
        }
        return getTestContainerStartTimeout( options );
    }

    /**
     * Determine the timeout while starting the osgi framework.<br/>
     * Timeout is dermined by first looking for a {@link TestContainerStartTimeoutOption} in the user options. If not
     * specified a default is used.
     * 
     * @param options user options
     * @return rmi lookup timeout
     */
    private static long getTestContainerStartTimeout( final Option... options )
    {
        final TestContainerStartTimeoutOption[] timeoutOptions =
            filter( TestContainerStartTimeoutOption.class, options );
        if ( timeoutOptions.length > 0 )
        {
            return timeoutOptions[0].getTimeout();
        }
        return CoreOptions.waitForFrameworkStartup().getTimeout();
    }

    @Override
    public String toString()
    {
        return "PaxRunnerTestContainer{}";
    }

    static private String toString(String javaHome, File workingFolder, String[] vmOptions, String[] arguments, Properties config) {
        StringBuilder sb = new StringBuilder();
        sb.append("cd ").append(toCygwin(workingFolder)).append("\n");
        for (Object str : config.keySet()) {
            sb.append(str).append("=").append(config.get(str)).append("\n");
        }
        sb.append("JAVA_EXEC=").append(toCygwin(new File(javaHome))).append("/bin/java\n");
        sb.append("RUN_JAVA_OPTS=\\\n");
        for (String vmOpt : vmOptions) {
            if (vmOpt.startsWith("-D")) {
                int index = vmOpt.indexOf("=");
                if (index != -1) {
                    index++;
                    String key = vmOpt.substring(0, index);
                    String value = vmOpt.substring(index);
                    sb.append(" ").append(key).append("\"").append(value.replaceAll("\"", "\\\"")).append("\"\\\n");
                }
            }
            sb.append("\\n").append(vmOpt).append("\\\n");
        }
        sb.setLength(sb.length()-2);
        sb.append("\n");
        sb.append("$JAVA_EXEC $RUN_JAVA_OPTS $MAIN_CLASS");
        for (String arg : arguments) {
            sb.append(" ").append(arg);
        }
        sb.append("\n");
        
        return sb.toString();
    }

    static private String toCygwin(File pathFile) {
        String path = pathFile.getPath();
        path = path.replaceAll("\\\\", "/");
        if (pathFile.isAbsolute()) {
            return "/cygdrive/"+path.substring(0,1)+"/"+path.substring(3);
        }
        return path;
    }

    

	public void call(TestAddress address) throws ClassNotFoundException,
			InvocationTargetException, InstantiationException,
			IllegalAccessException {
		m_target.call( address );
	}

	public long install(InputStream stream) {
		return m_target.install( stream );
	}

	public void cleanup() {
        // unwind installed bundles basically.
        m_target.cleanup();
	}

}
