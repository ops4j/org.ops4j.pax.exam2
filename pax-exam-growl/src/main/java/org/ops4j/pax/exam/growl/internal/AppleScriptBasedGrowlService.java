package org.ops4j.pax.exam.growl.internal;

import java.io.File;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.ops4j.pax.exam.growl.GrowlService;

/**
 * Silly simple Apple Script bridge to growl.
 */
public class AppleScriptBasedGrowlService implements GrowlService
{

    private static final String DEFAULT_APPLICATION = "Pax Exam";

    private static String SCRIPT = "tell application \"GrowlHelperApp\"\n"
                                   + "\tset the allNotificationsList to {\"GROWLNOTIFICATION\"}\n"
                                   + "\tset the enabledNotificationsList to {\"GROWLNOTIFICATION\"}\n"
                                   + "\tregister as application \"GROWLAPP\" all notifications allNotificationsList default notifications enabledNotificationsList\n"
                                   + "\tnotify with name \"GROWLNOTIFICATION\" title \"GROWLTITLE\" description \"GROWLTEXT\" application name \"GROWLAPP\" image from location \"file:///Users/tonit/pax-exam.png\"\n"
                                   + "end tell";
    
    private ScriptEngine m_engine;

    public AppleScriptBasedGrowlService(ScriptEngine engine)
    {
        // unpack default image
        m_engine = engine;
    }

    public boolean send( String title, String text )
    {
        return send( DEFAULT_APPLICATION, title, text, null );
    }

    public boolean send( String application, String title, String text )
    {
        return send( application, title, text, null );
    }

    public boolean send( String application, String title, String text, File image )
    {
        String s = SCRIPT.replaceAll( "GROWLAPP", application )
            .replaceAll( "GROWLNOTIFICATION", "standard" )
            .replaceAll( "GROWLTITLE", title )
            .replaceAll( "GROWLTEXT", text )
            .replaceAll( "GROWLIMAGE","file:///Users/tonit/pax-exam.png" );


        try
        {
            Object o = m_engine.eval( s );


        } catch( ScriptException e )
        {
            //System.out.println( "Problem: " + e.getMessage() );
            //throw new RuntimeException( "Problem in script !", e );
            return false;
        }
        return true;
    }


}
