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
package org.ops4j.pax.exam.spi.container;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.ops4j.pax.exam.TestTarget;
import org.ops4j.pax.exam.raw.extender.ProbeInvoker;
import org.ops4j.pax.exam.spi.ProbeCall;
import org.ops4j.pax.exam.spi.TestProbeBuilder;
import org.ops4j.pax.exam.spi.TestProbeProvider;
import org.ops4j.pax.exam.spi.probesupport.TestProbeBuilderImpl;

/**
 * @author Toni Menzel
 * @since Jan 11, 2010
 *
 *        TODO: to be changed into service
 */
public class DefaultRaw
{

    private static int id = 0;
    private static final String PAX_EXAM_EXECUTABLE_SIG = "PaxExam-Executable-SIG";
    private static final String PROBE_SIGNATURE_KEY = "Probe-Signature";

    public static TestProbeBuilder createProbe()
    {
        return new TestProbeBuilderImpl();
    }

    public static ProbeCall call( Class clazz, String method )
    {
        return new ClassMethodProbeCall( PAX_EXAM_EXECUTABLE_SIG + ( id++ ), clazz, method );
    }

    public static ProbeCall[] call( Class clazz )
    {
        List<ProbeCall> calls = new ArrayList<ProbeCall>();
        for( String m : parseMethods( clazz ) )
        {
            calls.add( new ClassMethodProbeCall( PAX_EXAM_EXECUTABLE_SIG + ( id++ ), clazz, m ) );
        }
        return calls.toArray( new ProbeCall[calls.size()] );
    }

    /**
     * parse test methods using reflection
     *
     * @param clazz
     */
    private static String[] parseMethods( Class clazz )
    {
        List<String> calls = new ArrayList<String>();

        for( Method m : clazz.getDeclaredMethods() )
        {
            calls.add( m.getName() );
        }
        return calls.toArray( new String[calls.size()] );
    }

    public static void execute( TestTarget target, ProbeCall call )
        throws InvocationTargetException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {

        assert ( target != null ) : "TestTarget must not be null.";
        target.getService( ProbeInvoker.class, "(" + PROBE_SIGNATURE_KEY + "=" + call.signature() + ")", 0 ).call();
    }

    public static InputStream fromURL( String s )
    {
        try
        {
            return new URL( s ).openStream();
        } catch( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static TestProbeProvider probe( final InputStream is, final String... testsSignatures )
    {
        return new TestProbeProvider()
        {

            public ProbeCall[] getTests()
            {
                List<ProbeCall> calls = new ArrayList<ProbeCall>();
                for( final String test : testsSignatures )
                {
                    calls.add( new ProbeCall()
                    {
                        public String getInstruction()
                        {
                            return null;
                        }

                        public String signature()
                        {
                            return test;
                        }
                    }
                    );
                }
                return calls.toArray( new ProbeCall[calls.size()] );
            }

            public InputStream getStream()
            {
                return is;
            }
        };
    }

}
