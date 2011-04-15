/*
 * Copyright 2006 Niclas Hedhman.
 * Copyright 2007 Alin Dreghiciu.
 * Copyright 2007 David Leangen.
 * Copyright 2011 Stephane Chomat.
 * 
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.container.externalframework.internal.runnerosgi;

import static org.ops4j.pax.runner.CommandLine.OPTION_HANDLERS;
import static org.ops4j.pax.runner.CommandLine.OPTION_SCANNERS;
import static org.ops4j.pax.runner.CommandLine.OPTION_SERVICES;

import java.io.File;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.LogLevel;
import org.ops4j.io.FileUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.CommandLine;
import org.ops4j.pax.runner.CommandLineImpl;
import org.ops4j.pax.runner.Configuration;
import org.ops4j.pax.runner.ConfigurationException;
import org.ops4j.pax.runner.OptionResolver;
import org.ops4j.pax.runner.OptionResolverImpl;
import org.ops4j.pax.scanner.ProvisionService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

/**
 * Main runner class. Does all the work.
 *
 * @author Alin Dreghiciu
 * @author Stephane Chomat
 * @since August 26, 2007
 */
public class RunnerStandeloneFramework
{

    /**
     * Logger.
     */
    private static Log LOGGER = createLogger();
    
    /**
     * Handler service configuration property name.
     */
    private static final String HANDLER_SERVICE = "handler.service";
    /**
     * Provision service configuration property name.
     */
    private static final String PROVISION_SERVICE = "provision.service";
    /**
     * Clean start configuration property name.
     */
    private static final String CLEAN_START = "clean";
    /**
     * Working directory configuration property name.
     */
    private static final String WORKING_DIRECTORY = "workingDirectory";

	private CommandLine commandLine;

	private Configuration configuration;

	private OptionResolver optionResolver;

     /**
     * Creates a new runner.
     */
    public RunnerStandeloneFramework(Properties config)
    {
    	super();
    	commandLine = new CommandLineImpl();
        
        configuration = new ConfigurationImpl(config, 
        		"classpath:META-INF/runner.properties");
       
        optionResolver = new OptionResolverImpl( commandLine, configuration );
        
    }

    public RunnerStandeloneFramework(CommandLine commandLine,
			Configuration config, OptionResolver resolver) {
    	NullArgumentException.validateNotNull( commandLine, "Command line" );
        NullArgumentException.validateNotNull( config, "Configuration" );
        NullArgumentException.validateNotNull( resolver, "PropertyResolver" );

    	this.commandLine = commandLine;
        
        configuration = config;
       
        optionResolver = resolver;
	}

	public CommandLine getCommandLine() {
		return commandLine;
	}
    public Configuration getConfiguration() {
		return configuration;
	}
    public OptionResolver getOptionResolver() {
		return optionResolver;
	}
   

    public static Log getLogger()
    {
        return LOGGER;
    }

    /**
     * Starts runner with a java runner.
     *
     * @param commandLine comand line to use
     * @param config      configuration to use
     * @param resolver    an option resolver
     * @param runner      java runner service
     */
    public void start(CreateActivator ca) throws BundleException
    {
    	LOGGER.info( commandLine );
        // cleanup if requested
        cleanup( optionResolver );
        // install aditional services
        installServices( configuration, optionResolver, ca);
        // install aditional handlers
        installHandlers(configuration, optionResolver, ca );
    }

    /**
     * Removes the working directory if option specified.
     *
     * @param resolver option resolver
     */
    public void cleanup( final OptionResolver resolver )
    {
        final boolean cleanStart = Boolean.valueOf( resolver.get( CLEAN_START ) );
        if( cleanStart )
        {
            final File workingDir = new File( resolver.getMandatory( WORKING_DIRECTORY ) );
            LOGGER.debug( "Removing working directory [" + workingDir.getAbsolutePath() + "]" );
            FileUtils.delete( workingDir );
        }
    }
    
