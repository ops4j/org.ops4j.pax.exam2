/*
 * Copyright 2009 Alin Dreghiciu
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
package org.ops4j.pax.exam.it;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * Boot classpath option integration tests.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.5.0, April 29, 2009
 */
@RunWith( JUnit4TestRunner.class )
public class BootClasspathLibraryOptionTest
{

    /**
     * Pax Exam regression options that adds servlet api to boot classpath before the framework.
     *
     * @return integration tests options
     */
    @Configuration
    public static Option[] configureBefore()
    {
        return options(
            bootClasspathLibrary(
                maven().groupId( "javax.servlet" ).artifactId( "servlet-api" ).version( "2.5" )
            ).beforeFramework(),
            systemPackage( "javax.servlet" )
        );
    }

    /**
     * Pax Exam regression options that adds servlet api to boot classpath after the framework (default).
     *
     * @return integration tests options
     */
    @Configuration
    public static Option[] configureAfter()
    {
        return options(
            bootClasspathLibrary(
                maven().groupId( "javax.servlet" ).artifactId( "servlet-api" ).version( "2.5" )
            ).afterFramework(),
            systemPackage( "javax.servlet" )
        );
    }

    @Test
    public void packageAvailableBefore()
        throws ClassNotFoundException
    {
        getClass().getClassLoader().loadClass( "javax.servlet.Servlet" );
    }

    @Test
    public void packageAvailableAfter()
        throws ClassNotFoundException
    {
        getClass().getClassLoader().loadClass( "javax.servlet.Servlet" );
    }

}