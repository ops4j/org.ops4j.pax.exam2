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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExceptionHelper;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDirectory;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.util.Injector;
import org.ops4j.pax.exam.util.InjectorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the default Test Runner using the Exam plumbing API.
 * Its also the blueprint for custom, much more specific runners.
 * This will make a single probe bundling in all @Tests in this class.
 *
 * This uses the whole regression class as a single unit of tests with the following valid annotations:
 * - @Configuration -> Configuration 1:N. Multiple configurations will result in multiple invocations of the same regression.
 * - @ProbeBuilder -> Customize the probe creation.
 * - @Test -> Single tests to be invoked. Note that in @Configuration you can specify the invocation strategy.
 * 
 * @author Toni Menzel
 * @author Harald Wellmann
 */
public class JUnit4TestRunner extends BlockJUnit4ClassRunner {

    private static Logger LOG = LoggerFactory.getLogger( JUnit4TestRunner.class );

    private StagedExamReactor m_reactor;
    final static private Map<TestAddress, FrameworkMethod> m_map = new HashMap<TestAddress, FrameworkMethod>();
    final private Map<FrameworkMethod, TestAddress> m_children = new HashMap<FrameworkMethod, TestAddress>();

	private ReactorManager m_manager = new ReactorManager();

    private static TestProbeBuilder probe;

    private boolean useProbeInvoker;

    public JUnit4TestRunner( Class<?> klass )
        throws Exception
    {
        super( klass );
        Object testClassInstance = klass.newInstance();

        ExamReactor examReactor = m_manager.prepareReactor(klass, testClassInstance);
        useProbeInvoker = ! m_manager.getSystemType().equals( Constants.EXAM_SYSTEM_CDI );
        if (useProbeInvoker)
        {
            addTestsToReactor( examReactor, klass, testClassInstance );
        }
        m_reactor = m_manager.stageReactor();
    }

    @Override
    public void run( RunNotifier notifier )
    {
        try {
            m_reactor.setUp();
            super.run( notifier );
        } catch( Exception e ) {
            throw new TestContainerException( "Problem interacting with reactor.", e );
        } finally {
            m_manager.shutdown();
        }
    }

    /**
     * Override to avoid running BeforeClass and AfterClass by the driver.
     * They shall only be run by the container.
     */
    protected Statement classBlock(final RunNotifier notifier) {
        if (useProbeInvoker)
        {
            Statement statement= childrenInvoker(notifier);
            return statement;
        }
        else
        {
            return super.classBlock( notifier );
        }
    }

    /**
     * Override to avoid running Before, After and Rule methods by the driver.
     * They shall only be run by the container.
     */
    protected Statement methodBlock(FrameworkMethod method) {
        if (!useProbeInvoker)
        {
            return super.methodBlock( method );
        }
        
        Object test;
        try {
            test= new ReflectiveCallable() {
                @Override
                protected Object runReflectiveCall() throws Throwable {
                    return createTest();
                }
            }.run();
        } catch (Throwable e) {
            return new Fail(e);
        }

        Statement statement= methodInvoker(method, test);
        return statement;
    }

    
    /**
     * We overwrite those with reactor content
     */
    @Override
    protected List<FrameworkMethod> getChildren()
    {
        if (! useProbeInvoker)
        {
            return super.getChildren();
        }
        
        if( m_children.isEmpty() ) {
            fillChildren();
        }
        return Arrays.asList( m_children.keySet().toArray( new FrameworkMethod[ m_children.size() ] ) );
    }

    private void fillChildren()
    {
        Set<TestAddress> targets = m_reactor.getTargets();
        TestDirectory testDirectory = TestDirectory.getInstance();
        for( final TestAddress address : targets ) {
            final FrameworkMethod frameworkMethod = m_map.get( address.root() );
            String className = frameworkMethod.getMethod().getDeclaringClass().getName();
            String methodName = frameworkMethod.getName();
            
            if (! className.equals( getTestClass().getName() )) {
                continue;
            }
            
            // now, someone later may refer to that artificial FrameworkMethod. We need to be able to tell the address.
            FrameworkMethod method = new FrameworkMethod( frameworkMethod.getMethod() ) {
                @Override
                public String getName()
                {
                    return frameworkMethod.getName() + ":" + address.caption();
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
            testDirectory.add( address, new TestInstantiationInstruction( className + ";" + methodName));

            m_children.put( method, address );
        }
    }

    @Override
    protected void collectInitializationErrors
        ( List<Throwable> errors )
    {
        // do nothing
    }

    private void addTestsToReactor( ExamReactor reactor, Class<?> testClass, Object testClassInstance )
        throws IOException, ExamConfigurationException
    {
        if (probe == null)
        {
            probe = m_manager.createProbe( testClassInstance );
        }

        //probe.setAnchor( testClass );
        for( FrameworkMethod s : super.getChildren() ) {
            // record the method -> adress matching
            TestAddress address = delegateTest( testClassInstance, probe, s );
            if( address == null ) {
                address = probe.addTest( testClass, s.getMethod().getName() );
            }
            m_map.put( address, s );
        }
        reactor.addProbe( probe );
    }

    private TestAddress delegateTest( Object testClassInstance, TestProbeBuilder probe, FrameworkMethod s )
    {
        try {
            Class<?>[] types = s.getMethod().getParameterTypes();
            if( types.length == 1 && types[ 0 ].isAssignableFrom( TestProbeBuilder.class ) ) {
                // do some backtracking:
                return (TestAddress) s.getMethod().invoke( testClassInstance, probe );

            }
            else {
                return null;
            }
        } catch( Exception e ) {
            throw new TestContainerException( "Problem delegating to test.", e );
        }
    }

    protected synchronized Statement methodInvoker( final FrameworkMethod method, final Object test )
    {
        if (!useProbeInvoker)
        {
            return super.methodInvoker( method, test );
        }
        
        return new Statement() {

            @Override
            public void evaluate()
                throws Throwable
            {
                TestAddress address = m_children.get( method );
                TestAddress root = address.root();

                LOG.debug( "Invoke " + method.getName() + " @ " + address + " Arguments: " + root.arguments() );
                try {
                    m_reactor.invoke( address );
                } catch( Exception e ) {
                    Throwable t = ExceptionHelper.unwind( e );
                    throw t;
                }
            }
        };
    }
    
    @Override
    protected Object createTest() throws Exception {
        if (useProbeInvoker) {
            return super.createTest();
        }
        else {
            Object test = super.createTest();
            inject(test);
            return test;
        }
    }

    private void inject(Object test) {
        Injector injector = findInjector();
        injector.injectFields(null, test);
    }

    private Injector findInjector() {
        Iterator<InjectorFactory> it = ServiceLoader.load(InjectorFactory.class).iterator();
        if (it.hasNext()) {
            InjectorFactory injectorFactory = it.next();
            return injectorFactory.createInjector( );
        }
        throw new IllegalStateException("no InjectorFactory implementation found in META-INF/services");
    }

}
