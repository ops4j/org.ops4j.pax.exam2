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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaVersionUtil {

    private static final Pattern MAJOR_VERSION_PATTERN = Pattern.compile("^([0-9]+)[^0-9].*");

    private static final Pattern ONE_VERSION_PATTERN = Pattern.compile("^1\\.([0-9]+)[^0-9].*");

    public static int getMajorVersion(String javaVersion) {
        Matcher majorVersionMatcher = MAJOR_VERSION_PATTERN.matcher(javaVersion);

        if (!majorVersionMatcher.matches()) {
            throw getUnableToParseVersionException(javaVersion);
        }

        int majorVersion = Integer.parseInt(majorVersionMatcher.group(1));

        // we are in the case of Java >= 9
        if (majorVersion > 1) {
            return majorVersion;
        }

        // we are in the case of Java < 9 (1.7, 1.8)
        Matcher oneVersionMatcher = ONE_VERSION_PATTERN.matcher(javaVersion);
        if (!oneVersionMatcher.matches()) {
            throw getUnableToParseVersionException(javaVersion);
        }

        return Integer.parseInt(oneVersionMatcher.group(1));
    }

    private static IllegalArgumentException getUnableToParseVersionException(String version) {
        return new IllegalArgumentException(
                String.format(Locale.ROOT, "We are unable to parse Java version '%1$s'.", version));
    }
}
