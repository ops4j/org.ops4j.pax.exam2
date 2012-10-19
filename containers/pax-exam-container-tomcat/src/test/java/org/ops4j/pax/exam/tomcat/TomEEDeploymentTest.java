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
package org.ops4j.pax.exam.tomcat;

import static org.ops4j.pax.exam.CoreOptions.war;

import java.io.IOException;

import org.junit.Test;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.spi.PaxExamRuntime;

public class TomEEDeploymentTest
{

    @Test
    public void deployWebapp() throws IOException
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        ExamSystem system = PaxExamRuntime.createServerSystem( war( "mvn:org.apache.openejb/tomee-webapp/1.5.0/war" ).name( "tomee" ) );
        TomcatTestContainer container = new TomcatTestContainer( system );
        container.start();
        container.deployModules();
        container.stop();
    }
}
