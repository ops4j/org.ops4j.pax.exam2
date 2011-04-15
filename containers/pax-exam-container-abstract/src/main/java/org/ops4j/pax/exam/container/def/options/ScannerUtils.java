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
package org.ops4j.pax.exam.container.def.options;

import org.ops4j.pax.exam.options.ProvisionOption;
import static org.ops4j.pax.scanner.ServiceConstants.*;

/**
 * Utility methods related to scanner options.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 18, 2008
 */
class ScannerUtils
{

    /**
     * Utility class. Ment to be used via the static methods.
     */
    private ScannerUtils()
    {
        // utility class
    }

    /**
     * Returns common scanner options. Ment to be used by subclasses when building the url.
     *
     * @param provision provision options to be used (cannot be null)
     *
     * @return common scanner options (cannot be null)
     */
    static String getOptions( final ProvisionOption<?> provision )
    {
        final StringBuilder options = new StringBuilder();
        if( provision.shouldUpdate() )
        {
            options.append( SEPARATOR_OPTION ).append( OPTION_UPDATE );
        }
        if( !provision.shouldStart() )
        {
            options.append( SEPARATOR_OPTION ).append( OPTION_NO_START );
        }
        if( provision.getStartLevel() != null )
        {
            options.append( SEPARATOR_OPTION ).append( provision.getStartLevel() );
        }
        return options.toString();
    }

}
