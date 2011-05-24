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
package org.ops4j.pax.exam.options.extra;

import static org.ops4j.lang.NullArgumentException.*;
import org.ops4j.pax.exam.Option;

/**
 * Option specifying a raw virtual machine option.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0 December 10, 2008
 */
public class VMOption
    implements Option
{

    /**
     * Virtual machine option. Cannot be null or empty.
     */
    private final String m_option;

    /**
     * Constructor.
     *
     * @param option virtual machine option (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If option is null or empty
     */
    public VMOption( final String option )
    {
        validateNotEmpty( option, true, "VM option" );
        m_option = option;
    }

    /**
     * Getter.
     *
     * @return virtual machine option (cannot be null or empty)
     */
    public String getOption()
    {
        return m_option;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "VMOption" );
        sb.append( "{option='" ).append( m_option ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }
}