    /**
     * Installs url handler service configured handlers.
     *
     * @param context the running context
     */
    public  void installHandlers(final Configuration config, 
    		final OptionResolver optionResolver, final CreateActivator ca )
    {
        LOGGER.debug( "Installing handlers" );
        final String option = optionResolver.get( OPTION_HANDLERS );
        if( option != null )
        {
            // first install each handler
            final String[] segments = option.split( "," );
            for( String segment : segments )
            {
                NullArgumentException.validateNotEmpty( segment, "Handler entry" );
                LOGGER.debug( "Handler [" + segment + "]" );
                final String activatorName = config.getProperty( segment );
                if( activatorName == null || activatorName.trim().length() == 0 )
                {
                    throw new ConfigurationException( "Handler [" + segment + "] is not supported" );
                }
                ca.createActivator( segment, activatorName );
            }
            // then install the handler service
            // maintain this order as in this way the bundle context will be easier to respond to getServiceListeners
            final String serviceActivatorName = config.getProperty( HANDLER_SERVICE );
            if( serviceActivatorName == null || serviceActivatorName.trim().length() == 0 )
            {
                throw new ConfigurationException( "Handler Service must be configured [" + HANDLER_SERVICE + "]" );
            }
            ca.createActivator( HANDLER_SERVICE, serviceActivatorName );
        }
    }

    /**
     * Installs provisioning service and configured scanners.
     *
     * @param context the running context
     *
     * @return installed provision service
     */
    public ProvisionService installScanners(final Configuration config, 
    		final OptionResolver optionResolver, final CreateActivator ca  )
    {
        LOGGER.debug( "Installing provisioning" );
        final String option = optionResolver.getMandatory( OPTION_SCANNERS );
        // first install a dummy start level service that will record the start level set by scanners
       
        // then install each scanner
        final String[] segments = option.split( "," );
        for( String segment : segments )
        {
            NullArgumentException.validateNotEmpty( segment, "Scanner entry" );
            LOGGER.debug( "Scanner [" + segment + "]" );
            final String activatorName = config.getProperty( segment );
            if( activatorName == null || activatorName.trim().length() == 0 )
            {
                throw new ConfigurationException( "Scanner [" + segment + "] is not supported" );
            }
            ca.createActivator( segment, activatorName );
        }
        // then install the provisioning service
        // maintain this order as in this way the bundle context will be easier to respond to getServiceListeners
        final String serviceActivatorName = config.getProperty( PROVISION_SERVICE );
        if( serviceActivatorName == null || serviceActivatorName.trim().length() == 0 )
        {
            throw new ConfigurationException( "Provision Service must be configured [" + PROVISION_SERVICE + "]" );
        }
        final BundleContext bundleContext = ca.createActivator( PROVISION_SERVICE, serviceActivatorName );
        // sanity check
        if( bundleContext == null )
        {
            throw new RuntimeException( "Could not create bundle context for provision service" );
        }
        final ServiceReference reference = bundleContext.getServiceReference( ProvisionService.class.getName() );
        if( reference == null )
        {
            throw new RuntimeException( "Could not resolve a provision service" );
        }
        return (ProvisionService) bundleContext.getService( reference );
    }

    /**
     * Installs additional services.
     *
     * @param context the running context
     */
    public void installServices(final Configuration config, final OptionResolver optionResolver, final CreateActivator ca )
    {
        LOGGER.debug( "Installing additional services" );
        final String option = optionResolver.get( OPTION_SERVICES );
        if( option != null )
        {
            final String[] segments = option.split( "," );
            for( String segment : segments )
            {
                NullArgumentException.validateNotEmpty( segment, "Service entry" );
                LOGGER.debug( "Installing service [" + segment + "]" );
                final String activatorName = config.getProperty( segment );
                if( activatorName == null || activatorName.trim().length() == 0 )
                {
                    throw new ConfigurationException( "Service [" + segment + "] is not supported" );
                }
                ca.createActivator( segment, activatorName );
            }
        }
    }

  

    /**
     * Creates the logger to use at the specified log level. The log level is only supported by the "special" JCL
     * implementation embedded into Pax Runner. In case that the JCL in the classpath in snot the embedded one it will
     * fallback to standard JCL usage.
     *
     * @param logLevel log level to use
     */
    private static Log createLogger( final LogLevel logLevel )
    {
        try
        {
            return LogFactory.getLog( RunnerStandeloneFramework.class );
        }
        catch( NoSuchMethodError ignore )
        {
            // fall back to standard JCL
        	return LogFactory.getLog( RunnerStandeloneFramework.class );
        }
    }

    /**
     * Creates a default logger at INFo level.
     */
    private static Log createLogger()
    {
        try
        {
        	return createLogger( LogLevel.INFO );
        }
        catch( NoClassDefFoundError ignore )
        {
            // fall back to standard JCL
        	return LogFactory.getLog( RunnerStandeloneFramework.class );
        }
    }

	
}
