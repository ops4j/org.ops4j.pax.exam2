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

import java.lang.reflect.InvocationTargetException;
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
    private static int id = 0;

    public static TestProbeBuilder createProbe()
    {
        return new TestProbeBuilderImpl();
    }

    public static ProbeCall call( Class clazz, String method )
    {
        return new ClassMethodProbeCall( "PaxExam-Executable-SIG" + ( id++ ), clazz, method );
    }

    public static void execute( TestContainer container, ProbeCall call )
        throws InvocationTargetException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        container.getService( ProbeInvoker.class, "(Probe-Signature=" + call.signature() + ")" ).call();
    }
}
