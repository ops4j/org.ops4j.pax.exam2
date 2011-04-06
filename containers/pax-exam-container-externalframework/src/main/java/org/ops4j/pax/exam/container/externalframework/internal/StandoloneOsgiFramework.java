/*
 * Copyright 2008.
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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.ServiceRegistry;
import org.apache.felix.framework.ServiceRegistry.ServiceRegistryCallbacks;
import org.apache.felix.framework.util.EventDispatcher;
import org.ops4j.io.FileUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.CommandLine;
import org.ops4j.pax.runner.CommandLineImpl;
import org.ops4j.pax.runner.Configuration;
import org.ops4j.pax.runner.ConfigurationException;
import org.ops4j.pax.runner.Context;
import org.ops4j.pax.runner.ContextImpl;
import org.ops4j.pax.runner.OptionResolver;
import org.ops4j.pax.runner.OptionResolverImpl;
import org.ops4j.pax.runner.osgi.RunnerBundleContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;

/**
 * A minimal version of Main runner class. Does url handler only.
 */
class StandoloneOsgiFramework implements Framework
{
	
	/**
	 * Configuration implementation that reads properties from a properties file.
	 *
	 */
	static public class ConfigurationImpl implements Configuration
	{

	    /**
	     * The loaded Properties.
	     */
	    private final Properties m_properties;

	    /**
	     * The protoc corresponding to classpath.
	     */
	    private static final String CLASSPATH_PROTOCOL = "classpath:";
	    
	    /**
	     * Creates the configuration by reading propertiesfrom an url.
	     * The url can start with classpath: and then the classpath is searched for the resource after ":".
	     *
	     * @param url url to load properties from
	     */
	    public ConfigurationImpl( Properties config, final String url )
	    {
	        NullArgumentException.validateNotEmpty( url, "Configuration url" );
	        InputStream inputStream;
	        try
	        {
	            if( url.startsWith( CLASSPATH_PROTOCOL ) )
	            {
	                String actualConfigFileName = url.split( ":" )[ 1 ];
	                NullArgumentException.validateNotEmpty( actualConfigFileName, "configuration file name" );
	                inputStream = getClass().getClassLoader().getResourceAsStream( actualConfigFileName );
	            }
	            else
	            {
	                inputStream = new URL( url ).openStream();
	            }
	            NullArgumentException.validateNotNull( inputStream, "Canot find url [" + url + "]" );
	            m_properties = new Properties();
	            m_properties.load( inputStream );
	            LOGGER.info( "Using config [" + url + "]" );
	        }
	        catch( IOException e )
	        {
	            throw new IllegalArgumentException( "Could not load configuration from url [" + url + "]", e );
	        }
	        m_properties.putAll(config);
	    }
	    
	    /**
	     * {@inheritDoc}
	     */
	    public String getProperty( final String key )
	    {
	        return m_properties.getProperty( key );
	    }

	    /**
	     * {&inheritDoc}
	     */
	    @SuppressWarnings( "unchecked" )
	    public String[] getPropertyNames( final String regex )
	    {
	        final List<String> result = new ArrayList<String>();
	        final Enumeration<String> propertyNames = (Enumeration<String>) m_properties.propertyNames();
	        while( propertyNames.hasMoreElements() )
	        {
	            String propertyName = propertyNames.nextElement();
	            if( propertyName.matches( regex ) )
	            {
	                result.add( propertyName );
	            }
	        }
	        return result.toArray( new String[result.size()] );
	    }

	}

	
	/**
     * Handlers option.
     */
    static final String OPTION_HANDLERS = "handlers";

    /**
     * Logger.
     */
    private static Log LOGGER;
    /**
     * Handler service configuration property name.
     */
    private static final String HANDLER_SERVICE = "handler.service";
    
    /**
     * Clean start configuration property name.
     */
    private static final String CLEAN_START = "clean";
    /**
     * Working directory configuration property name.
     */
    private static final String WORKING_DIRECTORY = "workingDirectory";

    Properties config;
	
    Context _context;

	private RunnerBundleContext _context0;
    /**
     * Creates a new runner.
     */
    public StandoloneOsgiFramework(Properties config)
    {
    	this.config = config;
        if( LOGGER == null )
        {
            createLogger();
        }
    }

    public static Log getLogger()
    {
        createLogger();
        return LOGGER;
    }

    /**
     * Starts runner with a java runner.
     *
     * @param commandLine comand line to use
     * @param config      configuration to use
     * @param resolver    an option resolver
     */
    public void start() throws BundleException
    {
    	final CommandLine commandLine = new CommandLineImpl();
        
        final Configuration configuration = new ConfigurationImpl(config, 
        		"classpath:META-INF/runner.properties");
       
        OptionResolverImpl resolver = new OptionResolverImpl( commandLine, configuration );
        
        _context = createContext( this, commandLine, configuration, resolver );
        _context0 =  new RunnerBundleContext( _context );
        
        LOGGER.info( commandLine );
        // cleanup if requested
        cleanup( resolver );
        // install aditional services
        //installServices( );
        // install aditional handlers
        installHandlers( );
        
    }

