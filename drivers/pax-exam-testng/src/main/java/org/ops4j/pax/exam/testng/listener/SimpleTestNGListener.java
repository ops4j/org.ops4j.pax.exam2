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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
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
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAnnotationTransformer;
import org.testng.IConfigurationListener2;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.IObjectFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.ObjectFactory;
import org.testng.internal.MethodInstance;

// @MetaInfServices(ITestNGListener.class)
public class SimpleTestNGListener implements ISuiteListener, IMethodInterceptor, IHookable //, ITestListener, IConfigurationListener2, IAnnotationTransformer
{
    private static Logger LOG = LoggerFactory.getLogger( SimpleTestNGListener.class );

    private StagedExamReactor reactor;

    private ExamSystem system;
    

    private Map<TestAddress, ITestNGMethod> addressToMethodMap = new HashMap<TestAddress, ITestNGMethod>();
    private Map<String, TestAddress> methodToAddressMap = new HashMap<String, TestAddress>();
    

    public void onStart( ISuite suite )
    {
        LOG.info("before Suite");
    }

    public void onFinish( ISuite suite )
    {
        LOG.info("after Suite");
    }

    public void run( IHookCallBack callBack, ITestResult testResult )
    {
        LOG.info( "running {}", testResult.getName() );
    }

    public List<IMethodInstance> intercept( List<IMethodInstance> methods, ITestContext context )
    {
        LOG.info( "intercept {}", methods );
        return methods;
    }

//    @Override
//    public void onTestStart( ITestResult result )
//    {
//        LOG.info( "onTestStart" );
//    }
//
//    @Override
//    public void onTestSuccess( ITestResult result )
//    {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void onTestFailure( ITestResult result )
//    {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void onTestSkipped( ITestResult result )
//    {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void onTestFailedButWithinSuccessPercentage( ITestResult result )
//    {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void onStart( ITestContext context )
//    {
//        LOG.info( "beforeClass");
//    }
//
//    @Override
//    public void onFinish( ITestContext context )
//    {
//        LOG.info( "afterClass");
//    }
//
//    @Override
//    public void onConfigurationSuccess( ITestResult itr )
//    {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void onConfigurationFailure( ITestResult itr )
//    {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void onConfigurationSkip( ITestResult itr )
//    {
//        // TODO Auto-generated method stub
//        
//    }
//
//    @Override
//    public void beforeConfiguration( ITestResult tr )
//    {
//        LOG.info( "before configuration" );
//    }
//
//    @Override
//    public void transform( ITestAnnotation annotation, Class testClass,
//            Constructor testConstructor, Method testMethod )
//    {
//        LOG.info("transform");
//    }
    
}
