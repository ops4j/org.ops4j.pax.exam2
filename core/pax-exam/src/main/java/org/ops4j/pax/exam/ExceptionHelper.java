package org.ops4j.pax.exam;

/**
 * 
 */
public class ExceptionHelper {

    public static Throwable unwind( Throwable e )
    {

        Throwable t = e.getCause();
        if( t != null ) {
            return unwind( t );
        }
        else {
            return e;
        }
    }
}
