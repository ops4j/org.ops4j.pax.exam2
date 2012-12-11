/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.junit;

import java.lang.reflect.Method;

import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.spi.DefaultExamSystem;
import org.ops4j.pax.exam.spi.PaxExamRuntime;

/**
 * JUnit rule for launching a Pax Exam container in server mode for a JUnit test.
 * <p>
 * Example
 * <pre>
 * public class MyTest {
 * 
 *     &#064;Rule
 *     public PaxExamServer exam = new PaxExamServer();
 * }
 * </pre>
 * 
 * This rule starts a Pax Exam container before each test and stops it after the test. The configuration
 * options for the exam container are taken from a method in the test class annotated with
 * {@link Configuration}. The test class must contain a unique no-args method with this annotation
 * and with return type {@code Option[]}.
 * <p>
 * Alternatively, you can pass a class literal to the {@code PaxExamServer} constructor. In this
 * case, the {@code @Configuration} method will be taken from the class argument.
 * <p>
 * This test rule can be used with any plain old unit test or in combination with other JUnit runners.
 * Do not use this rule in combination with the {@link PaxExam} runner.
 * <p>
 * {@code PaxExamServer} can also be used as a class rule. In this case, test container is started
 * once before running all tests of the class and is stopped when all tests have run.
 *  
 * @author Harald Wellmann
 */
public class PaxExamServer extends ExternalResource
{
    private Class<?> configClass;
    private TestContainer testContainer;

    public PaxExamServer()
    {
    }

    public PaxExamServer( Class<?> configClass )
    {
        this.configClass = configClass;
    }

    @Override
    public Statement apply( Statement base, Description description )
    {
        if( configClass == null )
        {
            configClass = description.getTestClass();
        }
        return super.apply( base, description );
    }

    @Override
    protected void before() throws Throwable
    {
        Option[] options = getConfigurationOptions();
        ExamSystem system = DefaultExamSystem.create( options );
        testContainer = PaxExamRuntime.createContainer( system );
        testContainer.start();
    }

    @Override
    protected void after()
    {
        testContainer.stop();
    }

    private Option[] getConfigurationOptions() throws Exception
    {
        Method m = getConfigurationMethod( configClass );
        Object configClassInstance = configClass.newInstance();
        Option[] options = (Option[]) m.invoke( configClassInstance );
        return options;
    }

    private Method getConfigurationMethod( Class<?> klass )
    {
        Method[] methods = klass.getMethods();
        for( Method m : methods )
        {
            Configuration conf = m.getAnnotation( Configuration.class );
            if( conf != null )
            {
                return m;
            }
        }
        throw new IllegalArgumentException( klass.getName() + " has no @Configuration method" );
    }
}
