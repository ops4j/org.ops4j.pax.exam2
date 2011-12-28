/*
 * Copyright 2011 Harald Wellmann.
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
package org.ops4j.pax.exam;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.ops4j.util.property.PropertiesPropertyResolver;

/**
 * Reads configuration settings from a properties file {@code exam.properties} at the root of the
 * classpath. System properties take precedence over properties in the configuration file.
 * 
 * @author Harald Wellmann
 * @since Dec 2011
 */
public class ConfigurationManager
{
    private PropertiesPropertyResolver resolver;

    public ConfigurationManager()
    {
        Properties props = new Properties();
        InputStream is = getClass().getResourceAsStream( Constants.EXAM_PROPERTIES_PATH );
        if( is != null )
        {
            try
            {
                props.load( is );
                resolver = new PropertiesPropertyResolver( props );
            }
            catch ( IOException exc )
            {
                throw new TestContainerException( exc );
            }
        }
        resolver = new PropertiesPropertyResolver( System.getProperties(), resolver );
    }

    public String getProperty( String key )
    {
        return resolver.get( key );
    }
}
