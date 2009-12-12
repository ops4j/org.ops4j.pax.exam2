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
package org.ops4j.pax.exam.it;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
import org.ops4j.pax.exam.junit.AppliesTo;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

/**
 * Pax Runner options integration tests.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, November 17, 2008
 */
@RunWith( JUnit4TestRunner.class )
public class PaxRunnerOptionsTest
{

    /**
     * Pax Exam test options that adds a log profile.
     * Valid for all test methods.
     *
     * @return test options
     */
    @Configuration
    public static Option[] configure()
    {
        return options(
            logProfile().version( "1.3.0" )
        );
    }

    /**
     * Pax Exam test options that adds provisioning via a raw scanner.
     * Valid for test methods that starts with "raw".
     *
     * @return test options
     */
    @Configuration
    @AppliesTo( "raw.*" )
    public static Option[] configureRawScanner()
    {
        return options(
            scan( "scan-dir:foo/bar" ).noStart().update().startLevel( 10 )
        );
    }

    /**
     * TODO what we can test here?
     */
    @Test
    @Ignore
    public void rawScanner()
    {
    }

    /**
     * Pax Exam test options that adds provisioning via a directory scanner.
     * Valid for test methods that starts with "dir".
     *
     * @return test options
     */
    @Configuration
    @AppliesTo( "dir.*" )
    public static Option[] configureDirScanner()
    {
        return options(
            scanDir( "/foo/bar/" ).filter( "*.jar" ).noStart().update().startLevel( 10 )
        );
    }

    /**
     * TODO what we can test here?
     */
    @Test
    @Ignore
    public void dirScanner()
    {
    }

    /**
     * Pax Exam test options that adds provisioning via a bundle scanner.
     * Valid for test methods that starts with "bundle".
     *
     * @return test options
     */
    @Configuration
    @AppliesTo( "bundle.*" )
    public static Option[] configureBundleScanner()
    {
        return options(
            scanBundle( "file:/foo/bar.jar" ).noStart().update().startLevel( 10 ),
            scanBundle( bundle( "file:bar/foo.jar" ) ).noStart().update().startLevel( 10 ),
            scanBundle( mavenBundle().groupId( "foo" ).artifactId( "bar" ) ).noStart().update().startLevel( 10 )
        );
    }

    /**
     * TODO what we can test here?
     */
    @Test
    @Ignore
    public void bundleScanner()
    {
    }

    /**
     * Pax Exam test options that sets a custom maven repository.
     * Repositories itself can be configured.
     * For example it is possible to allow snapshot versions as well.
     *
     * Valid for test methods that starts with "repository".
     *
     * @return test options
     */
    @Configuration
    @AppliesTo( "repository.*" )
    public static Option[] configureRepositories()
    {
        return options(
            repository( "http://repository.ops4j.org/mvn-snapshots" ).disableReleases().allowSnapshots(),
            repository( "http://repo1.maven.org/maven2/" )
        );
    }

    /**
     * TODO what we can test here?
     */
    @Test
    public void repositoryTest()
    {

    }
}