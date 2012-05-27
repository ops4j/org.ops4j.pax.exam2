/*
 * Copyright 2011 Harald Wellmann
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
package org.ops4j.pax.exam.testng.listener;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.ExceptionHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.DefaultExamReactor;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.PaxExamRuntime;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.spi.reactors.EagerSingleStagedReactorFactory;
import org.ops4j.pax.exam.testng.Configuration;
import org.ops4j.pax.exam.testng.ExamFactory;
import org.ops4j.pax.exam.testng.ExamReactorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.MethodInstance;

// @MetaInfServices(ITestNGListener.class)
public class ExamTestNGListener implements ISuiteListener, IMethodInterceptor, IHookable
{
    private static Logger LOG = LoggerFactory.getLogger( ExamTestNGListener.class );

    private StagedExamReactor reactor;

    private ExamSystem system;
    

    private Map<TestAddress, ITestNGMethod> addressToMethodMap = new HashMap<TestAddress, ITestNGMethod>();
    private Map<String, TestAddress> methodToAddressMap = new HashMap<String, TestAddress>();
    

    public void onStart( ISuite suite )
    {
        try
        {
            reactor = prepareReactor(suite);
            reactor.beforeClass();
        }
        catch ( Exception exc )
        {
            throw new TestContainerException( exc );
        }
    }

    public void onFinish( ISuite suite )
    {
        reactor.afterClass();
    }

    private synchronized StagedExamReactor prepareReactor( ISuite suite )
        throws Exception
    {
        system = PaxExamRuntime.createTestSystem();
        List<ITestNGMethod> methods = suite.getAllMethods();
        Class<?> testClass = methods.get( 0 ).getRealClass();
        
        Object testClassInstance = testClass.newInstance();
        ExamReactor reactor = getReactor( testClass );

        addConfigurationsToReactor( reactor, testClass, testClassInstance );
        addTestsToReactor( reactor, testClass, methods );
        return reactor.stage( getFactory( testClass ) );
    }

    private void addConfigurationsToReactor( ExamReactor reactor, Class<?> testClass, Object testClassInstance )
        throws IllegalAccessException, InvocationTargetException, IllegalArgumentException, IOException
    {
        Method[] methods = testClass.getMethods();
        for ( Method m : methods )
        {
            Configuration conf = m.getAnnotation( Configuration.class );
            if( conf != null )
            {
                // consider as option, so prepare that one:
                reactor.addConfiguration( ( (Option[]) m.invoke( testClassInstance ) ) );
            }
        }
    }

    private void addTestsToReactor( ExamReactor reactor, Class<?> testClass, List<ITestNGMethod> methods )
        throws IOException
    {
        TestProbeBuilder probe = system.createProbe();
        //probe = overwriteWithUserDefinition( testClass, testClassInstance, probe );

        
        // probe.setAnchor( testClass );
        for ( ITestNGMethod m : methods )
        {
            // record the method -> adress matching
            TestAddress address = probe.addTest( testClass, m.getMethodName() );
            addressToMethodMap.put( address, m );
            //methodToAddressMap.put (m.getMethodName(), address );
        }
        reactor.addProbe( probe );
    }

    private StagedExamReactorFactory getFactory( Class<?> testClass )
        throws InstantiationException, IllegalAccessException
    {
        ExamReactorStrategy strategy = testClass.getAnnotation( ExamReactorStrategy.class );

        StagedExamReactorFactory fact;
        if( strategy != null ) {
            fact = strategy.value()[ 0 ].newInstance();
        }
        else {
            // default:
            fact = new EagerSingleStagedReactorFactory();
        }
        return fact;
    }

    private DefaultExamReactor getReactor( Class<?> testClass )
        throws InstantiationException, IllegalAccessException
    {
        return new DefaultExamReactor( system, getExamFactory( testClass ) );
    }

    private TestContainerFactory getExamFactory( Class<?> testClass )
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

    public void run( IHookCallBack callBack, ITestResult testResult )
    {
        LOG.info( "running {}", testResult.getName() );
        //callBack.runTestMethod( testResult );
        
        TestAddress address = methodToAddressMap.get( testResult.getName() );
        TestAddress root = address.root();
        

        LOG.debug( "Invoke " + testResult.getName() + " @ " + address + " Arguments: " + root.arguments() );
        try {
            reactor.invoke( address );
            testResult.setStatus( ITestResult.SUCCESS );
        } catch( Exception e ) {
            Throwable t = ExceptionHelper.unwind( e );
            LOG.error("Exception" ,e);
            testResult.setStatus( ITestResult.FAILURE );
            testResult.setThrowable( t );
        }        
    }

    public List<IMethodInstance> intercept( List<IMethodInstance> methods, ITestContext context )
    {
        List<IMethodInstance> newInstances = new ArrayList<IMethodInstance>();
        Set<TestAddress> targets = reactor.getTargets();
        for( TestAddress address : targets ) {
            ITestNGMethod frameworkMethod = addressToMethodMap.get( address.root() );

            
            Method javaMethod = frameworkMethod.getConstructorOrMethod().getMethod();
            ReactorTestNGMethod reactorMethod = new ReactorTestNGMethod( frameworkMethod, javaMethod, address );
            MethodInstance newInstance = new MethodInstance(reactorMethod);
            newInstances.add(newInstance);
            methodToAddressMap.put( reactorMethod.getMethodName(), address );
        }
        return newInstances;
    }    
}
