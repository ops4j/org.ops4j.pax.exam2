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
import static org.ops4j.pax.exam.options.extra.ScannerUtils.*;

import org.ops4j.pax.exam.options.AbstractProvisionOption;

/**
 * Option specifying provision form an Pax Runner Dir scanner.
 *
 * @deprecated Only supported by Pax Runner Container which will be removed in Pax Exam 3.0.
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 17, 2008
 */
@Deprecated
public class DirScannerProvisionOption
    extends AbstractProvisionOption<DirScannerProvisionOption>
    implements Scanner
{

    /**
     * Directory path (cannot be null or empty).
     */
    private final String m_path;
    /**
     * Ant style regular expresion to be matched against file names (can be null = no filtering)
     */
    private String m_filter;

    /**
     * Constructor.
     *
     * @param path directory to be scanned path (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public DirScannerProvisionOption( final String path )
    {
        validateNotEmpty( path, true, "Directory path" );
        m_path = path;
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        final StringBuilder url = new StringBuilder().append( "scan-dir" ).append( ":" ).append( m_path );
        if( m_filter != null )
        {
            url.append( "!/" ).append( m_filter );
        }
        url.append( getOptions( this ) );
        return url.toString();
    }

    /**
     * Sets the filter to be applied to the scanned file names.
     *
     * @param filter ant style regular expresion to be matched against file names (cannot be null or empty)
     *
     * @return itself, for fluent api usage
     *
     * @throws IllegalArgumentException - If filter is null or empty
     */
    public DirScannerProvisionOption filter( final String filter )
    {
        validateNotEmpty( filter, true, "Filter" );
        m_filter = filter;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "DirScannerProvisionOption" );
        sb.append( "{url='" ).append( getURL() ).append( '\'' );
        sb.append( '}' );
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected DirScannerProvisionOption itself()
    {
        return this;
    }

}