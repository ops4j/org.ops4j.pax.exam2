/*
 * Copyright 2008 Alin Dreghiciu
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
package org.ops4j.pax.exam.plugintest;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.MavenUtils.*;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * How to use the maven plugin for determining maven artifact version integration tests.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, November 18, 2008
 */
@RunWith( JUnit4TestRunner.class )
public class GenerateDependsFileTest
{

    /**
     * Pax Exam test options that provisions the Pax URL handlers bundle suing the version determined from project.
     *
     * @return integration tests options
     */
    @Configuration
    public static Option[] configureForValidURL()
    {
        return options(
            provision(
                mavenBundle().groupId( "org.ops4j.base" ).artifactId( "ops4j-base-lang" ).version( asInProject() ),
                mavenBundle( "org.ops4j.base", "ops4j-base-lang" ).version( asInProject() ),
                mavenBundle( "org.ops4j.base", "ops4j-base-lang" ).versionAsInProject()
            ),
            logProfile()
        );
    }

    /**
     * The test does not need to do anything because it will just fail if the version above cannot be resolved.
     */
    @Test
    public void provisioned()
    {
        //does nothing
    }

}