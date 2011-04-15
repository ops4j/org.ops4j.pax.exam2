/*
 * Copyright 2008,2009 Alin Dreghiciu.
 * Copyright 2008,2009 Toni Menzel
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

import static org.ops4j.pax.exam.OptionUtils.expand;
import static org.ops4j.pax.exam.OptionUtils.filter;
import static org.ops4j.pax.exam.container.def.util.Util.filterOne;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.ops4j.pax.exam.Customizer;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.container.def.options.FileScannerProvisionOption;
import org.ops4j.pax.exam.container.def.options.RawPaxRunnerOptionOption;
import org.ops4j.pax.exam.container.def.options.VMOption;
import org.ops4j.pax.exam.container.def.options.WorkingDirectoryOption;
import org.ops4j.pax.exam.container.def.util.Util;
import org.ops4j.pax.exam.options.AbstractDelegateProvisionOption;
import org.ops4j.pax.exam.options.BootClasspathLibraryOption;
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.BundleStartLevelOption;
import org.ops4j.pax.exam.options.DebugClassLoadingOption;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.exam.options.FrameworkStartLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.options.UrlReference;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption;
import org.ops4j.pax.exam.container.externalframework.options.ExternalFrameworkConfigurationOption;
import org.ops4j.pax.exam.container.externalframework.options.OptionParser;


/**
 * Utility methods for converting configuration options to Osgi framework option.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0 December 10, 2008
 */
public class DefaultOptionsParser implements OptionParser
{

	/**
     * Controls if one of the options set a Args Option manually.
     * Otherwise, defaultArguments will include a --noArgs flag to prevent
     * unintentional runner.args files being picked up by paxrunner.
     */
    private boolean argsSetManually = false;

    /**
     * Pax Runner compatible arguments parsed from input.
     */
    private final String[] m_parsedArgs;

    /**
     * There's a default location (users home) as well as an option for this.
     * Effective working folder is of great importance not only to the pax runner instance.
     *
     * To make things simple, we store this property redundantly here.
     * It is readable by a getter.
     */
    private File m_workingFolder;
    private Customizer[] m_customizers;

	private String javaHome;

	private Properties mConfig = new Properties();

	private ExternalFrameworkConfigurationOption<?> externalConfigurationOption;

	private ArrayList<String> vmOptionsRet;

	private String autoStartProperty;

	private String autoInstallProperty;

	private WorkingDirectoryOption workingDirOption;
	
	long m_RMItimeOut;

	private long m_startTimeOut;
	

	
	
    /**
     * Converts configuration options to Pax Runner arguments.
     *
     * @param options array of configuration options
     * @throws PlatformException 
     * @throws MalformedURLException 
     */
    public DefaultOptionsParser( ExternalFrameworkConfigurationOption<?> externalFrameworkConfigurationOption, 
    		String autoInstallProperty, 
    		String autoStartProperty, 
    		Option... options )
    {
    	this.autoStartProperty = autoStartProperty;
    	this.autoInstallProperty = autoInstallProperty;
    	//remove duplicate entries
    	HashSet<Option> hashsetOption = new HashSet<Option>(Arrays.asList(expand(options)));
    	options = (Option[]) hashsetOption.toArray(new Option[hashsetOption.size()]);
    	
        final List<String> arguments = new ArrayList<String>();
        m_customizers = filter( Customizer.class, options );

        externalConfigurationOption = externalFrameworkConfigurationOption;
        
        workingDirOption = filterOne(false, true, WorkingDirectoryOption.class, options );

        add( arguments, extractArguments( filter( RawPaxRunnerOptionOption.class, options ) ) );
        extractArguments(
                 filter( SystemPropertyOption.class, options ),
                 filter( VMOption.class, options ),
                 filter( BootDelegationOption.class, options ),
                 filterOne(false, true, FrameworkStartLevelOption.class, options),
                 filterOne(false, true, BundleStartLevelOption.class, options )
             );
        
        extractArguments( filter( BootClasspathLibraryOption.class, options ) ) ;
        add( arguments, extractArguments( filter( DebugClassLoadingOption.class, options ) ) );
        add( arguments, defaultArguments() );
        

        m_parsedArgs = arguments.toArray( new String[arguments.size()] );
        m_RMItimeOut = Util.getRMITimeout(options);
        m_startTimeOut = Util.getTestContainerStartTimeout(options);
    }

