/*
 * Copyright 2010 - 2011 Toni Menzel.
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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExceptionHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.ExxamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.spi.container.PaxExamRuntime;
import org.ops4j.pax.exam.spi.container.PlumbingContext;
import org.ops4j.pax.exam.spi.driversupport.DefaultExamReactor;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

import static org.junit.Assert.*;

/**
 * This is the default Test Runner using Exxam plumbing API.
 * Its also the blueprint for custom, much more specific runners.
 * This will make a single probe bundling in all @Tests in this class.
 *
 * This uses the whole regression class as a single unit of tests with the following valid annotaions:
 * - @Configuration -> Configuration 1:N. Multiple configurations will result into multiple invokations of the same regression.
 * - @ProbeBuilder -> Customize the probe creation.
 * - @Test -> Single tests to be invoked. Note that in @Configuration you can specify the invokation strategy.
 */
public class JUnit4TestRunner extends BlockJUnit4ClassRunner {

    private static Logger LOG = LoggerFactory.getLogger( JUnit4TestRunner.class );

    final private StagedExamReactor m_reactor;
    final private Map<TestAddress, FrameworkMethod> m_map = new HashMap<TestAddress, FrameworkMethod>();
    final private Map<FrameworkMethod, TestAddress> m__childs = new HashMap<FrameworkMethod, TestAddress>();

    public JUnit4TestRunner( Class<?> klass )
        throws Exception
    {
        super( klass );

        m_reactor = prepareReactor();
    }

    @Override
    public void run( RunNotifier notifier )
    {
        try {
            super.run( notifier );
        } catch( Exception e ) {
            throw new TestContainerException( "Problem interacting with reactor.", e );
        } finally {
            m_reactor.tearDown();
        }
    }

    /**
     * We overwrite those with reactor content
     */
    @Override
    protected List<FrameworkMethod> getChildren()
    {
        if( m__childs.isEmpty() ) {
            fillChildren();
        }
        return Arrays.asList( m__childs.keySet().toArray( new FrameworkMethod[ m__childs.size() ] ) );
    }

    private void fillChildren()
    {
        Set<TestAddress> targets = m_reactor.getTargets();
        for( final TestAddress address : targets ) {
            FrameworkMethod frameworkMethod = m_map.get( address.root() );

            // now, someone later may refer to that artificial FrameworkMethod. We need to be able to tell the address.
            FrameworkMethod method = new FrameworkMethod( frameworkMethod.getMethod() ) {
                @Override
                public String getName()
                {
                    return address.caption();
                }

                @Override
                public boolean equals( Object obj )
                {
                    return address.equals( obj );
                }

                @Override
                public int hashCode()
                {
                    return address.hashCode();
                }
            };

            m__childs.put( method, address );
        }
    }

    @Override
    protected void collectInitializationErrors
        ( List<Throwable> errors )
    {
        // do nothing
    }

    private synchronized StagedExamReactor prepareReactor()
        throws Exception
    {

        Class testClass = getTestClass().getJavaClass();
        Object testClassInstance = testClass.newInstance();
        ExxamReactor reactor = getReactor( testClass );

        addConfigurationsToReactor( reactor, testClass, testClassInstance );
        addTestsToReactor( reactor, testClass, testClassInstance );
        return reactor.stage( getFactory( testClass ) );
    }

    private void addConfigurationsToReactor( ExxamReactor reactor, Class testClass, Object testClassInstance )
        throws IllegalAccessException, InvocationTargetException
    {
        Method[] methods = testClass.getMethods();
        for( Method m : methods ) {
            Configuration conf = m.getAnnotation( Configuration.class );
            if( conf != null ) {
                // consider as option, so prepare that one:
                LOG.info( "Add Configuration " + m.getName() );
                reactor.addConfiguration( (Option[]) m.invoke( testClassInstance ) );
            }
        }
    }

