/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.karaf.container.internal;

import java.util.Locale;

public class JavaVersionUtil {

    public static int getMajorVersion() {
        return getMajorVersion(System.getProperty("java.specification.version"));

    }

    /**
     * Only used for testing. Use {@link #getMajorVersion()} instead. Checkout http://openjdk.java.net/jeps/223 for more information
     * @param javaSpecVersion System.getProperty("java.specification.version")
     * @return the current major Java version.
     */
    static int getMajorVersion(String javaSpecVersion) {
        try {
            if (javaSpecVersion.contains(".")) { //before jdk 9
                return Integer.parseInt(javaSpecVersion.split("\\.")[1]);
            } else {
                return Integer.parseInt(javaSpecVersion);
            }
        } catch (NumberFormatException e ){
            throw getUnableToParseVersionException(javaSpecVersion);
        }
    }

    private static IllegalArgumentException getUnableToParseVersionException(String version) {
        return new IllegalArgumentException(
                String.format(Locale.ROOT, "We are unable to parse Java version '%1$s'.", version));
    }
}
