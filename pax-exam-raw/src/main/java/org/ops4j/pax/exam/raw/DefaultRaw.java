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
package org.ops4j.pax.exam.raw;

import org.ops4j.pax.exam.raw.extender.ProbeInvoker;
import org.ops4j.pax.exam.raw.internal.ClassMethodProbeCall;
import org.ops4j.pax.exam.raw.internal.TestProbeBuilderImpl;
import org.ops4j.pax.exam.spi.container.TestContainer;

/**
 * @author Toni Menzel
 * @since Jan 11, 2010
 */
public class DefaultRaw
{

    public static TestProbeBuilder createProbe()
    {
        return new TestProbeBuilderImpl();
    }

    public static ProbeCall call( Class clazz, String method )
    {
        return new ClassMethodProbeCall( clazz, method );
    }

    public static void execute( TestContainer container, ProbeCall call )
    {

        ProbeInvoker probe1 = container.getService( ProbeInvoker.class, "(Probe-Signature=PaxExam-Executable-SIG1)" );

    }
}
