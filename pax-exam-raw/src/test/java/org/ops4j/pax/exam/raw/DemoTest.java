/*
 * Copyright 2010 Toni Menzel.
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
package org.ops4j.pax.exam.raw;

import org.junit.Test;
import org.ops4j.pax.exam.runtime.PaxExamRuntime;
import org.ops4j.pax.exam.spi.container.TestTarget;

import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.container.remote.RBCRemoteTargetOptions.*;
import static org.ops4j.pax.exam.raw.DefaultRaw.*;

/**
 * @author Toni Menzel
 * @since Dec 2, 2009
 */
public class DemoTest
{

    @Test
    public void testPlan()
        throws Exception
    {
        TestTarget testTarget = PaxExamRuntime.getTestTargetFactory().newInstance(
            options(
                waitForRBCFor( 2000 ),
                location( "localhost", 9191 )
            )
        );

        long probeId = 0;
        try
        {
            TestProbeBuilder probe = createProbe().addTest( MyCode.class );
            probeId = testTarget.installBundle( probe.build() );

            for( ProbeCall call : probe.getTests() )
            {
                execute( testTarget, call );
            }

        } finally
        {
            testTarget.uninstallBundle( probeId );
            stopIfPossible( testTarget );
        }
    }


}
