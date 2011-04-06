/*
 * Copyright 2008 Alin Dreghiciu.
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
package org.ops4j.pax.exam.container.externalframework.options;

import org.ops4j.pax.exam.options.MavenUrlReference;

/**
 * Factory methods for External framework options.
 *
 * @author Stephane Chomat
 */
public class ExternalFrameworkOptions 
{

    public static final String JAVA_RUNNER = "org.apache.karaf.testing.javarunner";
    public static final String JAVA_RUNNER_DEFAULT = "org.ops4j.pax.runner.platform.DefaultJavaRunner";
    public static final String JAVA_RUNNER_IN_PROCESS = "org.ops4j.pax.runner.platform.InProcessJavaRunner";


    
    public static String getVersion(MavenUrlReference ref) {
        String[] parts = ref.getURL().split("/");
        if (parts.length >= 3) {
            return parts[2];
        }
        throw new RuntimeException("Cannot find version from "+ref.getURL());
    }

}
