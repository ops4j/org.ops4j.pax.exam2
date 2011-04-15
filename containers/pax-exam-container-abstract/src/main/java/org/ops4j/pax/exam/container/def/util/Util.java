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
package org.ops4j.pax.exam.container.def.util;

import static org.ops4j.pax.exam.OptionUtils.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.container.def.options.VMOption;
import org.ops4j.pax.exam.container.remote.options.RBCLookupTimeoutOption;
import org.ops4j.pax.exam.options.ProvisionOption;
import org.ops4j.pax.exam.options.SystemPropertyOption;
import org.ops4j.pax.exam.options.TestContainerStartTimeoutOption;

/**
 * Utility methods for converting configuration options to Pax Runner arguments.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0 December 10, 2008
 */
public class Util {




    /**
     * Adds a collection of arguments to a list of arguments by skipping null arguments.
     *
     * @param arguments      list to which the arguments should be added
     * @param argumentsToAdd arguments to be added (can be null or empty)
     */
    public static void add( final List<String> arguments,
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
    public static void add( final List<String> arguments,
                      final String argument )
    {
        if( argument != null && argument.trim().length() > 0 ) {
            arguments.add( argument );
        }
    }

    /**
     * Converts provision options into corresponding arguments (provision urls).
     *
     * @param bundles provision options
     *
     * @return converted Pax Runner collection of arguments
     */
    public static Collection<String> extractArguments( final ProvisionOption<?>[] bundles )
    {
        final List<String> arguments = new ArrayList<String>();
        for( ProvisionOption<?> bundle : bundles ) {
            arguments.add( bundle.getURL() );
        }
        return arguments;
    }

    

    /**
     * Converts system properties and vm options into corresponding arguments (--vmOptions).
     *
     * @param systemProperties system property options
     * @param vmOptions        virtual machine options
     *
     * @return converted Pax Runner argument
     */
    public static String extractArguments( final SystemPropertyOption[] systemProperties,
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
        return argument.toString();
    }

    /**
     * Filters the provided options by class returning an array of those option that are instance of the provided class.
     * Before filtering the options are expanded {@link #expand(Option[])}.
     *
     * @param optionType class of the desired options
     * @param options    options to be filtered (can be null or empty array)
     * @param <T>        type of desired options
     *
     * @return array of desired option type (never null). In case that the array of filtered options is null, empty or
     *         there is no option that matches the desired type an empty array is returned
     */
    @SuppressWarnings( "unchecked" )
    public static <T extends Option> T filterOne( boolean throwIfZero, boolean throwIfMoreOne, final Class<T> optionType,
                                                 final Option... options )
    {
        final List<T> filtered = new ArrayList<T>();
        for( Option option : options )
        {
            if( optionType.isAssignableFrom( option.getClass() ) )
            {
                filtered.add( (T) option );
            }
        }
        
        if (filtered.size() == 0) {
        	if (throwIfZero)
        		throw new IllegalArgumentException( "Configuration cannot contain zero "+optionType.getName() );
        		
        	return null;
        }
        
        if (filtered.size() > 1) {
        	if (throwIfMoreOne)
        		throw new IllegalArgumentException( "Configuration cannot contain more then one "+optionType.getName() );
        }
        
        return filtered.get( 0 );
    }
    
    /**
     * Determine the rmi lookup timeout.<br/>
     * Timeout is dermined by first looking for a {@link RBCLookupTimeoutOption} in the user options. If not specified a
     * default is used.
     * 
     * @param options user options
     * @return rmi lookup timeout
     */
    public static long getRMITimeout( final Option... options )
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
    public static long getTestContainerStartTimeout( final Option... options )
    {
        final TestContainerStartTimeoutOption[] timeoutOptions =
            filter( TestContainerStartTimeoutOption.class, options );
        if ( timeoutOptions.length > 0 )
        {
            return timeoutOptions[0].getTimeout();
        }
        return CoreOptions.waitForFrameworkStartup().getTimeout();
    }
    
}
