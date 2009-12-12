/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.junit.internal.runners.ClassRoadie;
import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.MethodValidator;
import org.junit.internal.runners.TestClass;
import org.junit.internal.runners.TestMethod;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import static org.ops4j.pax.exam.Constants.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import org.ops4j.pax.exam.Info;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.OptionUtils;
import static org.ops4j.pax.exam.junit.JUnitOptions.*;
import org.ops4j.pax.exam.junit.internal.JUnit4MethodRoadie;
import org.ops4j.pax.exam.junit.internal.JUnit4TestMethod;
import org.ops4j.pax.exam.junit.options.JUnitBundlesOption;
import org.ops4j.pax.exam.options.CompositeOption;
import org.ops4j.pax.exam.options.DefaultCompositeOption;
import org.ops4j.pax.exam.options.FrameworkOption;

/**
 * JUnit4 Runner to be used with the {@link org.junit.runner.RunWith} annotation to run with Pax Exam.
 * The class is basically a copy of {@link org.junit.internal.runners.JUnit4ClassRunner} addapted to Pax Exam, keeping
 * as much as possible the original implementation.
 * It was not possible to just extend and override the JUnit4ClassRunner due to internal list fTestMethods that is a
 * list of Methods, and we have to keep extra info about the test methods and Method is a final class.
 *
 * @author Alin Dreghiciu (adreghiciu@gmail.com)
 * @since 0.3.0, December 16, 2008
 */