    /* (non-Javadoc)
	 * @see org.osp4j.pax.exam.container.externalframework.internal.OptionParser#getArguments()
	 */
    public String[] getArguments()
    {
        return m_parsedArgs;
    }

    /**
     * Adds a collection of arguments to a list of arguments by skipping null arguments.
     *
     * @param arguments      list to which the arguments should be added
     * @param argumentsToAdd arguments to be added (can be null or empty)
     */
    private void add( final List<String> arguments,
                      final Collection<String> argumentsToAdd )
    {
        if( argumentsToAdd != null && argumentsToAdd.size() > 0 )
        {
            arguments.addAll( argumentsToAdd );
        }
    }

    /**
     * Adds an argumentto a list of arguments by skipping null or empty arguments.
     *
     * @param arguments list to which the arguments should be added
     * @param argument  argument to be added (can be null or empty)
     */
    private void add( final List<String> arguments,
                      final String argument )
    {
        if( argument != null && argument.trim().length() > 0 )
        {
            arguments.add( argument );
        }
    }

    /**
     * Returns a collection of default Pax Runner arguments.
     *
     * @return collection of default arguments
     */
    private Collection<String> defaultArguments()
    {
        final List<String> arguments = new ArrayList<String>();
        arguments.add( "--noConsole" );
        arguments.add( "--noDownloadFeedback" );
        if( !argsSetManually )
        {
            arguments.add( "--noArgs" );
        }
        String folder = System.getProperty( "java.io.tmpdir" )
                        + "/paxexam_runner_"
                        + System.getProperty( "user.name" );

        arguments.add( "--workingDirectory=" + createWorkingDirectory( folder ).getAbsolutePath() );
        return arguments;
    }
    
    /**
     * Converts system properties and vm options into corresponding arguments (--vmOptions).
     *
     * @param systemProperties system property options
     * @param vmOptions        virtual machine options
     * @param bootDelegationOptions 
     * @param frameworkStartLevelOption 
     * @param provisionOptions 
     *
     * @return converted Pax Runner argument
     * @throws PlatformException 
     * @throws MalformedURLException 
     */
    private void extractArguments( final SystemPropertyOption[] systemProperties,
                                     final VMOption[] vmOptions, 
                                     BootDelegationOption[] bootDelegationOptions, 
                                     FrameworkStartLevelOption frameworkStartLevelOption, 
                                     BundleStartLevelOption bundleStartLevelOption)
    {
        vmOptionsRet = new ArrayList<String>();
        if( systemProperties != null && systemProperties.length > 0 )
        {
            for( SystemPropertyOption property : systemProperties )
            {
                if( property != null && property.getKey() != null && property.getKey().trim().length() > 0 )
                {
                    mConfig .put(property.getKey().trim(), property.getValue());
                    if ("java.home".equals(property.getKey())) {
                    	javaHome = property.getValue();
                    }
                }
            }
            for( SystemPropertyOption property : systemProperties )
            {
                if( property != null && property.getKey() != null && property.getKey().trim().length() > 0 )
                {
                	StringBuilder sb = new StringBuilder();
                	Map<String, String> cycleMap = new HashMap<String, String>();
					String value = Helper.substVars(property.getValue(), property.getKey(), cycleMap , mConfig);
                	sb.append( "-D" ).append( property.getKey() ).append( "=" ).append( value );
                    vmOptionsRet.add(sb.toString());
                }
            }
        }
        if( vmOptions != null && vmOptions.length > 0 )
        {
            for( VMOption vmOption : vmOptions )
            {
                if( vmOption != null && vmOption.getOption() != null && vmOption.getOption().trim().length() > 0 )
                {
                   vmOptionsRet.add( vmOption.getOption() );
                }
            }
        }
        if (bootDelegationOptions != null && bootDelegationOptions.length > 0) {
        	for (BootDelegationOption bdo : bootDelegationOptions) {
        		StringBuilder sb = new StringBuilder();
            	sb.append( "-Dorg.osgi.framework.bootdelegation=" ).append( bdo.getPackage() );
                vmOptionsRet.add(sb.toString());
			}
        }
        if (frameworkStartLevelOption != null) {
        	StringBuilder sb = new StringBuilder();
        	sb.append( "-Dorg.osgi.framework.startlevel.beginning=" ).append( frameworkStartLevelOption.getStartLevel() );
            vmOptionsRet.add(sb.toString());
        }
        

        if (bundleStartLevelOption != null) {
            StringBuilder sb = new StringBuilder();
        	sb.append( "-Dfelix.startlevel.bundle=" ).append( bundleStartLevelOption.getStartLevel() );
            vmOptionsRet.add(sb.toString());
        }
    }
    
