package org.ops4j.pax.exam.growl.internal;

import java.io.File;
import org.ops4j.pax.exam.growl.GrowlService;

/**
 * Transparent Service that make it transparent even on win machines who do not have apple script engines.
 *
 * @author Toni Menzel
 */
public class NullGrowlService implements GrowlService
{

    public boolean send( String title, String text )
    {
        return false;
    }

    public boolean send( String application, String title, String text )
    {
        return false;
    }

    public boolean send( String application, String title, String text, File image )
    {
        return false;  
    }
}
