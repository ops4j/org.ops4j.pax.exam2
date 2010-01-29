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
package org.ops4j.pax.exam.container.def.options;

import static org.ops4j.pax.exam.container.def.options.ScannerUtils.*;
import org.ops4j.pax.exam.options.AbstractUrlProvisionOption;
import org.ops4j.pax.exam.options.UrlReference;
import static org.ops4j.pax.scanner.ServiceConstants.*;
import static org.ops4j.pax.scanner.composite.ServiceConstants.*;

/**
 * Option specifying provision form an Pax Runner composite scanner.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.6.0, May 01, 2009
 */
public class CompositeScannerProvisionOption
    extends AbstractUrlProvisionOption<CompositeScannerProvisionOption>
    implements Scanner
{

    /**
     * Constructor.
     *
     * @param url provision file url (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public CompositeScannerProvisionOption( final String url )
    {
        super( url );
    }

    /**
     * Constructor.
     *
     * @param url provision file url (cannot be null)
     *
     * @throws IllegalArgumentException - If url is null
     */
    public CompositeScannerProvisionOption( final UrlReference url )
    {
        super( url );
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        return new StringBuilder()
            .append( SCHEMA )
            .append( SEPARATOR_SCHEME )
            .append( super.getURL() )
            .append( getOptions( this ) )
            .toString();
    }

    /**
     * {@inheritDoc}
     */
    protected CompositeScannerProvisionOption itself()
    {
        return this;
    }

}