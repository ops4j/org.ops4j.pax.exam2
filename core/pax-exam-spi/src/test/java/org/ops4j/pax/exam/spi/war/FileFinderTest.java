/*
 * Copyright 2013 Harald Wellmann
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

package org.ops4j.pax.exam.spi.war;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FilenameFilter;

import org.junit.Test;


/**
 * @author Harald Wellmann
 *
 */
public class FileFinderTest {
    
    @Test
    public void findFileInTree() {
        File rootDir = new File("src/main/java");
        File file = FileFinder.findFile(rootDir, "FileFinder.java");
        assertThat(file.getPath(), is("src/main/java/org/ops4j/pax/exam/spi/war/FileFinder.java"));
    }

    @Test
    public void findFileInRoot() {
        File rootDir = new File("src/main/java/org/ops4j/pax/exam/spi/war");
        File file = FileFinder.findFile(rootDir, "FileFinder.java");
        assertThat(file.getPath(), is("src/main/java/org/ops4j/pax/exam/spi/war/FileFinder.java"));
    }

    @Test
    public void findFileWithFilter() {
        File rootDir = new File("src/main/java/");
        FilenameFilter filter = new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("Jar");
            }
        };
        File file = FileFinder.findFile(rootDir, filter);
        assertThat(file.getPath(), is("src/main/java/org/ops4j/pax/exam/spi/war/JarCreator.java"));
    }

    @Test
    public void findDirectory() {
        File rootDir = new File("src");
        File file = FileFinder.findFile(rootDir, "java");
        assertThat(file.getPath(), is("src/main/java"));        
    }

    @Test
    public void findSubdirectory() {
        File rootDir = new File("src/test");
        File file = FileFinder.findFile(rootDir, "java");
        assertThat(file.getPath(), is("src/test/java"));        
    }
}
