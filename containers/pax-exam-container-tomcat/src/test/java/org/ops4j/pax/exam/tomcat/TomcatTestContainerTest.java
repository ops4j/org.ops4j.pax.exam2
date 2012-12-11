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

import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.war;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.spi.DefaultExamSystem;

public class TomcatTestContainerTest
{

    @Test
    public void launchTomcat() throws IOException
    {
        ExamSystem system = DefaultExamSystem.create( options() );
        TomcatTestContainer container = new TomcatTestContainer( system );
        container.start();
        container.stop();
    }
    
    @Test
    public void deployWebapp() throws IOException
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        ExamSystem system = DefaultExamSystem.create( options( war( "mvn:org.apache.wicket/wicket-examples/1.5.3/war" ).name( "wicket-examples" ) ) );
        TomcatTestContainer container = new TomcatTestContainer( system );
        container.start();
        container.deployModules();
        container.stop();
    }
    
    @Test
    public void deployWebappFromStream() throws IOException
    {
        System.setProperty( "java.protocol.handler.pkgs", "org.ops4j.pax.url" );
        ExamSystem system = DefaultExamSystem.create( options() );
        TomcatTestContainer container = new TomcatTestContainer( system ) ;
        container.start();
        InputStream is = new URL("mvn:org.apache.wicket/wicket-examples/1.5.3/war").openStream();
        container.install( is );
        is.close();
        container.stop();
    }
}
