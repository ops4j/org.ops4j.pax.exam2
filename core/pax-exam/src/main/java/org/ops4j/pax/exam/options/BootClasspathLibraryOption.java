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
package org.ops4j.pax.exam.options;

import static org.ops4j.lang.NullArgumentException.*;
import org.ops4j.pax.exam.Option;

/**
 * Option specifying a library that will be made available in teh boot classpath.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 29, 2009
 */
public class BootClasspathLibraryOption
    implements Option
{

    /**
     * Library url (cannot be null).
     */
    private final UrlReference libraryUrl;
    /**
     * If the library should be in classpath after framework jar.
     */
    private boolean append;

    /**
     * Constructor.
     *
     * @param libraryUrl library url (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null or empty
     */
    public BootClasspathLibraryOption( final String libraryUrl )
    {
        this( new RawUrlReference( libraryUrl ) );
    }

    /**
     * Constructor.
     *
     * @param libraryUrl library url (cannot be null or empty)
     *
     * @throws IllegalArgumentException - If url is null
     */
    public BootClasspathLibraryOption( final UrlReference libraryUrl )
    {
        validateNotNull( libraryUrl, "URL" );
        this.libraryUrl = libraryUrl;
        append = true;
    }

    /**
     * To be used to specify that this library should be in the classpath before framework library.
     *
     * @return itself, for fluent api usage
     */
    public BootClasspathLibraryOption beforeFramework()
    {
        append = false;
        return itself();
    }

    /**
     * To be used to specify that this library should be in the classpath after framework library.
     *
     * @return itself, for fluent api usage
     */
    public BootClasspathLibraryOption afterFramework()
    {
        append = true;
        return itself();
    }

    /**
     * Getter.
     *
     * @return library url
     */
    public UrlReference getLibraryUrl()
    {
        return libraryUrl;
    }

    /**
     * Getter.
     *
     * @return true if the library should be before framework library in the classpath.
     */
    public boolean isBeforeFramework()
    {
        return !append;
    }

    /**
     * Getter.
     *
     * @return true if the library should be after framework library in the classpath.
     */
    public boolean isAfterFramework()
    {
        return append;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "BootClasspathOption" );
        sb.append( "{url=" ).append( libraryUrl );
        sb.append( ", append=" ).append( append );
        sb.append( '}' );
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected BootClasspathLibraryOption itself()
    {
        return this;
    }

}