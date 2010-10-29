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
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.TestTarget;
import org.ops4j.pax.exam.raw.extender.ProbeInvoker;
import org.ops4j.pax.exam.spi.probesupport.TestProbeBuilderImpl;
import org.ops4j.store.Store;
import org.ops4j.store.StoreFactory;

/**
 * @author Toni Menzel
 * @since Jan 11, 2010
 *        <p/>
 *        TODO: to be changed into service
 */
public class DefaultRaw
{

    private static Logger LOG = LoggerFactory.getLogger( DefaultRaw.class );

    
    private static final String PAX_EXAM_EXECUTABLE_SIG = "PaxExam-Executable-SIG";
    private static final String PROBE_SIGNATURE_KEY = "Probe-Signature";
    private static final int TIMEOUT_IN_MILLIS = 2000;
    private static int ID = 0;

    public static TestProbeBuilder createProbe( Properties p )
        throws IOException
    {
        Store<InputStream> store = StoreFactory.anonymousStore();
        return new TestProbeBuilderImpl( p, store );
    }

    public static TestProbeBuilder createProbe()
        throws IOException
    {
        Properties p = new Properties();
        return createProbe( p );
    }

    public static TestAddress call( Class clazz, String method )
    {
        return new ClassMethodTestAddress( getNextCallID(), clazz, method );
    }

    public static TestAddress[] call( Class clazz )
    {
        List<TestAddress> calls = new ArrayList<TestAddress>();
        for( String m : parseMethods( clazz ) )
        {
            calls.add( new ClassMethodTestAddress( getNextCallID(), clazz, m ) );
        }
        return calls.toArray( new TestAddress[ calls.size() ] );
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
            if( Modifier.isPublic( m.getModifiers() ) )
            {
                calls.add( m.getName() );
            }
            else
            {
                LOG.debug( "Skipping " + clazz.getName() + " Method " + m.getName() + " (not public)" );
            }
        }
        return calls.toArray( new String[ calls.size() ] );
    }

    public static void execute( TestTarget target, TestAddress address )
        throws InvocationTargetException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        assert ( target != null ) : "TestTarget must not be null.";
        assert ( address != null ) : "TestAddress must not be null.";

        String filterExpression = "(" + PROBE_SIGNATURE_KEY + "=" + address.signature() + ")";
        ProbeInvoker service = target.getService( ProbeInvoker.class, filterExpression, TIMEOUT_IN_MILLIS );
        service.call();
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

            public TestAddress[] getTests()
            {
                List<TestAddress> calls = new ArrayList<TestAddress>();
                for( final String test : testsSignatures )
                {
                    calls.add( new TestAddress()
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
                return calls.toArray( new TestAddress[ calls.size() ] );
            }

            public InputStream getStream()
            {
                return is;
            }
        };
    }

    private static String getNextCallID()
    {
        return PAX_EXAM_EXECUTABLE_SIG + ( ID++ );
    }
}
