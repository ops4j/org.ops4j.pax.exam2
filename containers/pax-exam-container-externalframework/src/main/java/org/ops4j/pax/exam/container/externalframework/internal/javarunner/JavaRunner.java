/*
 * Copyright 2008 Stuart McCulloch.
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.container.externalframework.internal.javarunner;

import java.io.File;

/**
 * Simple API for an external Java runner service.
 *
 * @author Stuart McCulloch
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since March 14, 2008
 */
public interface JavaRunner
{

    /**
     * Starts the selected Java program, up to service implementation whether it waits for it to exit.
     *
     * @param vmOptions      selected JVM options
     * @param classpath      application class path
     * @param mainClass      main program entry point
     * @param programOptions program specific options
     * @param javaHome       java home directory
     * @param workingDir     working directory
     *
     * @throws org.ops4j.pax.runner.platform.PlatformException
     *          if something goes wrong
     */
    void exec( String[] vmOptions,
               String[] classpath,
               String mainClass,
               String[] programOptions,
               String javaHome,
               File workingDir )
        throws Exception;

}
