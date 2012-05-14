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

import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_DEFAULT;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_JAVAEE;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_KEY;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_TEST;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.options.WarProbeOption;
import org.ops4j.pax.exam.spi.DefaultExamReactor;
import org.ops4j.pax.exam.spi.DefaultExamSystem;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.PaxExamRuntime;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
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

    private static ReactorManager instance;

    private ExamSystem system;
    private Class<?> testClass;

    private ExamReactor reactor;

    private StagedExamReactor stagedReactor;

    private String systemType;

    private TestProbeBuilder probe;

    private Map<TestAddress, Object> testAddressToMethodMap = new HashMap<TestAddress, Object>();

    private ReactorManager() throws IOException
    {
        system = createExamSystem();
    }

    public static synchronized ReactorManager getInstance() throws IOException
    {
        if( instance == null )
        {
            instance = new ReactorManager();
        }
        return instance;
    }

    public synchronized ExamReactor prepareReactor( Class<?> testClass, Object testClassInstance )
        throws Exception
    {
        this.testClass = testClass;
        this.reactor = getReactor( testClass );

        addConfigurationsToReactor( reactor, testClass, testClassInstance );
        return reactor;
    }

    public StagedExamReactor stageReactor() throws IOException, InstantiationException,
        IllegalAccessException
    {
        stagedReactor = reactor.stage( getFactory( testClass ) );
        return stagedReactor;
    }

    private ExamSystem createExamSystem() throws IOException
    {
        ConfigurationManager cm = new ConfigurationManager();
        systemType = cm.getProperty( EXAM_SYSTEM_KEY, EXAM_SYSTEM_TEST );
        if( EXAM_SYSTEM_DEFAULT.equals( systemType ) )
        {
            system = DefaultExamSystem.create( new Option[0] );
        }
        else if( EXAM_SYSTEM_JAVAEE.equals( systemType ) )
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

    private StagedExamReactorFactory getFactory( Class<?> testClass )
        throws InstantiationException, IllegalAccessException
    {
        ExamReactorStrategy strategy = testClass.getAnnotation( ExamReactorStrategy.class );

        StagedExamReactorFactory fact;
        if( strategy != null )
        {
            fact = strategy.value()[0].newInstance();
        }
        else
        {
            // default:
            fact = new PerMethod();
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
        ExamFactory f = testClass.getAnnotation( ExamFactory.class );

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

    public TestProbeBuilder createProbe( Object testClassInstance ) throws IOException,
        ExamConfigurationException
    {
        if( probe == null )
        {
            probe = system.createProbe();
        }
        probe = overwriteWithUserDefinition( testClass, testClassInstance, probe );
        return probe;
    }

    private TestProbeBuilder overwriteWithUserDefinition( Class<?> testClass, Object instance,
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
    }

    /**
     * @return the systemType
     */
    public String getSystemType()
    {
        return systemType;
    }

    public Object lookupTestMethod( TestAddress address )
    {
        return testAddressToMethodMap.get( address );
    }

    public void storeTestMethod( TestAddress address, Object testMethod )
    {
        testAddressToMethodMap.put( address, testMethod );
    }
}
