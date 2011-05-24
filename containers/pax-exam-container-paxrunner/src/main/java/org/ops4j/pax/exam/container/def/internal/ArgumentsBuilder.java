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
package org.ops4j.pax.exam.container.def.internal;

import static org.ops4j.pax.exam.OptionUtils.filter;
import static org.ops4j.pax.exam.CoreOptions.scanBundle;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.options.BootClasspathLibraryOption;
import org.ops4j.pax.exam.options.BootDelegationOption;
import org.ops4j.pax.exam.options.BundleStartLevelOption;
import org.ops4j.pax.exam.options.CustomFrameworkOption;
import org.ops4j.pax.exam.options.DebugClassLoadingOption;
import org.ops4j.pax.exam.options.FrameworkOption;
import org.ops4j.pax.exam.options.FrameworkStartLevelOption;
import org.ops4j.pax.exam.options.MavenPluginGeneratedConfigOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.SystemPackageOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.extra.AutoWrapOption;
import org.ops4j.pax.exam.options.extra.CleanCachesOption;
import org.ops4j.pax.exam.options.extra.ExcludeDefaultRepositoriesOption;
import org.ops4j.pax.exam.options.extra.LocalRepositoryOption;
import org.ops4j.pax.exam.options.extra.ProfileOption;
import org.ops4j.pax.exam.options.extra.RawPaxRunnerOptionOption;
import org.ops4j.pax.exam.options.extra.RepositoryOptionImpl;
import org.ops4j.pax.exam.options.extra.Scanner;
import org.ops4j.pax.exam.options.extra.VMOption;

/**
 * Utility methods for converting configuration options to Pax Runner arguments.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0 December 10, 2008
 */
class ArgumentsBuilder {

    /**
     * Controls if one of the options set a Args Option manually.
     * Otherwise, defaultArguments will include a --noArgs flag to prevent
     * unintentional runner.args files being picked up by paxrunner.
     */
    private boolean argsSetManually = false;
    
    final private List<String> m_paxrunneArguments;


    /**
     * Converts configuration options to Pax Runner arguments.
     * @param selectedFramework 
     *
     * @param options array of configuration options
     */
    ArgumentsBuilder( ExamSystem system, FrameworkOption selectedFramework )
        throws IOException
    {
        m_paxrunneArguments = new ArrayList<String>();
        
    	add( m_paxrunneArguments, extractArguments( system.getOptions( RawPaxRunnerOptionOption.class ) ) );
        
        add( m_paxrunneArguments, extractArguments( system.getOptions( MavenPluginGeneratedConfigOption.class ) ) );

        add( m_paxrunneArguments, extractArguments( selectedFramework ) );
        add( m_paxrunneArguments, extractArguments( system.getOptions( ProfileOption.class ) ) );
        add( m_paxrunneArguments, extractArguments( system.getOptions( BootDelegationOption.class ) ) );
        add( m_paxrunneArguments, extractArguments( system.getOptions( SystemPackageOption.class ) ) );
        add( m_paxrunneArguments, extractArguments( system.getOptions( ProvisionOption.class ) ) );
        add( m_paxrunneArguments,
             extractArguments(
            		 system.getOptions( RepositoryOptionImpl.class ),
            		 system.getOptions( ExcludeDefaultRepositoriesOption.class )
             )
        );
        add( m_paxrunneArguments, extractArguments( system.getOptions( AutoWrapOption.class ) ) );
        add( m_paxrunneArguments, extractArguments( system.getOptions( CleanCachesOption.class ) ) );
        add( m_paxrunneArguments, extractArguments( system.getOptions( LocalRepositoryOption.class ) ) );
        add( m_paxrunneArguments, extractArguments( system.getOptions( FrameworkStartLevelOption.class ) ) );
        add( m_paxrunneArguments, extractArguments( system.getOptions( BundleStartLevelOption.class ) ) );
        
        add( m_paxrunneArguments,
             extractArguments( 
            		 system.getOptions( SystemPropertyOption.class ),
            		 system.getOptions( VMOption.class )
             )
        );
        add( m_paxrunneArguments, extractArguments( filter( BootClasspathLibraryOption.class ) ) );
        add( m_paxrunneArguments, extractArguments( filter( DebugClassLoadingOption.class ) ) );
        add( m_paxrunneArguments, defaultArguments( system ) );
      }

