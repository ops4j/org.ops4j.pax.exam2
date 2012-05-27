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

import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_CDI;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_DEFAULT;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_JAVAEE;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_KEY;
import static org.ops4j.pax.exam.Constants.EXAM_SYSTEM_TEST;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.exam.ConfigurationManager;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.ProbeInvoker;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
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
import org.ops4j.pax.exam.spi.reactors.PerSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the exam system and reactor required by a test driver. This class is a singleton and
 * keeps track of all tests in the current test suite and lets a reactor reuse the Exam system and
 * the test probe, where applicable.
 * 
 * <p>
 * This class was factored out from the JUnit4TestRunner and does not depend on JUnit.
 * <p>
 * TODO move it to another module so it can be used by the TestNG driver.
 * <p>
 * TODO check if there are any concurrency issues. Some methods are synchronized, which is just
 * inherited from the 2.1.0 implementation. The use cases are not quite clear.
 * 
 * @author Harald Wellmann
 */
public class ReactorManager
{
    private static Logger LOG = LoggerFactory.getLogger( ReactorManager.class );

    /** Singleton instance of this manager. */
    private static ReactorManager instance;

    /** Exam system, containing system and user configuration options. */
    private ExamSystem system;

    /** The system type, which determines the kind of probe to be used. */
    private String systemType;

    /** The current test class. */
    private Class<?> testClass;

    /** The reactor. */
    private ExamReactor reactor;

        
    /**
     * A probe builder for the current test probe. A probe builder contains a number of test classes
     * and their dependent classes and a list of test methods to be executed.
     * <p>
     * Test methods are added incrementally as classes are scanned. By default, the same probe
     * builder is reused for all test classes, unless a given class overrides the default probe
     * configuration.
     */
    private TestProbeBuilder defaultProbeBuilder;

    /**
     * Maps test addresses to driver-dependent test method wrappers. A test address is a unique
     * identifier for a test method in a given container which is used by a {@link ProbeInvoker} for
     * indirectly invoking the test method in the container.
     * <p>
     * This map is not used when tests are executed directly, i.e. without invoker.
     */
    private Map<TestAddress, Object> testAddressToMethodMap = new HashMap<TestAddress, Object>();

    
    private Set<Class<?>> testClasses = new HashSet<Class<?>>();
    
    private boolean suiteStarted;
    
    /**
     * Private constructor for singleton.
     */
    private ReactorManager()
    {
        try
        {
            system = createExamSystem();
        }
        catch ( IOException exc )
        {
            throw new TestContainerException( "cannot create Exam system", exc );
        }
    }

    /**
     * Returns the singleton ReactorManager instance.
     */
    public static synchronized ReactorManager getInstance()
    {
        if( instance == null )
        {
            instance = new ReactorManager();
        }
        return instance;
    }

    /**
     * Prepares the unstaged reactor for the given test class instance. Any configurations from
     * {@code Configuration} methods of the class are added to the reactor.
     * 
     * @param testClass
     * @param testClassInstance
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     */
    public synchronized ExamReactor prepareReactor( Class<?> testClass, Object testClassInstance )
        throws InstantiationException, IllegalAccessException, IllegalArgumentException,
        InvocationTargetException, IOException
    {
        this.testClass = testClass;
        this.reactor = createReactor( testClass );
        testClasses.add( testClass );
        addConfigurationsToReactor( testClass, testClassInstance );
        return reactor;
    }

    /**
     * Stages the reactor for the current class.
     * @return staged reactor
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public StagedExamReactor stageReactor() throws IOException, InstantiationException,
        IllegalAccessException
    {
        StagedExamReactor stagedReactor = reactor.stage( getStagingFactory( testClass ) );
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

    /**
     * Scans the current test class for declared or inherited {@code @Configuration} methods
     * and invokes them, adding the returned configuration to the reactor.
     * 
     * @param testClass
     * @param testClassInstance
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws IllegalArgumentException
     * @throws IOException
     */
    private void addConfigurationsToReactor( Class<?> testClass, Object testClassInstance )
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

