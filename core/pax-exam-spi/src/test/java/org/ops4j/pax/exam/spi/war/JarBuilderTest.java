/*
 * Copyright 2013 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.spi.war;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.jarProbe;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.zip.ZipFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.options.JarProbeOption;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.reactors.PerSuite;

import com.google.common.io.Files;

public class JarBuilderTest {

    private File tempDir;
    private ZipFile jar;
    
    @Before
    public void before() {
        tempDir = Files.createTempDir();
    }

    @After
    public void tearDown() {
        FileUtils.delete(tempDir);
        if (jar != null) {
            try {
                jar.close();
            }
            catch (IOException e) {
                // ignore
            }
        }
    }


    private ZipFile localCopy(JarProbeOption option) throws IOException {
        URI uri = new JarBuilder(tempDir, option).buildJar(); 
        return new ZipFile(new File(uri));
    }

    @Test
    public void buildJarWithClasses() throws MalformedURLException, IOException {
        jar = localCopy(jarProbe().classes(ExamReactor.class, PerSuite.class));
        assertThat(jar.getEntry("META-INF/beans.xml"), is(nullValue()));
        assertThat(jar.getEntry("org/ops4j/pax/exam/spi/ExamReactor.class"),
            is(notNullValue()));
        assertThat(jar.getEntry("org/ops4j/pax/exam/spi/reactors/PerSuite.class"),
            is(notNullValue()));
        assertThat(jar.getEntry("org/ops4j/pax/exam/spi/reactors/PerClass.class"),
            is(nullValue()));
    }

    @Test
    public void buildJarWithMetaInfResource() throws MalformedURLException, IOException {
        jar = localCopy(jarProbe().classes(ExamReactor.class).metaInfResource("src/test/assets/web.xml"));
        assertThat(jar.getEntry("META-INF/web.xml"), is(notNullValue()));
        assertThat(jar.getEntry("org/ops4j/pax/exam/spi/ExamReactor.class"),
            is(notNullValue()));
        assertThat(jar.getEntry("org/ops4j/pax/exam/spi/reactors/PerSuite.class"),
            is(nullValue()));
    }

    @Test
    public void buildJarWithResource() throws MalformedURLException, IOException {
        jar = localCopy(jarProbe().classes(ExamReactor.class).resources("META-INF/maven/org.ops4j.base/ops4j-base-lang/pom.properties"));
        assertThat(jar.getEntry("META-INF/maven/org.ops4j.base/ops4j-base-lang/pom.properties"), is(notNullValue()));
        assertThat(jar.getEntry("org/ops4j/pax/exam/spi/ExamReactor.class"),
            is(notNullValue()));
        assertThat(jar.getEntry("org/ops4j/pax/exam/spi/reactors/PerSuite.class"),
            is(nullValue()));
    }
}
