package org.ops4j.pax.exam;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.ops4j.util.property.PropertiesPropertyResolver;

public class ConfigurationManager
{
    private PropertiesPropertyResolver resolver;

    public ConfigurationManager()
    {        
        Properties props = new Properties();
        InputStream is = getClass().getResourceAsStream( "/exam.properties" );
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