public class JUnit4TestRunner
    extends Runner
    implements Filterable, Sortable
{

    private final List<JUnit4TestMethod> m_testMethods;
    private final TestClass m_testClass;

    public JUnit4TestRunner( Class<?> klass )
        throws InitializationError
    {
        m_testClass = new TestClass( klass );
        try
        {
            m_testMethods = getTestMethods();
        }
        catch( Exception e )
        {
            throw new InitializationError( e );
        }
        validate();
    }

    protected List<JUnit4TestMethod> getTestMethods()
        throws Exception
    {
        final Collection<JUnit4ConfigMethod> configMethods = getConfigurationMethods();
        final List<JUnit4TestMethod> methods = new ArrayList<JUnit4TestMethod>();
        final Collection<Method> testMethods = m_testClass.getAnnotatedMethods( Test.class );
        for( Method testMethod : testMethods )
        {
            final Option configOptions = getOptions( testMethod, configMethods );
            final FrameworkOption[] frameworkOptions = OptionUtils.filter( FrameworkOption.class, configOptions );
            final Option[] filteredOptions = OptionUtils.remove( FrameworkOption.class, configOptions );
            if( frameworkOptions.length == 0 )
            {
                methods.add( new JUnit4TestMethod( testMethod, m_testClass, null, filteredOptions ) );
            }
            else
            {
                for( FrameworkOption frameworkOption : frameworkOptions )
                {
                    methods.add( new JUnit4TestMethod( testMethod, m_testClass, frameworkOption, filteredOptions ) );
                }
            }
        }
        return methods;
    }

    /**
     * Finds the configuration methods based on the configured {@link ConfigurationStrategy}.
     *
     * @return collection of configuration methods (cannot be null but can be empty)
     *
     * @throws Exception - If test instance cannot be created
     *                   - Re-thrown while finding the configuration methods
     */
    protected Collection<JUnit4ConfigMethod> getConfigurationMethods()
        throws Exception
    {
        final Object testInstance = m_testClass.getJavaClass().newInstance();

        ConfigurationStrategy configStrategy = m_testClass.getJavaClass().getAnnotation( ConfigurationStrategy.class );
        if( configStrategy == null )
        {
            configStrategy = DefaultConfigurationStrategy.class.getAnnotation( ConfigurationStrategy.class );
        }
        final Class<? extends JUnit4ConfigMethods>[] configMethodsClasses = configStrategy.value();
        final List<JUnit4ConfigMethod> configMethods = new ArrayList<JUnit4ConfigMethod>();
        for( final Class<? extends JUnit4ConfigMethods> configMethodsClass : configMethodsClasses )
        {
            final Collection<? extends JUnit4ConfigMethod> methods =
                configMethodsClass.newInstance().getConfigMethods( m_testClass, testInstance );
            if( methods != null )
            {
                configMethods.addAll( methods );
            }
        }
        Configuration profileConfiguration = m_testClass.getJavaClass().getAnnotation( Configuration.class );
        if( profileConfiguration != null )
        {
            for( final Class<? extends CompositeOption> options : profileConfiguration.extend() )
            {
                configMethods.add( new JUnit4ConfigMethod()
                {

                    public boolean matches( Method testMethod )
                    {
                        // match all
                        return true;
                    }

                    public Option[] getOptions()
                        throws Exception
                    {
                        return options.newInstance().getOptions();
                    }
                }
                );
            }
        }
        return configMethods;
    }

    protected void validate()
        throws InitializationError
    {
        MethodValidator methodValidator = new MethodValidator( m_testClass );
        // skip the validation bellow as we may have BundleContext as parameter
        // methodValidator.validateMethodsForDefaultRunner();
        methodValidator.assertValid();
    }

    @Override
    public void run( final RunNotifier notifier )
    {
        new ClassRoadie( notifier, m_testClass, getDescription(), new Runnable()
        {
            public void run()
            {
                runMethods( notifier );
            }
        }
        ).runProtected();
    }

    protected void runMethods( final RunNotifier notifier )
    {
        for( JUnit4TestMethod method : m_testMethods )
        {
            invokeTestMethod( method, notifier );
        }
    }

    @Override
    public Description getDescription()
    {
        Description spec = Description.createSuiteDescription( getName(), classAnnotations() );
        List<JUnit4TestMethod> testMethods = m_testMethods;
        for( JUnit4TestMethod method : testMethods )
        {
            spec.addChild( methodDescription( method ) );
        }
        return spec;
    }

    protected Annotation[] classAnnotations()
    {
        return m_testClass.getJavaClass().getAnnotations();
    }

    protected String getName()
    {
        return getTestClass().getName();
    }

    protected Object createTest()
        throws Exception
    {
        return getTestClass().getConstructor().newInstance();
    }

    protected void invokeTestMethod( JUnit4TestMethod method, RunNotifier notifier )
    {
        Description description = methodDescription( method );
        Object test;
        try
        {
            test = createTest();
        }
        catch( InvocationTargetException e )
        {
            notifier.testAborted( description, e.getCause() );
            return;
        }
        catch( Exception e )
        {
            notifier.testAborted( description, e );
            return;
        }
        new JUnit4MethodRoadie( test, method, notifier, description ).run();
    }

    protected TestMethod wrapMethod( Method method )
    {
        return new TestMethod( method, m_testClass );
    }

    protected String testName( Method method )
    {
        return method.getName();
    }

    protected String testName( JUnit4TestMethod method )
    {
        return method.getName();
    }

    protected Description methodDescription( Method method )
    {
        return Description.createTestDescription( getTestClass().getJavaClass(), testName( method ),
                                                  testAnnotations( method )
        );
    }

    protected Description methodDescription( JUnit4TestMethod method )
    {
        return Description.createTestDescription( getTestClass().getJavaClass(), testName( method ),
                                                  testAnnotations( method.getTestMethod() )
        );
    }

    protected Annotation[] testAnnotations( Method method )
    {
        return method.getAnnotations();
    }

    public void filter( Filter filter )
        throws NoTestsRemainException
    {
        for( Iterator<JUnit4TestMethod> iter = m_testMethods.iterator(); iter.hasNext(); )
        {
            JUnit4TestMethod method = iter.next();
            if( !filter.shouldRun( methodDescription( method.getTestMethod() ) ) )
            {
                iter.remove();
            }
        }
        if( m_testMethods.isEmpty() )
        {
            throw new NoTestsRemainException();
        }
    }

    public void sort( final Sorter sorter )
    {
        Collections.sort( m_testMethods, new Comparator<JUnit4TestMethod>()
        {
            public int compare( JUnit4TestMethod o1, JUnit4TestMethod o2 )
            {
                return sorter.compare( methodDescription( o1 ), methodDescription( o2 ) );
            }
        }
        );
    }

    protected TestClass getTestClass()
    {
        return m_testClass;
    }

    private static Option getOptions( final Method methodName,
                                      final Collection<JUnit4ConfigMethod> configMethods )
        throws Exception
    {
        // always add the junit extender
        final DefaultCompositeOption option = new DefaultCompositeOption(
            mavenBundle()
                .groupId( "org.ops4j.pax.exam" )
                .artifactId( "pax-exam" )
                .version( Info.getPaxExamVersion() )
                .update( Info.isPaxExamSnapshotVersion() )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle()
                .groupId( "org.ops4j.pax.exam" )
                .artifactId( "pax-exam-junit-extender" )
                .version( Info.getPaxExamVersion() )
                .update( Info.isPaxExamSnapshotVersion() )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES ),
            mavenBundle()
                .groupId( "org.ops4j.pax.exam" )
                .artifactId( "pax-exam-junit-extender-impl" )
                .version( Info.getPaxExamVersion() )
                .update( Info.isPaxExamSnapshotVersion() )
                .startLevel( START_LEVEL_SYSTEM_BUNDLES )
        );
        // add options based on available configuration options from the test itself
        for( JUnit4ConfigMethod configMethod : configMethods )
        {
            if( configMethod.matches( methodName ) )
            {
                option.add( configMethod.getOptions() );
            }
        }
        // add junit bundles, if the user did not add junit bundles into configuration
        if( OptionUtils.filter( JUnitBundlesOption.class, option ).length == 0 )
        {
            option.add( junitBundles() );
        }
        return option;
    }

    @ConfigurationStrategy
    private class DefaultConfigurationStrategy
    {

    }

}
