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

import static org.ops4j.pax.exam.options.extra.ScannerUtils.*;

import org.ops4j.pax.exam.options.AbstractUrlProvisionOption;
import org.ops4j.pax.exam.options.UrlReference;

/**
 * Option specifying provision form an Pax Runner File scanner.
 *
 * @deprecated Only supported by Pax Runner Container which will be removed in Pax Exam 3.0.
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 17, 2008
 */
@Deprecated
public class FileScannerProvisionOption
    extends AbstractUrlProvisionOption<FileScannerProvisionOption>
    implements Scanner
{

    /**
     * Constructor.
     *
     * @param url provision file url (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public FileScannerProvisionOption( final String url )
    {
        super( url );
        update( false );
    }

    /**
     * Constructor.
     *
     * @param url provision file url (cannot be null)
     *
     * @throws IllegalArgumentException - If url is null
     */
    public FileScannerProvisionOption( final UrlReference url )
    {
        super( url );
        update( false );
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        return new StringBuilder()
            .append( "scan-file" )
            .append( ":" )
            .append( super.getURL() )
            .append( getOptions( this ) )
            .toString();
    }

    /**
     * {@inheritDoc}
     */
    protected FileScannerProvisionOption itself()
    {
        return this;
    }

}