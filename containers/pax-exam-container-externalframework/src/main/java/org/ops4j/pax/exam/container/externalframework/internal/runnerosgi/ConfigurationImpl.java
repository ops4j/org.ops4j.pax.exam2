/*
 * Copyright 2007 Alin Dreghiciu.
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.runner.Configuration;

/**
 * Configuration implementation that reads properties from a properties file.
 *
 * @author Alin Dreghiciu
 * @since August 26, 2007
 */
public class ConfigurationImpl
    implements Configuration
{

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog( ConfigurationImpl.class );
    /**
     * The protoc corresponding to classpath.
     */
    private static final String CLASSPATH_PROTOCOL = "classpath:";
    /**
     * The loaded Properties.
     */
    private final Properties m_properties;

    /**
     * Creates the configuration by reading propertiesfrom an url.
     * The url can start with classpath: and then the classpath is searched for the resource after ":".
     *
     * @param url url to load properties from
     */
    public ConfigurationImpl( final String url )
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

    }
    
    public ConfigurationImpl ( Properties config, String url) {
    	this(url);
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
