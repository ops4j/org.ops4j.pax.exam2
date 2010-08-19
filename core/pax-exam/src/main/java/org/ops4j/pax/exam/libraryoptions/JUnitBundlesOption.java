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
package org.ops4j.pax.exam.libraryoptions;

import static org.ops4j.pax.exam.Constants.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.options.AbstractDelegateProvisionOption;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;

/**
 * Option specifying junit bundles (osgi-fyed JUnit).
 * By default uses junit bundle published by SpringSource, version 4.4.0 (can be changed).
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 09, 2008
 */
public class JUnitBundlesOption
    extends AbstractDelegateProvisionOption<JUnitBundlesOption>
{

    /**
     * Constructor.
     */
    public JUnitBundlesOption()
    {
        super(
            mavenBundle()
                .groupId( "org.junit" )
                .artifactId( "com.springsource.org.junit" )
                .version( "4.4.0" )
        );
        noUpdate();
        startLevel( START_LEVEL_SYSTEM_BUNDLES );
    }

    /**
     * Sets the junit version.
     *
     * @param version junit version.
     *
     * @return itself, for fluent api usage
     */
    public JUnitBundlesOption version( final String version )
    {
        ( (MavenArtifactProvisionOption) getDelegate() ).version( version );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "JUnitBundlesOption" );
        sb.append( "{url=" ).append( getURL() );
        sb.append( '}' );
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected JUnitBundlesOption itself()
    {
        return this;
    }

}