    private void addTestsToReactor( ExxamReactor reactor, Class testClass, Object testClassInstance )
        throws IOException, ExamConfigurationException
    {
        Properties extraProperties = new Properties();
        TestProbeBuilder probe = new PlumbingContext().createProbe( extraProperties );
        probe = overwriteWithUserDefinition( testClass, testClassInstance, probe );

        //probe.setAnchor( testClass );
        for( FrameworkMethod s : super.getChildren() ) {
            // record the method -> adress matching
            TestAddress address = delegateTest( testClassInstance, probe, s );
            if( address == null ) {
                address = probe.addTest( testClass, s.getMethod().getName() );
            }
            m_map.put( address, s );
        }
        reactor.addProbe( probe.build() );
    }

    private TestAddress delegateTest( Object testClassInstance, TestProbeBuilder probe, FrameworkMethod s )
    {
        try {
            Class<?>[] types = s.getMethod().getParameterTypes();
            if( types.length == 1 && types[ 0 ].isAssignableFrom( TestProbeBuilder.class ) ) {
                // its a delegate:
                // invoke:
                return (TestAddress) s.getMethod().invoke( testClassInstance, probe );

            }
            else {
                return null;
            }
        } catch( Exception e ) {
            throw new TestContainerException( "Problem delegating to test.", e );
        }
    }

    @SuppressWarnings( "unchecked" )
    private StagedExamReactorFactory getFactory( Class testClass )
        throws InstantiationException, IllegalAccessException
    {
        ExamReactorStrategy strategy = (ExamReactorStrategy) testClass.getAnnotation( ExamReactorStrategy.class );

        StagedExamReactorFactory fact;
        if( strategy != null ) {
            fact = strategy.value()[ 0 ].newInstance();
        }
        else {
            // default:
            fact = new AllConfinedStagedReactorFactory();
        }
        return fact;
    }

    private DefaultExamReactor getReactor( Class testClass )
        throws InstantiationException, IllegalAccessException
    {
        return new DefaultExamReactor( getExamFactory( testClass ) );
    }

    @SuppressWarnings( "unchecked" )
    private TestContainerFactory getExamFactory( Class testClass )
        throws IllegalAccessException, InstantiationException
    {
        ExamFactory f = (ExamFactory) testClass.getAnnotation( ExamFactory.class );

        TestContainerFactory fact;
        if( f != null ) {
            fact = f.value().newInstance();
        }
        else {
            // default:
            fact = PaxExamRuntime.getTestContainerFactory();
        }
        return fact;
    }

    protected synchronized Statement methodInvoker( final FrameworkMethod method, final Object test )
    {
        return new Statement() {

            @Override
            public void evaluate()
                throws Throwable
            {
                TestAddress address = m__childs.get( method );
                TestAddress root = address.root();

                LOG.info( "Invoke " + method.getName() + " @ " + address + " Arguments: " + root.arguments() );
                try {
                    m_reactor.invoke( address );
                } catch( Exception e ) {
                    Throwable t = ExceptionHelper.unwind( e );
                    fail( t.getMessage() );
                }
            }
        };

    }

    @Override
    protected void validatePublicVoidNoArgMethods( Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors )
    {

    }

    private TestProbeBuilder overwriteWithUserDefinition( Class testClass, Object instance, TestProbeBuilder probe )
        throws ExamConfigurationException
    {
        Method[] methods = testClass.getMethods();
        for( Method m : methods ) {
            ProbeBuilder conf = m.getAnnotation( ProbeBuilder.class );
            if( conf != null ) {
                // consider as option, so prepare that one:
                LOG.debug( "User defined probe hook found: " + m.getName() );
                TestProbeBuilder probeBuilder;
                try {
                    probeBuilder = (TestProbeBuilder) m.invoke( instance, probe );
                } catch( Exception e ) {
                    throw new ExamConfigurationException( "Invoking custom probe hook " + m.getName() + " failed", e );
                }
                if( probeBuilder != null ) {
                    return probe;
                }
                else {
                    throw new ExamConfigurationException( "Invoking custom probe hook " + m.getName() + " succeeded but returned null" );
                }

            }
        }
        LOG.debug( "No User defined probe hook found" );
        return probe;
    }
}
