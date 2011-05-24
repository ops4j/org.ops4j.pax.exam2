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
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.options.UrlReference;

/**
 * Option specifying provision form an Pax Runner Bundle scanner (scan-bundle).
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 17, 2008
 */
public class BundleScannerProvisionOption
    extends AbstractUrlProvisionOption<BundleScannerProvisionOption>
{

    /**
     * Constructor.
     *
     * @param url bundle url
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public BundleScannerProvisionOption( final String url )
    {
        super( new UrlProvisionOption( url ) );
    }

    /**
     * Constructor.
     *
     * @param url bundle url
     *
     * @throws IllegalArgumentException - If url is null
     */
    public BundleScannerProvisionOption( final UrlReference url )
    {
        super( url );
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        return new StringBuilder()
            .append( "scan-bundle" )
            .append( ":" )
            .append( super.getURL() )
            .append( getOptions( this ) )
            .toString();
    }

    /**
     * {@inheritDoc}
     */
    protected BundleScannerProvisionOption itself()
    {
        return this;
    }

}