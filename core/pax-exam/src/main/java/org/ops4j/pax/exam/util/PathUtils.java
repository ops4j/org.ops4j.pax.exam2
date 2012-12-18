/*
 * Copyright (C) 2011 Harald Wellmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.util;

public class PathUtils {

    /**
     * Returns the base directory of the current Maven project, used by tests that require absolute
     * paths.
     * <p>
     * This works both when running under Surefire and when runnning a JUnit test from Eclipse,
     * assuming the JUnit launcher default was not changed by the user.
     * 
     * @return base directory of current project
     */
    public static String getBaseDir() {
        // 'basedir' is set by Maven Surefire. It always points to the current subproject,
        // even in reactor builds.
        String baseDir = System.getProperty("basedir");

        // if 'basedir' is not set, try the current directory
        if (baseDir == null) {
            baseDir = System.getProperty("user.dir");
        }
        return baseDir;
    }
}