    /**
     * Removes the working directory if option specified.
     *
     * @param resolver option resolver
     */
    private void cleanup( final OptionResolver resolver )
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
     * Creates and initialize the context.
     *
     * @param commandLine comand line to use
     * @param config      configuration to use
     * @param resolver    an option resolver
     *
     * @return the created context
     */
    private Context createContext( final Framework framework, 
    		final CommandLine commandLine, 
    		final Configuration config, 
    		final OptionResolver resolver )
    {
        NullArgumentException.validateNotNull( commandLine, "Command line" );
        NullArgumentException.validateNotNull( config, "Configuration" );
        NullArgumentException.validateNotNull( resolver, "PropertyResolver" );

        Logger logger2 = new Logger();
        logger2.setLogLevel(Logger.LOG_DEBUG);
        final EventDispatcher dispatcher = EventDispatcher.start( logger2 );
        final ServiceRegistry serviceRegistry = new ServiceRegistry( logger2, new ServiceRegistryCallbacks(){

			public void serviceChanged(ServiceEvent event, Dictionary oldProps) {
				
				dispatcher.fireServiceEvent( event, oldProps, framework );
			}} );
        

        return new ContextImpl()
            .setCommandLine( commandLine )
            .setConfiguration( config )
            .setOptionResolver( resolver )
            .setServiceRegistry( serviceRegistry )
            .setEventDispatcher( dispatcher );
    }

    /**
     * Installs url handler service configured handlers.
     *
     * @param context the running context
     */
    private void installHandlers( )
    {
        LOGGER.debug( "Installing handlers" );
        final Configuration config = _context.getConfiguration();
        final String option = _context.getOptionResolver().get( OPTION_HANDLERS );
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
                createActivator( segment, activatorName);
            }
            // then install the handler service
            // maintain this order as in this way the bundle context will be easier to respond to getServiceListeners
            final String serviceActivatorName = config.getProperty( HANDLER_SERVICE );
            if( serviceActivatorName == null || serviceActivatorName.trim().length() == 0 )
            {
                throw new ConfigurationException( "Handler Service must be configured [" + HANDLER_SERVICE + "]" );
            }
            createActivator( HANDLER_SERVICE, serviceActivatorName);
        }
    }

    

    /**
     * Activator factory method.
     *
     * @param bundleName     name of the bundle to be created
     * @param activatorClazz class name of the activator
     * @param context        the running context
     *
     * @return activator related bundle context
     */
    public BundleContext createActivator( final String bundleName, final String activatorClazz )
    {
        try
        {
            final BundleActivator activator = (BundleActivator) Class.forName( activatorClazz ).newInstance();
            final BundleContext bundleContext = new RunnerBundleContext( _context );
            activator.start( bundleContext );
            return bundleContext;
        }
        catch( Exception e )
        {
            throw new RuntimeException( "Could not create [" + bundleName + "]", e );
        }
    }

    /**
     * Creates the logger to use at the specified log level. The log level is only supported by the "special" JCL
     * implementation embedded into Pax Runner. In case that the JCL in the classpath in snot the embedded one it will
     * fallback to standard JCL usage.
     *
     * @param logLevel log level to use
     */
    private static void createLogger( )
    {
        try
        {
            LOGGER = LogFactory.getLog( StandoloneOsgiFramework.class );
        }
        catch( NoSuchMethodError ignore )
        {
            // fall back to standard JCL
            LOGGER = LogFactory.getLog( StandoloneOsgiFramework.class );
        }
    }

	public Enumeration findEntries(String arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public BundleContext getBundleContext() {
		// TODO Auto-generated method stub
		return _context0;
	}

	public URL getEntry(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration getEntryPaths(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Dictionary getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	public Dictionary getHeaders(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getLastModified() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ServiceReference[] getRegisteredServices() {
		// TODO Auto-generated method stub
		return null;
	}

	public URL getResource(String arg0) {
		return _context0.getBundle().getResource(arg0);
	}

	public Enumeration getResources(String arg0) throws IOException {
		return _context0.getBundle().getResources(arg0);
	}

	public ServiceReference[] getServicesInUse() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map getSignerCertificates(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getState() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Version getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasPermission(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public Class loadClass(String arg0) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public long getBundleId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSymbolicName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void init() throws BundleException {
		// TODO Auto-generated method stub
		
	}


	public void start(int arg0) throws BundleException {
		// TODO Auto-generated method stub
		
	}

	public void stop() throws BundleException {
		// TODO Auto-generated method stub
		
	}

	public void stop(int arg0) throws BundleException {
		throw new UnsupportedOperationException();
	}

	public void uninstall() throws BundleException {
		throw new UnsupportedOperationException();
	}

	public void update() throws BundleException {
		// TODO Auto-generated method stub
		
	}

	public void update(InputStream arg0) throws BundleException {
		// TODO Auto-generated method stub
		
	}

	public FrameworkEvent waitForStop(long arg0) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}
}

