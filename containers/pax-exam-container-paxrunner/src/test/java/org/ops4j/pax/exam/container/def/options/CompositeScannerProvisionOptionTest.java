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
import org.ops4j.pax.exam.options.extra.CompositeScannerProvisionOption;

import static org.ops4j.pax.exam.CoreOptions.*;

/**
 * Unit Tests for {@link CompositeScannerProvisionOption} and corresponding factory methods.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.6.0, May 03, 2009
 */
public class CompositeScannerProvisionOptionTest
{

    /**
     * Test provision spec when using an string repository url.
     */
    @Test
    public void urlAsString()
    {
        assertThat(
            "Scan composite url",
            new CompositeScannerProvisionOption(
                "file:foo.composite"
            ).getURL(),
            is( equalTo( "scan-composite:file:foo.composite@update" ) )
        );
    }

    /**
     * Test provision spec when using an string repository url via static factory method.
     */
    @Test
    public void urlAsStringViaFactoryMethod()
    {
        assertThat(
            "Scan composite url",
            scanComposite(
                "file:foo.composite"
            ).getURL(),
            is( equalTo( "scan-composite:file:foo.composite@update" ) )
        );
    }

    /**
     * Test provision spec when using an url reference for repository url.
     */
    @Test
    public void urlAsReference()
    {
        assertThat(
            "Scan composite url",
            new CompositeScannerProvisionOption(
                url( "file:foo.composite" )
            ).getURL(),
            is( equalTo( "scan-composite:file:foo.composite@update" ) )
        );
    }

    /**
     * Test provision spec when using an url reference for repository url via static factory method.
     */
    @Test
    public void urlAsReferenceViaFactoryMethod()
    {
        assertThat(
            "Scan composite url",
            scanComposite(
                url( "file:foo.composite" )
            ).getURL(),
            is( equalTo( "scan-composite:file:foo.composite@update" ) )
        );
    }

    /**
     * Test provision spec when using an maven url reference for repository url.
     */
    @Test
    public void urlAsMavenUrl()
    {
        assertThat(
            "Scan composite url",
            new CompositeScannerProvisionOption(
                maven().groupId( "bar" ).artifactId( "foo" ).type( "composite" )
            ).getURL(),
            is( equalTo( "scan-composite:mvn:bar/foo//composite@update" ) )
        );
    }

    /**
     * Test provision spec when using an maven url reference for repository url via static factory method.
     */
    @Test
    public void urlAsMavenUrlViaFactoryMethod()
    {
        assertThat(
            "Scan composite url",
            scanComposite(
                maven().groupId( "bar" ).artifactId( "foo" ).type( "composite" )
            ).getURL(),
            is( equalTo( "scan-composite:mvn:bar/foo//composite@update" ) )
        );
    }

}