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
package org.ops4j.pax.exam.junit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.ExxamReactor;
import org.ops4j.pax.exam.spi.ProbeCall;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.TestProbeBuilder;
import org.ops4j.pax.exam.spi.container.DefaultRaw;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;
import org.ops4j.pax.exam.spi.driversupport.DefaultExamReactor;

import static org.ops4j.pax.exam.spi.container.DefaultRaw.createProbe;

public class JUnit4TestRunner extends BlockJUnit4ClassRunner
{

    private static Log LOG = LogFactory.getLog( JUnit4TestRunner.class );

    private final StagedExamReactor m_reactor;
    private final Map<FrameworkMethod, ProbeCall> m_map;

    public JUnit4TestRunner( Class<?> klass )
        throws InitializationError
    {
        super( klass );
        m_map = new HashMap<FrameworkMethod, ProbeCall>();
        try
        {
            m_reactor = prepareReactor();
        } catch( Exception e )
        {
            e.printStackTrace();
            throw new InitializationError( e );
        }
    }

    private StagedExamReactor prepareReactor()
        throws Exception
    {
        ExxamReactor reactor = getReactor();
        Class testClass = getTestClass().getJavaClass();

        Object testClassInstance = testClass.newInstance();
        Method[] methods = testClass.getDeclaredMethods();
        for( Method m : methods )
        {

            Configuration conf = (Configuration) m.getAnnotation( Configuration.class );
            if( conf != null )
            {
                // consider as option, so prepare that one:
                LOG.debug( "Add Configuration " + m.getName() );
                reactor.addConfiguration( (Option[]) m.invoke( testClassInstance ) );
            }
        }

        // add the tests to run as well:
        TestProbeBuilder probe = createProbe();
        probe.setAnchor( testClass );
        for( FrameworkMethod s : getChildren() )
        {
            LOG.debug( "Add Test " + s.getName() );
            ProbeCall call = save( s, DefaultRaw.call( testClass, s.getName() ) );
            probe.addTest( call );
        }
        reactor.addProbe( probe );
        // finally stage it
        return reactor.stage();
    }

    private DefaultExamReactor getReactor()
    {
        return new DefaultExamReactor( PaxExamRuntime.getTestContainerFactory() );
    }

    private ProbeCall save( FrameworkMethod fwMethod, ProbeCall call )
    {
        m_map.put( fwMethod, call );
        return call;
    }

    // overwrite call chain

    protected Statement methodInvoker( final FrameworkMethod method, final Object test )
    {
        return new Statement()
        {

            @Override
            public void evaluate()
                throws Throwable
            {
                // usually we just need the signature here.
                m_reactor.invoke( findMatchingCall( method, test ) );
            }
        };

    }

    protected ProbeCall findMatchingCall( FrameworkMethod method, Object test )
    {
        return m_map.get( method );
    }

    @Override
    protected List<MethodRule> rules( Object test )
    {
        List<MethodRule> rules = new ArrayList<MethodRule>();
        rules.add( new ExamRule( test ) );
        rules.addAll( super.rules( test ) );

        return rules;
    }

    protected void validateTestMethods( List<Throwable> errors )
    {
    }

    protected Statement methodBlock( FrameworkMethod method )
    {

        Object test;
        try
        {
            test = new ReflectiveCallable()
            {
                @Override
                protected Object runReflectiveCall()
                    throws Throwable
                {
                    return createTest();
                }
            }.run();
        } catch( Throwable e )
        {
            return new Fail( e );
        }

        Statement statement = methodInvoker( method, test );

        return statement;
    }

}
