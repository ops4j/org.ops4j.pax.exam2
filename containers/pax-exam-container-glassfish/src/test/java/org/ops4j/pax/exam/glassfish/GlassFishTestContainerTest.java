/*
 * Copyright 2012 Harald Wellmann
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
package org.ops4j.pax.exam.glassfish;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.mavenWar;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;

import java.util.Iterator;

import javax.inject.Inject;

import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.BundleContext;

@RunWith( PaxExam.class )
@ExamReactorStrategy( PerClass.class )
public class GlassFishTestContainerTest
{
    @Inject
    private BundleContext bc;

    @Inject
    private GlassFish gf;

    @Configuration( )
    public Option[] config()
    {
        return options(
            systemProperty( "osgi.console" ).value( "6666" ),
            mavenWar( "org.ops4j.pax.exam.samples", "pax-exam-sample1-web", 
                Info.getPaxExamVersion() ).name( "sample1" ),
            junitBundles() );
    }

    @Test
    public void getBundleContext()
    {
        assertThat( bc, is( notNullValue() ) );
    }

    @Test
    public void getGlassFish()
    {
        assertThat( gf, is( notNullValue() ) );
    }

    @Test
    public void getDeployedApplications() throws GlassFishException
    {
        Deployer deployer = gf.getDeployer();
        Iterator<String> applications = deployer.getDeployedApplications().iterator();
        assertThat( applications.hasNext(), is(true));
        String applicationName = applications.next();
        assertThat( applicationName, is("sample1"));
    }
}
