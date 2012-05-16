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
package org.ops4j.pax.exam.options.libraries;

import static org.ops4j.pax.exam.Constants.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.options.AbstractDelegateProvisionOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.options.WrappedUrlProvisionOption;

/**
 * This provides JMock Support for Pax Exam.
 * By default we supply version 2.5.1. Version can be changed.
 *
 * @author Toni Menzel (tonit)
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since Mar 15, 2009
 */
@Deprecated
public class JMockBundlesOption
    extends AbstractDelegateProvisionOption<JMockBundlesOption>
{

    /**
     * You'll get a wrapped artifact of jmock version 2.5.1 by default.
     */
    public JMockBundlesOption()
    {
        super(
            wrappedBundle(
                maven()
                    .groupId( "org.jmock" )
                    .artifactId( "jmock" )
                    .version( "2.5.1" )
            )
        );
        noUpdate();
        startLevel( START_LEVEL_SYSTEM_BUNDLES );
    }

    /**
     * Sets the JMock version.
     *
     * @param version JMock version.
     *
     * @return itself, for fluent api usage
     */
    public JMockBundlesOption version( final String version )
    {
        ( (MavenArtifactUrlReference) ( (WrappedUrlProvisionOption) getDelegate() ).getUrlReference() ).version(
            version
        );
        return itself();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "JMockBundlesOption" );
        sb.append( "{url=" ).append( getURL() );
        sb.append( '}' );
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    protected JMockBundlesOption itself()
    {
        return this;
    }

}
