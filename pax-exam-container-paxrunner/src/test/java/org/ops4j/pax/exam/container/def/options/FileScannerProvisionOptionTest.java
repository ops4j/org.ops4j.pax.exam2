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
 * Unit Tests for {@link FileScannerProvisionOption} and corresponding factory methods.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 27, 2009
 */
public class FileScannerProvisionOptionTest
{

    /**
     * Test provision spec when using an string repository url.
     */
    @Test
    public void urlAsString()
    {
        assertThat(
            "Scan file url",
            new FileScannerProvisionOption(
                "file:foo.bundles"
            ).getURL(),
            is( equalTo( "scan-file:file:foo.bundles" ) )
        );
    }

    /**
     * Test provision spec when using an string repository url via static factory method.
     */
    @Test
    public void urlAsStringViaFactoryMethod()
    {
        assertThat(
            "Scan file url",
            scanFile(
                "file:foo.bundles"
            ).getURL(),
            is( equalTo( "scan-file:file:foo.bundles" ) )
        );
    }

    /**
     * Test provision spec when using an url reference for repository url.
     */
    @Test
    public void urlAsReference()
    {
        assertThat(
            "Scan file url",
            new FileScannerProvisionOption(
                url( "file:foo.bundles" )
            ).getURL(),
            is( equalTo( "scan-file:file:foo.bundles" ) )
        );
    }

    /**
     * Test provision spec when using an url reference for repository url via static factory method.
     */
    @Test
    public void urlAsReferenceViaFactoryMethod()
    {
        assertThat(
            "Scan file url",
            scanFile(
                url( "file:foo.bundles" )
            ).getURL(),
            is( equalTo( "scan-file:file:foo.bundles" ) )
        );
    }

    /**
     * Test provision spec when using an maven url reference for repository url.
     */
    @Test
    public void urlAsMavenUrl()
    {
        assertThat(
            "Scan file url",
            new FileScannerProvisionOption(
                maven().groupId( "bar" ).artifactId( "foo" ).type( "bundles" )
            ).getURL(),
            is( equalTo( "scan-file:mvn:bar/foo//bundles" ) )
        );
    }

    /**
     * Test provision spec when using an maven url reference for repository url via static factory method.
     */
    @Test
    public void urlAsMavenUrlViaFactoryMethod()
    {
        assertThat(
            "Scan file url",
            scanFile(
                maven().groupId( "bar" ).artifactId( "foo" ).type( "bundles" )
            ).getURL(),
            is( equalTo( "scan-file:mvn:bar/foo//bundles" ) )
        );
    }

}