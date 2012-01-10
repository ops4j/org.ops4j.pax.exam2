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
package org.ops4j.pax.exam.regression.multi.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import static org.ops4j.pax.exam.CoreOptions.cleanCaches;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.repository;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.isPaxRunnerContainer;
import static org.ops4j.pax.exam.regression.multi.RegressionConfiguration.regressionDefaults;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * Tests the repository() option for using external Maven repositories. 
 * NOTE: This test is not fail-safe. If the bundle happens to be in the local repository 
 * already, the test passes, even though remote access may be broken.
 * 
 * @author Harald Wellmann
 */
@RunWith( JUnit4TestRunner.class )
@ExamReactorStrategy( AllConfinedStagedReactorFactory.class )
public class RepositoryTest
{

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public static Option[] configuration() throws Exception
    {
        return options( //
            regressionDefaults(),
            repository( "http://www.knopflerfish.org/maven2" ).id("knopflerfish"),
            cleanCaches(),
            mavenBundle( "org.knopflerfish.bundle", "demo1", "2.0.0" ),
            junitBundles() );
    }

    @Test
    public void bundleFromExternalRepositoryIsResolved() throws Exception
    {
        assumeThat(isPaxRunnerContainer(), is(true));

        Bundle[] bundles = bundleContext.getBundles();
        boolean demoBundleFound = false;
        for ( Bundle bundle : bundles )
        {
            if( bundle.getSymbolicName().equals( "org.knopflerfish.bundle.demo1" ) )
            {
                demoBundleFound = true;
            }
        }
        assertThat( demoBundleFound, is( true ) );
    }
}
