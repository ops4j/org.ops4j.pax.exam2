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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit Tests for {@link MavenArtifactUrlReference}.
 * 
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 26, 2009
 */
public class MavenUrlReferenceTest {

    @Test
    public void specifyTypeWithVersion() {
        assertThat("Maven url", new MavenArtifactUrlReference().groupId("foo").artifactId("bar")
            .version("1.0").type("xml").getURL(), is(equalTo("mvn:foo/bar/1.0/xml")));
    }

    @Test
    public void specifyTypeWithoutVersion() {
        assertThat("Maven url", new MavenArtifactUrlReference().groupId("foo").artifactId("bar")
            .type("xml").getURL(), is(equalTo("mvn:foo/bar//xml")));
    }

    @Test
    public void specifyClasifier() {
        assertThat("Maven url", new MavenArtifactUrlReference().groupId("foo").artifactId("bar")
            .classifier("classifier").getURL(), is(equalTo("mvn:foo/bar///classifier")));
    }

    @Test
    public void specifyClasifierWithVersion() {
        assertThat("Maven url", new MavenArtifactUrlReference().groupId("foo").artifactId("bar")
            .version("1.0").classifier("classifier").getURL(),
            is(equalTo("mvn:foo/bar/1.0//classifier")));
    }

    @Test
    public void specifyClasifierWithType() {
        assertThat("Maven url", new MavenArtifactUrlReference().groupId("foo").artifactId("bar")
            .type("xml").classifier("classifier").getURL(),
            is(equalTo("mvn:foo/bar//xml/classifier")));
    }

    @Test
    public void specifyClasifierWithVersionAndType() {
        assertThat("Maven url", new MavenArtifactUrlReference().groupId("foo").artifactId("bar")
            .version("1.0").type("xml").classifier("classifier").getURL(),
            is(equalTo("mvn:foo/bar/1.0/xml/classifier")));
    }

}