    public String[] get() {
    	return m_paxrunneArguments.toArray( new String[ m_paxrunneArguments.size() ] );
    }
    
    /**
     * @param m_selectedFramework 
     * @param m_frameworkName 
     * @return Pax Runner arguments
     * @throws IOException 
     */
    public static String[] build ( ExamSystem system, FrameworkOption selectedFramework ) throws IOException
    {
    	// add UUID
    	return new ArgumentsBuilder(system,selectedFramework).get();
    }

    /**
     * Wrap provision options that are not already scanner provision bundles with a {@link org.ops4j.pax.exam.options.extra.BundleScannerProvisionOption}
     * in order to force update.
     *
     * @param options options to be wrapped (can be null or an empty array)
     *
     * @return eventual wrapped bundles
     */
    private List<ProvisionOption> wrap( ProvisionOption[] options )
    {
        final List<ProvisionOption> processed = new ArrayList<ProvisionOption>();
        for( final ProvisionOption provisionOption : options ) {
            if( !( provisionOption instanceof Scanner ) ) {
                processed.add( scanBundle( provisionOption ).start( provisionOption.shouldStart() ).startLevel(
                    provisionOption.getStartLevel()
                ).update(
                    provisionOption.shouldUpdate()
                )
                );
            }
            else {
                processed.add( provisionOption );
            }
        }
        return processed;
                
          
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
        if( argumentsToAdd != null && argumentsToAdd.size() > 0 ) {
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
        if( argument != null && argument.trim().length() > 0 ) {
            arguments.add( argument );
        }
    }

    /**
     * Returns a collection of default Pax Runner arguments.
     *
     * @return collection of default arguments
     * @throws java.io.IOException problems 
     */
    private Collection<String> defaultArguments(ExamSystem system)
        throws IOException
    {
        final List<String> arguments = new ArrayList<String>();
        arguments.add( "--noConsole" );
        arguments.add( "--noDownloadFeedback" );
        arguments.add( "--log=warn" );

        if( !argsSetManually ) {
            arguments.add( "--noArgs" );
        }

        arguments.add( "--workingDirectory=" + system.getTempFolder() );
        return arguments;
    }

    /**
     * Converts framework options into corresponding arguments (--platform, --version).
     *
     * @param frameworks framework options
     *
     * @return converted Pax Runner collection of arguments
     *
     * @throws IllegalArgumentException - If there are more then one framework options
     */
    private Collection<String> extractArguments( final FrameworkOption framework )
    {
        final List<String> arguments = new ArrayList<String>();
            if( framework instanceof CustomFrameworkOption ) {
                String basePlatform = ( (CustomFrameworkOption) framework ).getBasePlatform();
                if( basePlatform != null && basePlatform.trim().length() > 0 ) {
                    arguments.add( "--platform=" + basePlatform );
                }
                arguments.add( "--definitionURL=" + ( (CustomFrameworkOption) framework ).getDefinitionURL() );
            }
            else {
                arguments.add( "--platform=" + framework.getName() );
                final String version = framework.getVersion();
                if( version != null && version.trim().length() > 0 ) {
                    arguments.add( "--version=" + version );
                }
            }
        
        return arguments;
    }

    /**
     * @return all arguments that have been recognized by OptionResolvers as PaxRunner arguments
     */
    private Collection<String> extractArguments(
        MavenPluginGeneratedConfigOption[] mavenPluginGeneratedConfigOption )
    {
        final List<String> arguments = new ArrayList<String>();
        for( MavenPluginGeneratedConfigOption arg : mavenPluginGeneratedConfigOption ) {
            URL url = arg.getURL();
            arguments.add( "--args=" + url.toExternalForm() );
        }
        argsSetManually = true;
        return arguments;
    }

    /**
     * Converts provision options into corresponding arguments (provision urls).
     *
     * @param bundles provision options
     *
     * @return converted Pax Runner collection of arguments
     */
    private Collection<String> extractArguments( final ProvisionOption[] bundles )
    {
        final List<String> arguments = new ArrayList<String>();
        for( ProvisionOption bundle : wrap(bundles) ) {
            arguments.add( bundle.getURL() );
        }
        return arguments;
    }

    /**
     * Converts profile options into corresponding arguments (--profiles).
     *
     * @param profiles profile options
     *
     * @return converted Pax Runner collection of arguments
     */
    private static String extractArguments( final ProfileOption[] profiles )
    {
        final StringBuilder argument = new StringBuilder();
        if( profiles != null && profiles.length > 0 ) {
            for( ProfileOption profile : profiles ) {
                if( profile != null && profile.getProfile() != null && profile.getProfile().length() > 0 ) {
                    if( argument.length() == 0 ) {
                        argument.append( "--profiles=" );
                    }
                    else {
                        argument.append( "," );
                    }
                    argument.append( profile.getProfile() );
                }
            }
        }
        return argument.toString();
    }

    /**
     * Converts boot delegation packages options into corresponding arguments (--bootDelegation).
     *
     * @param packages boot delegation package options
     *
     * @return converted Pax Runner collection of arguments
     */
    private static String extractArguments( final BootDelegationOption[] packages )
    {
        final StringBuilder argument = new StringBuilder();
        if( packages != null && packages.length > 0 ) {
            for( BootDelegationOption pkg : packages ) {
                if( pkg != null && pkg.getPackage() != null && pkg.getPackage().length() > 0 ) {
                    if( argument.length() == 0 ) {
                        argument.append( "--bootDelegation=" );
                    }
                    else {
                        argument.append( "," );
                    }
                    argument.append( pkg.getPackage() );
                }
            }
        }
        return argument.toString();
    }

    /**
     * Converts system package options into corresponding arguments (--systemPackages).
     *
     * @param packages system package options
     *
     * @return converted Pax Runner collection of arguments
     */
    private String extractArguments( final SystemPackageOption[] packages )
    {
        final StringBuilder argument = new StringBuilder();
        if( packages != null && packages.length > 0 ) {
            for( SystemPackageOption pkg : packages ) {
                if( pkg != null && pkg.getPackage() != null && pkg.getPackage().length() > 0 ) {
                    if( argument.length() == 0 ) {
                        argument.append( "--systemPackages=" );
                    }
                    else {
                        argument.append( "," );
                    }
                    argument.append( pkg.getPackage() );
                }
            }
        }
        return argument.toString();
    }

    /**
     * Converts system properties and vm options into corresponding arguments (--vmOptions).
     *
     * @param systemProperties system property options
     * @param vmOptions        virtual machine options
     *
     * @return converted Pax Runner argument
     */
    private String extractArguments( final SystemPropertyOption[] systemProperties,
                                     final VMOption[] vmOptions )
    {
        final StringBuilder argument = new StringBuilder();
        if( systemProperties != null && systemProperties.length > 0 ) {
            for( SystemPropertyOption property : systemProperties ) {
                if( property != null && property.getKey() != null && property.getKey().trim().length() > 0 ) {
                    if( argument.length() > 0 ) {
                        argument.append( " " );
                    }
                    argument.append( "-D" ).append( property.getKey() ).append( "=" ).append( property.getValue() );
                }
            }
        }
        if( vmOptions != null && vmOptions.length > 0 ) {
            for( VMOption vmOption : vmOptions ) {
                if( vmOption != null && vmOption.getOption() != null && vmOption.getOption().trim().length() > 0 ) {
                    if( argument.length() > 0 ) {
                        argument.append( " " );
                    }
                    argument.append( vmOption.getOption() );
                }
            }
        }
        if( argument.length() > 0 ) {
            argument.insert( 0, "--vmOptions=" );
        }
        return argument.toString();
    }

    /**
     * Converts repository options into corresponding arguments (--repositories).
     *
     * @param repositoriesOptions repository options to be converted
     * @param excludeDefaultRepositoriesOptions
     *                            if array not empty the default list of maven repos should be excluded
     *
     * @return converted Pax Runner argument
     */
    private String extractArguments( RepositoryOptionImpl[] repositoriesOptions,
                                     ExcludeDefaultRepositoriesOption[] excludeDefaultRepositoriesOptions )
    {
        final StringBuilder argument = new StringBuilder();
        final boolean excludeDefaultRepositories = excludeDefaultRepositoriesOptions.length > 0;

        if( repositoriesOptions.length > 0 || excludeDefaultRepositories ) {
            argument.append( "--repositories=" );
            if( !excludeDefaultRepositories ) {
                argument.append( "+" );
            }
            for( int i = 0; i < repositoriesOptions.length; i++ ) {
                argument.append( repositoriesOptions[ i ].getRepository() );
                if( i + 1 < repositoriesOptions.length ) {
                    argument.append( "," );
                }
            }
        }
        return argument.toString();
    }

    private String extractArguments( AutoWrapOption[] autoWrapOptions )
    {
        if( autoWrapOptions.length > 0 ) {
            return "--autoWrap";
        }
        else {
            return null;
        }
    }

    private String extractArguments( CleanCachesOption[] cleanCachesOption )
    {
        if( cleanCachesOption.length > 0 ) {
            return "--clean";
        }
        else {
            return null;
        }
    }

    private String extractArguments( LocalRepositoryOption[] localRepositoryOptions )
    {
        if( localRepositoryOptions != null && localRepositoryOptions.length > 0 ) {
            LocalRepositoryOption local = localRepositoryOptions[ 0 ];
            return "--localRepository=" + local.getLocalRepositoryPath();

        }
        return null;
    }

    private List<String> extractArguments( RawPaxRunnerOptionOption[] paxrunnerOptions )
    {
        List<String> args = new ArrayList<String>();
        final boolean excludeDefaultRepositories = paxrunnerOptions.length > 0;

        if( paxrunnerOptions.length > 0 || excludeDefaultRepositories ) {
            for( int i = 0; i < paxrunnerOptions.length; i++ ) {

                args.add( paxrunnerOptions[ i ].getOption().trim() );
            }
        }
        return args;
    }

    /**
     * Converts framework start level option into coresponding argument (--startLevel).
     *
     * @param startLevels framework start levels options
     *
     * @return converted Pax Runner collection of arguments
     *
     * @throws IllegalArgumentException - If there is more then one framework start level option
     */
    private Collection<String> extractArguments( final FrameworkStartLevelOption[] startLevels )
    {
        final List<String> arguments = new ArrayList<String>();
        if( startLevels.length > 1 ) {
            throw new IllegalArgumentException( "Configuration cannot contain more then one framework start level" );
        }
        if( startLevels.length > 0 ) {
            arguments.add( "--startLevel=" + startLevels[ 0 ].getStartLevel() );
        }
        return arguments;
    }

    /**
     * Converts initial bundle start level option into coresponding argument (--bundleStartLevel).
     *
     * @param startLevels initial bundle start levels options
     *
     * @return converted Pax Runner collection of arguments
     *
     * @throws IllegalArgumentException - If there is more then one initial bundle start level option
     */
    private Collection<String> extractArguments( final BundleStartLevelOption[] startLevels )
    {
        final List<String> arguments = new ArrayList<String>();
        if( startLevels.length > 1 ) {
            throw new IllegalArgumentException( "Configuration cannot contain more then one bundle start level" );
        }
        if( startLevels.length > 0 ) {
            arguments.add( "--bundleStartLevel=" + startLevels[ 0 ].getStartLevel() );
        }
        return arguments;
    }

    /**
     * Converts boot classpath library options into corresponding arguments (--bcp/a, --bcp/p).
     *
     * @param libraries boot classpath libraries
     *
     * @return converted Pax Runner collection of arguments
     */
    private Collection<String> extractArguments( final BootClasspathLibraryOption[] libraries )
    {
        final List<String> arguments = new ArrayList<String>();
        for( BootClasspathLibraryOption library : libraries ) {
            if( library.isBeforeFramework() ) {
                arguments.add( "--bcp/p=" + library.getLibraryUrl().getURL() );
            }
            else {
                arguments.add( "--bcp/a=" + library.getLibraryUrl().getURL() );
            }
        }
        return arguments;
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
        if( debugClassLoadingOptions.length > 0 ) {
            return "--debugClassLoading";
        }
        else {
            return null;
        }
    }
}
