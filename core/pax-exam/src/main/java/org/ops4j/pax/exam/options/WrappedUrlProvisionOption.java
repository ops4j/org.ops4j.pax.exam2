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
package org.ops4j.pax.exam.options;

import static org.osgi.framework.Constants.*;

/**
 * Option specifying a provision url that will wrap (osgify) another bundle.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 27, 2008
 */
public class WrappedUrlProvisionOption
    extends AbstractUrlProvisionOption<WrappedUrlProvisionOption>
{

    /**
     * Wrapped jar bundle symbolic name. Can be null.
     */
    private String bundleSymbolicName;
    /**
     * Wrapped jar bundle version. Can be null.
     */
    private String bundleVersion;
    /**
     * Wrapped jar imports.
     */
    private String[] imports;
    /**
     * Wrapped jar exports.
     */
    private String[] exports;
    /**
     * Wrapped jar raw instructions.
     */
    private String[] instructions;
    private WrappedUrlProvisionOption.OverwriteMode overwriteMode;

    /**
     * Constructor.
     *
     * @param url wrapped jar url (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public WrappedUrlProvisionOption( final String url )
    {
        super( url );
    }

    /**
     * Constructor.
     *
     * @param url wrapped jar url (cannot be null)
     *
     * @throws IllegalArgumentException - If url is null
     */
    public WrappedUrlProvisionOption( final UrlReference url )
    {
        super( url );
    }

    /**
     * {@inheritDoc}
     */
    public String getURL()
    {
        final StringBuilder options = new StringBuilder();
        if( overwriteMode != null )
        {
            if( options.length() > 0 )
            {
                options.append( "&" );
            }
            options.append( "overwrite=" ).append( overwriteMode );
        }
        if( bundleSymbolicName != null )
        {
            if( options.length() > 0 )
            {
                options.append( "&" );
            }
            options.append( BUNDLE_SYMBOLICNAME ).append( "=" ).append( bundleSymbolicName );
        }
        if( bundleVersion != null )
        {
            if( options.length() > 0 )
            {
                options.append( "&" );
            }
            options.append( BUNDLE_VERSION ).append( "=" ).append( bundleVersion );
        }
        if( imports != null && imports.length > 0 )
        {
            if( options.length() > 0 )
            {
                options.append( "&" );
            }
            options.append( IMPORT_PACKAGE ).append( "=" );
            for( String entry : imports )
            {
                options.append( entry ).append( "," );
            }
            options.delete( options.length() - 1, options.length() );
        }
        if( exports != null && exports.length > 0 )
        {
            if( options.length() > 0 )
            {
                options.append( "&" );
            }
            options.append( EXPORT_PACKAGE ).append( "=" );
            for( String entry : exports )
            {
                options.append( entry ).append( "," );
            }
            options.delete( options.length() - 1, options.length() );
        }
        if( instructions != null && instructions.length > 0 )
        {
            for( String entry : instructions )
            {
                if( options.length() > 0 )
                {
                    options.append( "&" );
                }
                options.append( entry );
            }
        }
        if( options.length() > 0 )
        {
            options.insert( 0, "$" );
        }
        return "wrap:" + super.getURL() + options.toString();
    }

    /**
     * Sets wrapped jar bundle symbolic name.
     *
     * @param bundleSymbolicName bundle symbolic name
     *
     * @return itself
     */
    public WrappedUrlProvisionOption bundleSymbolicName( final String bundleSymbolicName )
    {
        this.bundleSymbolicName = bundleSymbolicName;

        return this;
    }

    /**
     * Sets wrapped jar bundle version.
     *
     * @param bundleVersion bundle symbolic name
     *
     * @return itself
     */
    public WrappedUrlProvisionOption bundleVersion( final String bundleVersion )
    {
        this.bundleVersion = bundleVersion;

        return this;
    }

    /**
     * Sets wrapped jar imports.
     *
     * @param imports BND style imports
     *
     * @return itself
     */
    public WrappedUrlProvisionOption imports( final String... imports )
    {
        this.imports = imports;

        return this;
    }

    /**
     * Sets wrapped jar exports.
     *
     * @param exports BND style exports
     *
     * @return itself
     */
    public WrappedUrlProvisionOption exports( final String... exports )
    {
        this.exports = exports;

        return this;
    }

    /**
     * Sets wrapped jar manifest overwrite mode.
     *
     * @param mode overwrite mode
     *
     * @return itself
     */
    public WrappedUrlProvisionOption overwriteManifest( final OverwriteMode mode )
    {
        overwriteMode = mode;

        return this;
    }

    /**
     * Sets wrapped jar raw BND instructions.
     *
     * @param instructions BND instructions
     *
     * @return itself
     */
    public WrappedUrlProvisionOption instructions( final String... instructions )
    {
        this.instructions = instructions;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    protected WrappedUrlProvisionOption itself()
    {
        return this;
    }

    /**
     * Strategy to use regarding manifest rewrite, for a jar that is already a bundle (has osgi manifest attributes).
     */
    public static enum OverwriteMode
    {

        /**
         * Keep existing manifest.
         */
        KEEP,

        /**
         * Merge instructions with current manifest entries.
         */
        MERGE,

        /**
         * Full rewrite.
         */
        FULL

    }

}