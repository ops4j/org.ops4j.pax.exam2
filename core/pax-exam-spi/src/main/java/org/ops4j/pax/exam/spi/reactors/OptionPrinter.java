/*
 * Copyright (C) 2010 Okidokiteam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.spi.reactors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionDescription;
import org.ops4j.pax.exam.TestContainer;

/**
 * Simple helper for reporting about options.
 */
public class OptionPrinter
{

    private static Logger LOG = LoggerFactory.getLogger( OptionPrinter.class );

    public void print( String contextText, final OptionDescription options, Class<? extends TestContainer> containerClazz )
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "\n**[OPTIONS Report] : " ).append( contextText );
        sb.append( "\nContainer used: " ).append( containerClazz.getName() );
        if( options.getIgnoredOptions().length + options.getUsedOptions().length == 0 )
        {
            sb.append( "\nPossible problem: No options discovered. " );

        }
        if( options.getIgnoredOptions().length > 0 )
        {

            sb.append( "\nOptions Included :" );
            for( Option s : options.getUsedOptions() )
            {
                sb.append( "\n          " ).append( s );

            }
        }
        else
        {
            sb.append( "\nPossible problem: No included options discovered. " );
        }

        if( options.getIgnoredOptions().length > 0 )
        {

            sb.append( "\nOptions not included :" );

            for( Option s : options.getIgnoredOptions() )
            {
                sb.append( "\n          " ).append( s );

            }
        }
        sb.append( "\n**" + "" );

        LOG.debug( sb.toString() );
    }
}
