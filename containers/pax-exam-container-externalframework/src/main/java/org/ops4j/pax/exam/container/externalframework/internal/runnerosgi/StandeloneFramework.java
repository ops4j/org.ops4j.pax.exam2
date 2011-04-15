/*
 * Copyright 2006 Niclas Hedhman.
 * Copyright 2007 Alin Dreghiciu.
 * Copyright 2007 David Leangen.
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


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.LogLevel;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.ServiceRegistry;
import org.apache.felix.framework.util.EventDispatcher;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.*;
import org.ops4j.pax.runner.osgi.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.launch.Framework;

/**
 * Main runner class. Does all the work.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public class StandeloneFramework implements Framework, CreateActivator
{

    /**
     * Logger.
     */
    private static Log LOGGER;

    protected Context _context;

	private RunnerBundleContext _context0;

	private OptionResolver optionResolver;
    /**
     * Creates a new runner.
     */
    public StandeloneFramework(OptionResolver optionResolver)
    {
    	this.optionResolver = optionResolver;
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
     * @param runner      java runner service
     */
    public void start() throws BundleException
    {
    	_context = createContext( );
    	_context.setOptionResolver(optionResolver);
        _context0 =  new RunnerBundleContext( _context );
        
        RunnerStartLevel.install( _context.getServiceRegistry() );
    }

   public Context getContext() {
	   return _context;
   }

   /**
    * Creates and initialize the context.
    *
    * @return the created context
    */
   protected Context createContext( )
   {
       final ServiceRegistry serviceRegistry = new ServiceRegistry( null );
       final EventDispatcher dispatcher = EventDispatcher.start( new Logger( Logger.LOG_DEBUG ) );
       serviceRegistry.addServiceListener( new ServiceListener()
       {
           public void serviceChanged( ServiceEvent event )
           {
               dispatcher.fireServiceEvent( event );
           }
       }
       );

       return new ContextImpl()
           .setServiceRegistry( serviceRegistry )
           .setEventDispatcher( dispatcher );
   }
    
    /* (non-Javadoc)
	 * @see org.ops4j.pax.runner.osgi.felix.CreateActivator#createActivator(java.lang.String, java.lang.String)
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
    private static void createLogger( final LogLevel logLevel )
    {
        try
        {
            LOGGER = LogFactory.getLog( StandeloneFramework.class );
        }
        catch( NoSuchMethodError ignore )
        {
            // fall back to standard JCL
            LOGGER = LogFactory.getLog( StandeloneFramework.class );
        }
    }

    /**
     * Creates a default logger at INFo level.
     */
    private static void createLogger()
    {
        try
        {
            createLogger( LogLevel.INFO );
        }
        catch( NoClassDefFoundError ignore )
        {
            // fall back to standard JCL
            LOGGER = LogFactory.getLog( StandeloneFramework.class );
        }
    }

	public int getState() {
		return _context0.getBundle().getState();
	}

	public Dictionary getHeaders() {
		return _context0.getBundle().getHeaders();
	}

	public ServiceReference[] getRegisteredServices() {
		// TODO Auto-generated method stub
		return null;
	}

	public ServiceReference[] getServicesInUse() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasPermission(Object permission) {
		// TODO Auto-generated method stub
		return false;
	}

	public URL getResource(String name) {
		return _context0.getBundle().getResource(name);
	}

	public Dictionary getHeaders(String locale) {
		return _context0.getBundle().getHeaders(locale);
	}

	public Class loadClass(String name) throws ClassNotFoundException {
		return _context0.getBundle().loadClass(name);
	}

	public Enumeration getResources(String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Enumeration getEntryPaths(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public URL getEntry(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getLastModified() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Enumeration findEntries(String path, String filePattern,
			boolean recurse) {
		// TODO Auto-generated method stub
		return null;
	}

	public BundleContext getBundleContext() {
		return _context0;
	}

	public Map getSignerCertificates(int signersType) {
		// TODO Auto-generated method stub
		return null;
	}

	public Version getVersion() {
		return null; //_context0.getBundle().getVersion();
	}

	public void init() throws BundleException {
	}

	public FrameworkEvent waitForStop(long timeout) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	public void start(int options) throws BundleException {
		start();
	}

	public void stop() throws BundleException {
		// TODO Auto-generated method stub
		
	}

	public void stop(int options) throws BundleException {
		stop();
	}

	public void uninstall() throws BundleException {
		
	}

	public void update() throws BundleException {
		
	}

	public void update(InputStream in) throws BundleException {
		// TODO Auto-generated method stub
		
	}

	public long getBundleId() {
		return 0;
	}

	public String getLocation() {
		return null;
	}

	public String getSymbolicName() {
		return "System";
	}
}
