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
package org.ops4j.pax.exam.forked.provision;


public final class Info
{

    private static final int LABEL_LENGTH = 1;
    private static final String SPACES = "                                                                            ";

    private Info()
    {
        // utility class
    }

    public static void println( final String message )
    {
        println( null, message );
    }

    public static void print( final String message )
    {
        print( null, message );
    }

    public static void println()
    {
        System.out.println();
    }

    public static void println( final String label, final String message )
    {
        print( label, message );
        println();
    }

    public static void print( final String label, final String message )
    {
        if( label != null )
        {
            System.out.print( format( label + ":" ) );
        }
        else
        {
            System.out.print( format( "" ) );
        }
        if( message != null )
        {
            if( label != null )
            {
                System.out.print( " " + message );
            }
            else
            {
                System.out.print( " -> " + message );
            }
        }
    }

    private static String format( final String toFormat )
    {
        if( toFormat == null )
        {
            return fill( LABEL_LENGTH );
        }
        if( toFormat.length() >= LABEL_LENGTH )
        {
            return toFormat.substring( toFormat.length() - LABEL_LENGTH );
        }
        return fill( LABEL_LENGTH - toFormat.length() ) + toFormat;
    }

    private static String fill( final int length )
    {
        return SPACES.substring( 0, Math.min( length, LABEL_LENGTH ) - 1 );
    }

}