    /* (non-Javadoc)
	 * @see org.osp4j.pax.exam.container.externalframework.internal.OptionParser#addBundleOption(int, org.ops4j.pax.exam.Option[])
	 */
    public void addBundleOption(int sl, Option[] options) throws MalformedURLException {
    	appendBundles(getWorkingFolder(), filter( ProvisionOption.class, options ), sl);
    }

    


    private List<String> extractArguments( RawPaxRunnerOptionOption[] paxrunnerOptions )
    {
        List<String> args = new ArrayList<String>();
        final boolean excludeDefaultRepositories = paxrunnerOptions.length > 0;

        if( paxrunnerOptions.length > 0 || excludeDefaultRepositories )
        {
            for( int i = 0; i < paxrunnerOptions.length; i++ )
            {

                args.add( paxrunnerOptions[ i ].getOption().trim() );
            }
        }
        return args;
    }



    /**
     * Converts boot classpath library options into corresponding arguments (--bcp/a, --bcp/p).
     *
     * @param libraries boot classpath libraries
     *
     * @return converted Pax Runner collection of arguments
     */
    private void extractArguments( final BootClasspathLibraryOption[] libraries )
    {
        for( BootClasspathLibraryOption library : libraries )
        {
        	try {
				Download.download(getWorkingFolder(), 
						new URL(library.getLibraryUrl().getURL()), true, false, false, true);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TestContainerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }
    }
	/**
     * Converts debug class loading option into corresponding argument (--debugClassLoading).
     *
     * @param debugClassLoadingOptions debug class loading options
     *
     * @return converted Pax Runner argument
     */
    private String extractArguments( final DebugClassLoadingOption[] debugClassLoadingOptions )
    {
        if( debugClassLoadingOptions.length > 0 )
        {
            return "--debugClassLoading";
        }
        else
        {
            return null;
        }
    }

    /**
     * Creates by default a working directory as ${java.io.tmpdir}/paxexam_runner_${user.name}.
     * Unless manualWorkingDirectory is set.
     *
     * @param workingDirectoryOption
     *
     * @return created working directory
     */
    private File createWorkingDirectory( String workingDirectoryOption )
    {
        final File workDir = new File( workingDirectoryOption );
        // create if not existent:
        if( !workDir.exists() )
        {
            workDir.mkdirs();
        }
        if( m_workingFolder == null )
        {
            m_workingFolder = workDir;
        }
        return workDir;
    }

    /* (non-Javadoc)
	 * @see org.osp4j.pax.exam.container.externalframework.internal.OptionParser#getWorkingFolder()
	 */
    public File getWorkingFolder()
    {
        return m_workingFolder;
    }

    /* (non-Javadoc)
	 * @see org.osp4j.pax.exam.container.externalframework.internal.OptionParser#getCustomizers()
	 */
    public Customizer[] getCustomizers()
    {
        return m_customizers;
    }
    
    /* (non-Javadoc)
	 * @see org.osp4j.pax.exam.container.externalframework.internal.OptionParser#getVmOptions()
	 */
    public String[] getVmOptions() {
		return (String[]) vmOptionsRet.toArray(new String[vmOptionsRet.size()]);
	}
	
    
	
	 /* (non-Javadoc)
	 * @see org.osp4j.pax.exam.container.externalframework.internal.OptionParser#getJavaHome()
	 */
    public String getJavaHome()
    {
    	if( javaHome == null )
        {
            javaHome = System.getProperty( "JAVA_HOME" );
            if( javaHome == null )
            {
                try
                {
                    javaHome = System.getenv( "JAVA_HOME" );
                }
                catch( Error e )
                {
                    // should only happen under Java 1.4.x as this method does not exist
                }
                if( javaHome == null )
                {
                    javaHome = System.getProperty( "java.home" );
                }
            }
        }
        return javaHome;
    }
    
    /**
     * Writes bundles to configuration file.
     * @param bundles 
     *
     * @param writer            a property writer
     * @param bundles           bundles to write
     * @param context           platform context
     * @param defaultStartlevel default start level for bundles. used if no start level is set on bundles.
     *
     * @throws java.net.MalformedURLException re-thrown from getting the file url
     * @throws org.ops4j.pax.runner.platform.PlatformException
     *                                        if one of the bundles does not have a file
     */
    private void appendBundles( File bundleDir, 
    		ProvisionOption<?>[] bundles, Integer defaultStartlevel ) throws MalformedURLException
    {
    	TreeMap<Integer, Map<String, NamedUrlProvition>> references = new TreeMap<Integer, Map<String, NamedUrlProvition>>();
        for( ProvisionOption<?> reference : bundles )
        {
            Integer sl = reference.getStartLevel();
        	if (sl == null)
        		sl = defaultStartlevel;
            
            if (reference instanceof AbstractDelegateProvisionOption) {
            	reference = (((AbstractDelegateProvisionOption<?>) reference).getDelegate());
            }
            if (reference instanceof MavenArtifactProvisionOption ||
            		reference instanceof WrappedUrlProvisionOption || 
            		reference instanceof UrlProvisionOption) {
            	add(bundleDir, references, reference, sl);
            }
            if (reference instanceof FileScannerProvisionOption) {
            	UrlReference urlRef = ((FileScannerProvisionOption)reference).getUrlReference();
            	add(bundleDir, references, new UrlProvisionOption(urlRef),sl);
            }
        }
    	
    	for(Integer k : references.keySet()) {
    		Map<String, NamedUrlProvition> list = references.get(k);
            final StringBuilder install = new StringBuilder()
                .append( "-D"+autoInstallProperty ).append(k).append("=");
            final StringBuilder start = new StringBuilder()
            .append( "-D"+autoStartProperty ).append(k).append("=");

            int l_install = install.length();
            int l_start = start.length();
            for (NamedUrlProvition provisionOption : list.values()) {
            	StringBuilder propertyName = null;
            	final boolean shouldStart = provisionOption.shouldStart();
                if( shouldStart )
                {
                    propertyName = start;
                }
                else
                {
                    propertyName = install;
                }
                
                System.out.println("bundle "+provisionOption.getURL()+" at "+k);
                propertyName.append("\"");
                propertyName.append(provisionOption.getURL());
                propertyName.append("|");
                propertyName.append(provisionOption.getName());
                propertyName.append("\" ");
			}
            
            if (l_install != install.length()) {
            	install.setLength(install.length()-1);
            	vmOptionsRet.add(install.toString());
            }
            
            if (l_start != start.length()) {
            	start.setLength(start.length()-1);
                vmOptionsRet.add(start.toString());
            }
            
        }
    }
	
	
	static void add(File bundleDir, TreeMap<Integer, Map<String, NamedUrlProvition>> references,
			ProvisionOption<?> reference, Integer sl) throws MalformedURLException {
		Map<String, NamedUrlProvition> list = references.get(sl);
		if (list == null) {
			list = new HashMap<String, NamedUrlProvition>();
			references.put(sl, list);
		}
		if (!list.containsKey(reference.getURL())) {
			File f = Download.download(bundleDir, new URL(reference.getURL()), true, false, false, true);
			list.put(reference.getURL(), new NamedUrlProvition(reference.getURL(),f.toURI().toString()));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.osp4j.pax.exam.container.externalframework.internal.OptionParser#getExternalConfigurationOption()
	 */
	public ExternalFrameworkConfigurationOption<?> getExternalConfigurationOption() {
		return externalConfigurationOption;
	}
	
	/* (non-Javadoc)
	 * @see org.osp4j.pax.exam.container.externalframework.internal.OptionParser#getFrameworkOption()
	 */
	public FrameworkOption getFrameworkOption() {
		return externalConfigurationOption;
	}
	
	/* (non-Javadoc)
	 * @see org.osp4j.pax.exam.container.externalframework.internal.OptionParser#getConfig()
	 */
	public Properties getConfig() {
		return mConfig;
	}
	
	/* (non-Javadoc)
	 * @see org.osp4j.pax.exam.container.externalframework.internal.OptionParser#getWorkingDirOption()
	 */
	public WorkingDirectoryOption getWorkingDirOption() {
		return workingDirOption;
	}

	public long getRMITimeout() {
		return m_RMItimeOut;
	}
	
	public long getStartTimeout() {
		return m_startTimeOut;
	}

}
