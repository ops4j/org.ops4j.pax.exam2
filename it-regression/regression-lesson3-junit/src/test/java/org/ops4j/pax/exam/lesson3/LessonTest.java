/*
 * Copyright (C) 2011 Toni Menzel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.exam.lesson3;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;
import static org.junit.Assert.*;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.LibraryOptions.*;

/**
 * This is what's probably most known to Pax Exam 1.x users.
 * You can recognize the "Junit Driver" approach by the @RunWith() annotation at class level.
 *
 * This overloads JUnit4s default runner so that Pax Exam is in full control of:
 * - the test roaster
 * - the test invokation
 *
 * So whats the test roaster ?
 * You know methods annotated with @Test annotations from JUnit4 API, right ?
 * This is the standard roaster. Those will appear in your Test Runner when launching this class in your IDE using "Run with JUnit..".
 *
 * But: You learned (in Lesson 1) that with Pax Exam you might have your tests executed more than once in different Test Container instances.
 * (Remember the TestContainerFactory.parse() returning a list of TestContainers ?)
 *
 * So wouldn't it be nice to have that reflected in your JUnit Roaster ?
 * Thats what the @RunWith(JUnit4TestRunner.class) does. Pax Exam re-aranges the JUnit4 Roaster and gives you a single entry for each physical test.
 * In this lesson we are using the NativeTestContainer implementation (see the pom.xml), and additional put two OSGi Frameworks to it: Felix and Equinox.
 * You will see each of the @Test methods below twice. Once for each framework.
 *
 * The @Configuration is a desclarative way of what you did manually in the previous lessons. Now you only return Option[] in any @Configuration-annotated method
 * and you are set.
 * A probe will be generated underneath with every @Test put into it.
 *
 * Important:
 * It might be subtle at first, but it is very important to understand that this test class is also the class that will end up in your probe.
 * Just because you use it to initially kick of the tests (see, you press "Run with JUnit" on this class) it does not mean the tests will run in the same instance of this class.
 * Underneath, the @Tests are invoked on a fresh instance of this class insight the OSGi Container (which might be a totally different JVM).
 * So @Tests should be rather side-effect and stateless and aware of package visibility inside the OSGi container.
 */

@RunWith( JUnit4TestRunner.class )

/**
 * NEW & Optional:
 * You can annotate your class with the @ExamReactorStrategy to overwrite the default strategy:
 * @ExamReactorStrategy( AllConfinedStagedReactorFactory.class )
 * This is the default setting.
 * It resembles the way Exam 1.x worked: a new TestContainer instance for every test in your probe(s).
 * Depending on the TestContainerFactory you use (pom.xml!) this may be slower than every other strategy.
 * But its probably also the most side-effect free solution.
 * Lets do the math how many test containers are launched (one after another):
 * 2 tests x 2 test containers in pom = 4 launches
 *
 *
 * or
 *
 * @ExamReactorStrategy( EagerSingleStagedReactorFactory.class )
 * This is the other extreme to AllConfinedStagedReactorFactory. It uses one TestContainer (for all of your tests).
 * Important: You will still get of cause two test container instances for every physical container (like Felix + Equinox).
 * Its just that the Felix container will be started once, all your tests are running against it, then it will shutdown.
 * So in this specific example, you will have two test container launches (one for Felix and another for Equinox.
 * This does not change when adding more Tests to this TestCase.
 *
 *
 */
@ExamReactorStrategy( AllConfinedStagedReactorFactory.class )
public class LessonTest {

    @Configuration()
    public Option[] config()
    {
        return options(
            junitBundles()
        );
    }

    /**
     * Just like any other Test in previous lessons, they can receive an instance of BundleContext plus optional arguments.
     * Because you have Test Setup (@Configuration method) and Tests (this method) side by side, there is no point passing additional arguments.
     *
     * @param ctx BundleContext injected. Must be first argument, if any.
     */
    @Test
    public void withBC( BundleContext ctx )
    {
        assertThat( ctx, is( notNullValue() ) );
        System.out.println( "BundleContext of bundle injected: " + ctx.getBundle().getSymbolicName() );

    }

    @Test
    public void without()
    {
        System.out.println( "------- HERE!" );
    }
}