    /**
     * Creates a staging factory indicated by the {@link ExamReactorStrategy} annotation of
     * the test class.
     * @param testClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private StagedExamReactorFactory getStagingFactory( Class<?> testClass )
        throws InstantiationException, IllegalAccessException
    {
        ExamReactorStrategy strategy = testClass.getAnnotation( ExamReactorStrategy.class );

        StagedExamReactorFactory fact;
        if( strategy != null )
        {
            fact = strategy.value()[0].newInstance();
        }
        else if (systemType.equals( EXAM_SYSTEM_CDI ) || systemType.equals( EXAM_SYSTEM_JAVAEE ))
        {
            fact = new PerSuite();
        }
        else
        {
            // OSGi default from Pax Exam 2.x
            fact = new PerMethod();
        }
        return fact;
    }

    /**
     * Creates an unstaged reactor for the given test class.
     * @param testClass
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private DefaultExamReactor createReactor( Class<?> testClass )
        throws InstantiationException, IllegalAccessException
    {
        return new DefaultExamReactor( system, createsTestContainerFactory( testClass ) );
    }

    /**
     * Creates the test container factory to be used by the reactor.
     * <p>
     * TODO Do we really need this? 
     * 
     * @param testClass
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private TestContainerFactory createsTestContainerFactory( Class<?> testClass )
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

    /**
     * Lazily creates a probe builder. The same probe builder will be reused for all test classes,
     * unless the default builder is overridden in a given class. 
     * @param testClassInstance
     * @return
     * @throws IOException
     * @throws ExamConfigurationException
     */
    public TestProbeBuilder createProbeBuilder( Object testClassInstance ) throws IOException,
        ExamConfigurationException
    {
        if( defaultProbeBuilder == null )
        {
            defaultProbeBuilder = system.createProbe();
        }
        TestProbeBuilder probeBuilder = overwriteWithUserDefinition( testClass, testClassInstance );
        return probeBuilder;
    }

    private TestProbeBuilder overwriteWithUserDefinition( Class<?> testClass, Object instance )
        throws ExamConfigurationException
    {
        Method[] methods = testClass.getMethods();
        for( Method m : methods )
        {
            ProbeBuilder conf = m.getAnnotation( ProbeBuilder.class );
            if( conf != null )
            {
                LOG.debug( "User defined probe hook found: " + m.getName() );
                TestProbeBuilder probeBuilder;
                try
                {
                    probeBuilder = (TestProbeBuilder) m.invoke( instance, defaultProbeBuilder );
                }
                catch ( Exception e )
                {
                    throw new ExamConfigurationException( "Invoking custom probe hook "
                            + m.getName() + " failed", e );
                }
                if( probeBuilder != null )
                {
                    return probeBuilder;
                }
                else
                {
                    throw new ExamConfigurationException( "Invoking custom probe hook "
                            + m.getName() + " succeeded but returned null" );
                }
            }
        }
        LOG.debug( "No User defined probe hook found" );
        return defaultProbeBuilder;
    }

    /**
     * @return the systemType
     */
    public String getSystemType()
    {
        return systemType;
    }

    /**
     * Looks up a test method for a given address.
     * @param address test method address used by probe
     * @return test method wrapper - the type is only known to the test driver.
     */
    public Object lookupTestMethod( TestAddress address )
    {
        return testAddressToMethodMap.get( address );
    }

    /**
     * Stores the test method wrapper for a given test address
     * @param address test method address used by probe
     * @param testMethod test method wrapper - the type is only known to the test driver
     */
    public void storeTestMethod( TestAddress address, Object testMethod )
    {
        testAddressToMethodMap.put( address, testMethod );
    }

    public void afterClass( StagedExamReactor stagedReactor, Class<?> klass )
    {
        stagedReactor.afterClass();
        testClasses.remove( klass );
        if (testClasses.isEmpty())
        {
            LOG.info( "suite finished" );
            stagedReactor.afterSuite();
        }
    }

    public void beforeClass(  StagedExamReactor stagedReactor, Class<?> klass )
    {
        if (! suiteStarted)
        {
            suiteStarted = true;
            stagedReactor.beforeSuite();
        }
        stagedReactor.beforeClass();
    }
}
