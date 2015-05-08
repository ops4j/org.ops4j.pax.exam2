/*
 * Copyright 2015 Harald Wellmann
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

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ops4j.io.FileUtils;
import org.ops4j.pax.exam.spi.DefaultExamSystem;

public class ZipBuilderTest {

    private File tempDir;

    @Before
    public void before() {
        tempDir = DefaultExamSystem.createTempDir();
    }

    @After
    public void tearDown() {
        FileUtils.delete(tempDir);
    }

    @Test
    public void shouldAddDirectory() throws IOException {
        File archive = new File(tempDir, "test.zip");
        ZipBuilder zipBuilder = new ZipBuilder(archive);
        zipBuilder.addDirectory(new File("target", "classes"), "WEB-INF/classes");
        zipBuilder.close();

        try (ZipFile zipFile = new ZipFile(archive)) {
            assertThat(
                zipFile.getEntry("WEB-INF/classes/org/ops4j/pax/exam/spi/ExamReactor.class"),
                is(notNullValue()));
            assertThat(zipFile.getEntry("org/ops4j/pax/exam/spi/ExamReactor.class"),
                is(nullValue()));
        }
    }
}
