/*
 * Copyright 2009 Alin Dreghiciu.
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
import static org.ops4j.pax.exam.options.extra.ScannerUtils.*;

import org.ops4j.pax.exam.options.AbstractProvisionOption;

/**
 * Option specifying a raw (non scanner type specific) provision spec for Pax Runner.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.4.1, April 17, 2009
 */
public class RawScannerProvisionOption
    extends AbstractProvisionOption<RawScannerProvisionOption>
    implements Scanner
{

    /**
     * Provisioning spec; cannot be null.
     */
    private String m_provisionSpec;

    /**
     * Constructor.
     *
     * @param provisionSpec provision url (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public RawScannerProvisionOption( final String provisionSpec )
    {
        validateNotEmpty( provisionSpec, true, "Provisioning spec" );
        m_provisionSpec = provisionSpec;
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        return new StringBuilder()
            .append( m_provisionSpec )
            .append( getOptions( this ) )
            .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( RawScannerProvisionOption.class.getSimpleName() );
        sb.append( "{url='" ).append( getURL() ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected RawScannerProvisionOption itself()
    {
        return this;
    }

}