/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.junit.options;

import static org.ops4j.pax.exam.Constants.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.options.AbstractDelegateProvisionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption;

/**
 * Option specifying Mockito bundles (osgi-fyed mockito).
 * See: http://code.google.com/p/mockito/
 * By default uses the mockito-all delivery wrapped up on the fly as a bundle.
 *
 * Version: 1.7. Can be changed.
 *
 * @author Toni Menzel (tonit)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since Mar 14, 2009
 */
public class MockitoBundlesOption
    extends AbstractDelegateProvisionOption<MockitoBundlesOption>
{

    /**
     * Constructor.
     */
    public MockitoBundlesOption()
    {
        super(
            wrappedBundle(
                maven()
                    .groupId( "org.mockito" )
                    .artifactId( "mockito-all" )
                    .version( "1.7" )
            )
        );
        noUpdate();
        startLevel( START_LEVEL_SYSTEM_BUNDLES );
    }

    /**
     * Sets the Mockito version.
     *
     * @param version Mockito version.
     *
     * @return itself, for fluent api usage
     */
    public MockitoBundlesOption version( final String version )
    {
        ( (MavenArtifactUrlReference) ( (WrappedUrlProvisionOption) getDelegate() ).getUrlReference() ).version(
            version
        );
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "MockitoBundlesOption" );
        sb.append( "{url=" ).append( getURL() );
        sb.append( '}' );
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected MockitoBundlesOption itself()
    {
        return this;
    }

}
