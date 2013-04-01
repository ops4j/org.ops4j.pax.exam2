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

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * Finds files matching a given filter in a given directory tree.
 * 
 * @author Harald Wellmann
 * 
 */
public class FileFinder {

    private static FileFilter directoryFilter = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            return pathname.isDirectory();
        }
    };

    /**
     * Hidden utility class constructor
     */
    private FileFinder() {
    }

    /**
     * Finds a file with the given name in the given root directory or any subdirectory. The files
     * and directories are scanned in alphabetical order, so the result is deterministic.
     * <p>
     * The method returns the first matching result, if any, and ignores all other matches.
     * 
     * @param rootDir
     *            root directory
     * @param fileName
     *            exact file name, without any wildcards
     * @return matching file, or null
     */
    public static File findFile(File rootDir, final String fileName) {

        FilenameFilter filter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.equals(fileName);
            }
        };

        return findFile(rootDir, filter);
    }

    /**
     * Finds a file matching the given file name filter in the given root directory or any
     * subdirectory. The files and directories are scanned in alphabetical order, so the result is
     * deterministic.
     * <p>
     * The method returns the first matching result, if any, and ignores all other matches.
     * 
     * @param rootDir
     *            root directory
     * @param filter
     *            file name filter
     * @return matching file, or null
     */
    public static File findFile(File rootDir, FilenameFilter filter) {
        File[] files = rootDir.listFiles(filter);
        Arrays.sort(files);
        if (files.length > 0) {
            return files[0];
        }

        files = rootDir.listFiles(directoryFilter);
        Arrays.sort(files);
        for (File subDir : files) {
            File found = findFile(subDir, filter);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
