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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;

/**
 * Unit Tests for {@link FeaturesScannerProvisionOption} and corresponding factory methods.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 27, 2009
 */
public class FeaturesScannerProvisionOptionTest
{

    /**
     * Test provision spec when using an string repository url.
     */
    @Test
    public void urlAsString()
    {
        assertThat(
            "Scan features url",
            new FeaturesScannerProvisionOption(
                "file:foo-features.xml", "f1", "f2/1.0.0"
            ).getURL(),
            is( equalTo( "scan-features:file:foo-features.xml!/f1,f2/1.0.0" ) )
        );
    }

    /**
     * Test provision spec when using an string repository url via static factory method.
     */
    @Test
    public void urlAsStringViaFactoryMethod()
    {
        assertThat(
            "Scan features url",
            scanFeatures(
                "file:foo-features.xml", "f1", "f2/1.0.0"
            ).getURL(),
            is( equalTo( "scan-features:file:foo-features.xml!/f1,f2/1.0.0" ) )
        );
    }

    /**
     * Test provision spec when using an url reference for repository url.
     */
    @Test
    public void urlAsReference()
    {
        assertThat(
            "Scan features url",
            new FeaturesScannerProvisionOption(
                url( "file:foo-features.xml" ), "f1", "f2/1.0.0"
            ).getURL(),
            is( equalTo( "scan-features:file:foo-features.xml!/f1,f2/1.0.0" ) )
        );
    }

    /**
     * Test provision spec when using an url reference for repository url via static factory method.
     */
    @Test
    public void urlAsReferenceViaFactoryMethod()
    {
        assertThat(
            "Scan features url",
            scanFeatures(
                url( "file:foo-features.xml" ), "f1", "f2/1.0.0"
            ).getURL(),
            is( equalTo( "scan-features:file:foo-features.xml!/f1,f2/1.0.0" ) )
        );
    }

    /**
     * Test provision spec when using an maven url reference for repository url.
     */
    @Test
    public void urlAsMavenUrl()
    {
        assertThat(
            "Scan features url",
            new FeaturesScannerProvisionOption(
                maven().groupId( "bar" ).artifactId( "foo" ).classifier( "features" ).type( "xml" ), "f1", "f2/1.0.0"
            ).getURL(),
            is( equalTo( "scan-features:mvn:bar/foo//xml/features!/f1,f2/1.0.0" ) )
        );
    }

    /**
     * Test provision spec when using an maven url reference for repository url via static factory method.
     */
    @Test
    public void urlAsMavenUrlViaFactoryMethod()
    {
        assertThat(
            "Scan features url",
            scanFeatures(
                maven().groupId( "bar" ).artifactId( "foo" ).classifier( "features" ).type( "xml" ), "f1", "f2/1.0.0"
            ).getURL(),
            is( equalTo( "scan-features:mvn:bar/foo//xml/features!/f1,f2/1.0.0" ) )
        );
    }

}