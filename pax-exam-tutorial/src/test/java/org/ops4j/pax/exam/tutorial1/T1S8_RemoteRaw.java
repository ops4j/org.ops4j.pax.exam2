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

import org.junit.Test;
import org.ops4j.pax.exam.container.def.internal.PaxRunnerTestContainerFactory;
import org.ops4j.pax.exam.spi.container.ProbeCall;
import org.ops4j.pax.exam.tutorial1.MyCode;
import org.ops4j.pax.exam.spi.container.TestProbeBuilder;
import org.ops4j.pax.exam.runtime.PaxExamRuntime;
import org.ops4j.pax.exam.spi.container.TestContainer;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.remote.RBCRemoteTargetOptions.*;
import static org.ops4j.pax.exam.spi.container.DefaultRaw.*;

/**
 * This demo shows how to not necessarily use the junit user interface but fully
 * control the lifecycle yourself
 *
 * @author Toni Menzel
 * @since Jan 26, 2010
 */
public class T1S8_RemoteRaw
{

    @Test
    public void testPlan()
        throws Exception
    {
        TestContainer testTarget = PaxExamRuntime.getTestContainerFactory( PaxRunnerTestContainerFactory.class ).newInstance(
            options(
                waitForRBCFor( 2000 )
                //location( "192.168.73.204", 8181 )
            )
        ).start();

        try
        {
            TestProbeBuilder probe = createProbe().addTest( MyCode.class );
            testTarget.install( probe.build() );

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
