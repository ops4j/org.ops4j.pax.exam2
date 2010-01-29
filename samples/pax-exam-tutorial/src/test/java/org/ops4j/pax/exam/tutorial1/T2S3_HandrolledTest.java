/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.tutorial1;

import java.io.InputStream;
import java.net.URL;
import org.junit.Test;
import org.ops4j.pax.exam.container.def.internal.PaxRunnerTestContainerFactory;
import org.ops4j.pax.exam.runtime.PaxExamRuntime;
import org.ops4j.pax.exam.spi.container.ProbeCall;
import org.ops4j.pax.exam.spi.container.TestContainer;
import org.ops4j.pax.exam.spi.container.TestProbeProvider;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.*;
import static org.ops4j.pax.exam.container.remote.RBCRemoteTargetOptions.*;
import static org.ops4j.pax.exam.spi.container.DefaultRaw.*;

/**
 * @author Toni Menzel
 * @since Jan 29, 2010
 */
public class T2S3_HandrolledTest
{

    @Test
    public void testPlan()
        throws Exception
    {
        TestContainer testTarget = PaxExamRuntime.getTestContainerFactory( PaxRunnerTestContainerFactory.class ).newInstance(
            options(
                // waitForRBCFor( 2000 )
                //location( "192.168.73.204", 8181 )
                logProfile(),
                rawPaxRunnerOption( "log", "debug" ),
                mavenBundle().groupId( "org.apache.felix" ).artifactId( "org.apache.felix.dependencymanager" ).version( "3.0.0-SNAPSHOT" )
            )
        ).start();

        try
        {
            TestProbeProvider probe = new TestProbeProvider()
            {

                public ProbeCall[] getTests()
                {
                    return new ProbeCall[]{
                        new ProbeCall()
                        {

                            public String getInstruction()
                            {
                                return null;
                            }

                            public String signature()
                            {
                                return "mytest";
                            }
                        }
                    };
                }

                public InputStream getStream()
                {
                    return fromURL( "file:samples/probe1/target/samples.probe1-2.0.0-SNAPSHOT.jar" );
                    //return fromURL( "mvn:org.ops4j.pax.exam/samples-probe1/2.0.0-SNAPSHOT" );
                }
            };

            testTarget.install( probe.getStream() );
            Thread.sleep( 5000 );
            for( ProbeCall call : probe.getTests() )
            {
                execute( testTarget, call );
            }
        } finally
        {
            testTarget.stop();
        }
    }


}
