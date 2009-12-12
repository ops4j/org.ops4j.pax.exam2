package org.ops4j.pax.exam.growl;

import java.io.File;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.logging.Log;
import org.ops4j.pax.exam.growl.internal.AppleScriptBasedGrowlService;
import org.ops4j.pax.exam.growl.internal.NullGrowlService;
import org.ops4j.pax.exam.growl.internal.GrowlLogger;

/**
 * Api factory to access growl notifications.
 * If there is no apple script engine present, a null implementation will be used (nothing will happen)
 */
public class GrowlFactory implements GrowlService
{

    private final GrowlService m_service;

    GrowlFactory()
    {
        m_service = getService();
    }

    public boolean send( String title, String text )
    {

        return m_service.send( title, text );
    }

    public boolean send( String application, String title, String text )
    {
        return m_service.send( title, text );
    }

    public boolean send( String application, String title, String text, File image )
    {
        return m_service.send( application, title, text, image );
    }

    public static ScriptEngine getEngine()
    {
        ScriptEngineManager mgr = new ScriptEngineManager();
        return mgr.getEngineByName( "AppleScript" );
    }

    public static GrowlService getService()
    {
        ScriptEngine engine = getEngine();
        if( engine != null )
        {
            return new AppleScriptBasedGrowlService( engine );
        }
        else
        {
            //  return a null service to make it transparent 
            return new NullGrowlService();
        }
    }

    public static Log getLogger( Log log, String title, int mask )
    {
        return new GrowlLogger( log, title, mask );
    }


}
