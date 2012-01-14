/*
 * Copyright 2012 Harald Wellmann.
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.options.WarProbeOption;
import org.ops4j.pax.exam.spi.DefaultExamReactor;
import org.ops4j.pax.exam.spi.DefaultExamSystem;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.PaxExamRuntime;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the exam system and reactor required by a driver.
 * <p>
 * This class was factored out from the JUnit4TestRunner and does not depend on JUnit.
 * 
 * @author Harald Wellmann
 */
public class ReactorManager
{
    private static Logger LOG = LoggerFactory.getLogger( ReactorManager.class );

    private ExamSystem system;
    private Class<?> testClass;

    private ExamReactor reactor;

    private StagedExamReactor stagedReactor;

    public synchronized ExamReactor prepareReactor( Class<?> testClass, Object testClassInstance )
        throws Exception
    {
        this.system = createExamSystem();
        this.testClass = testClass;
        this.reactor = getReactor( testClass );

        addConfigurationsToReactor( reactor, testClass, testClassInstance );
        return reactor;
    }
    
    public StagedExamReactor stageReactor( ) throws IOException, InstantiationException, IllegalAccessException
    {
        stagedReactor = reactor.stage( getFactory( testClass ) );
        return stagedReactor;        
    }

    public ExamSystem createExamSystem() throws IOException
    {
        ConfigurationManager cm = new ConfigurationManager();
        String systemType = cm.getProperty( Constants.EXAM_SYSTEM_KEY );
        if( Constants.EXAM_SYSTEM_DEFAULT.equals( systemType ) )
        {
            system = DefaultExamSystem.create( new Option[0] );
        }
        else if( Constants.EXAM_SYSTEM_JAVAEE.equals( systemType ) )
        {
            system = DefaultExamSystem.create( new Option[]{ new WarProbeOption() } );
        }
        else
        {
            system = PaxExamRuntime.createTestSystem();
        }
        return system;
    }

    public void addConfigurationsToReactor( ExamReactor reactor, Class<?> testClass,
            Object testClassInstance )
        throws IllegalAccessException, InvocationTargetException, IllegalArgumentException,
        IOException
    {
        Method[] methods = testClass.getMethods();
        for( Method m : methods )
        {
            Configuration conf = m.getAnnotation( Configuration.class );
            if( conf != null )
            {
                // consider as option, so prepare that one:
                reactor.addConfiguration( ( (Option[]) m.invoke( testClassInstance ) ) );
            }
        }
    }

    public StagedExamReactorFactory getFactory( Class<?> testClass )
        throws InstantiationException, IllegalAccessException
    {
        ExamReactorStrategy strategy =
            (ExamReactorStrategy) testClass.getAnnotation( ExamReactorStrategy.class );

        StagedExamReactorFactory fact;
        if( strategy != null )
        {
            fact = strategy.value()[0].newInstance();
        }
        else
        {
            // default:
            fact = new AllConfinedStagedReactorFactory();
        }
        return fact;
    }

    public DefaultExamReactor getReactor( Class<?> testClass )
        throws InstantiationException, IllegalAccessException
    {
        return new DefaultExamReactor( system, getExamFactory( testClass ) );
    }

    public TestContainerFactory getExamFactory( Class<?> testClass )
        throws IllegalAccessException, InstantiationException
    {
        ExamFactory f = (ExamFactory) testClass.getAnnotation( ExamFactory.class );

        TestContainerFactory fact;
        if( f != null )
        {
            fact = f.value().newInstance();
        }
        else
        {
            // default:
            fact = PaxExamRuntime.getTestContainerFactory();
        }
        return fact;
    }

    public TestProbeBuilder createProbe(Object testClassInstance) throws IOException, ExamConfigurationException
    {
        TestProbeBuilder probe = system.createProbe(  );
        probe = overwriteWithUserDefinition( testClass, testClassInstance, probe );
        return probe;
    }
    

    public TestProbeBuilder overwriteWithUserDefinition( Class<?> testClass, Object instance,
            TestProbeBuilder probe )
        throws ExamConfigurationException
    {
        Method[] methods = testClass.getMethods();
        for( Method m : methods )
        {
            ProbeBuilder conf = m.getAnnotation( ProbeBuilder.class );
            if( conf != null )
            {
                // consider as option, so prepare that one:
                LOG.debug( "User defined probe hook found: " + m.getName() );
                TestProbeBuilder probeBuilder;
                try
                {
                    probeBuilder = (TestProbeBuilder) m.invoke( instance, probe );
                }
                catch ( Exception e )
                {
                    throw new ExamConfigurationException( "Invoking custom probe hook "
                            + m.getName() + " failed", e );
                }
                if( probeBuilder != null )
                {
                    return probe;
                }
                else
                {
                    throw new ExamConfigurationException( "Invoking custom probe hook "
                            + m.getName() + " succeeded but returned null" );
                }
            }
        }
        LOG.debug( "No User defined probe hook found" );
        return probe;
    }

    public void shutdown()
    {
        stagedReactor.tearDown();
        system.clear();
    }
}
