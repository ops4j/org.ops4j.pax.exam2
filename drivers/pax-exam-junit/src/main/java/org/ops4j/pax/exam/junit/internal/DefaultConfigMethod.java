/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.ops4j.pax.exam.junit.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import static org.ops4j.lang.NullArgumentException.*;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4ConfigMethod;
import org.ops4j.pax.exam.junit.RequiresConfiguration;

/**
 * Configuration methodmarked with {@link Configuration} and {@link RequiresConfiguration} annotations.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @author Toni Menzel (toni@okidokiteam.com)
 * @since 0.3.0, December 16, 2008
 */
public class DefaultConfigMethod
    implements JUnit4ConfigMethod
{

    /**
     * Configuration method. Must be a accessible method (cannot be null).
     */
    private final Method m_method;
    /**
     * Instance of the class containing the configuration method. If null then the method is supposed to be static.
     */
    private final Object m_configInstance;

    /**
     * Configuration options. Lazy initialized only when the getter is called.
     */
    private Option[] m_options;

    /**
     * Constructor.
     *
     * @param configMethod   configuration method (cannot be null)
     * @param configInstance instance of the class containing the regression method.
     *                       If null then the method is supposed to be static.
     *
     * @throws IllegalArgumentException - If method is null
     */
    public DefaultConfigMethod( final Method configMethod,
                                final Object configInstance )
    {
        validateNotNull( configMethod, "Configuration method" );

        m_method = configMethod;
        m_configInstance = configInstance;
    }

    /**
     * Matches a regression method name against this configuration method.
     *
     * @param method regression method name (cannot be null or empty)
     *
     * @return true if the regression method name matches the configuration method, false otherwise
     *
     * @throws IllegalArgumentException - If method name is null or empty
     */
    public boolean matches( final Method method )
    {
        validateNotNull( method, "Method" );

        final RequiresConfiguration requiresConfigAnnotation = method.getAnnotation( RequiresConfiguration.class );
        final String[] requiredConfigs;
        if( requiresConfigAnnotation != null )
        {
            requiredConfigs = requiresConfigAnnotation.value();
        }
        else
        {
            requiredConfigs = new String[]{ ".*" };
        }
        for( String requiredConfig : requiredConfigs )
        {
            if( m_method.getName().matches( requiredConfig ) )
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the configuration options for this configuration method.
     *
     * @return array of configuration options
     *
     * @throws IllegalAccessException - Re-thrown, from invoking the configuration method via reflection
     * @throws java.lang.reflect.InvocationTargetException
     *                                - Re-thrown, from invoking the configuration method via reflection
     * @throws InstantiationException - Re-thrown, from invoking the configuration method via reflection
     */
    public Option[] getOptions()
        throws IllegalAccessException, InvocationTargetException, InstantiationException
    {
        if( m_options == null )
        {
            List<Option> options = new ArrayList<Option>();

            Configuration config = m_method.getAnnotation( Configuration.class );
            for( Class<? extends CompositeOption> option : config.extend() )
            {
                for( Option o : option.newInstance().getOptions() )
                {
                    options.add( o );
                }
            }

            for( Option o : (Option[]) m_method.invoke( m_configInstance ) )
            {
                options.add( o );
            }
            m_options = options.toArray( new Option[options.size()] );
        }

        return m_options;
    }

}