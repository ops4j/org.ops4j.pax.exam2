/*
 * Copyright 2013 Harald Wellmann
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



import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;

/**
 * <p>
 * The custom runner <code>Parameterized</code> implements parameterized tests.
 * When running a parameterized test class, instances are created for the
 * cross-product of the test methods and the test data elements.
 * </p>
 *
 * For example, to test a Fibonacci function, write:
 *
 * <pre>
 * &#064;RunWith(Parameterized.class)
 * public class FibonacciTest {
 *      &#064;Parameters(name= &quot;{index}: fib({0})={1}&quot;)
 *      public static Iterable&lt;Object[]&gt; data() {
 *              return Arrays.asList(new Object[][] { { 0, 0 }, { 1, 1 }, { 2, 1 },
 *                 { 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 } });
 *     }
 *
 *      private int fInput;
 *
 *      private int fExpected;
 *
 *      public FibonacciTest(int input, int expected) {
 *              fInput= input;
 *              fExpected= expected;
 *     }
 *
 *      &#064;Test
 *      public void test() {
 *              assertEquals(fExpected, Fibonacci.compute(fInput));
 *     }
 * }
 * </pre>
 *
 * <p>
 * Each instance of <code>FibonacciTest</code> will be constructed using the
 * two-argument constructor and the data values in the
 * <code>&#064;Parameters</code> method.
 *
 * <p>
 * In order that you can easily identify the individual tests, you may provide a
 * name for the <code>&#064;Parameters</code> annotation. This name is allowed
 * to contain placeholders, which are replaced at runtime. The placeholders are
 * <dl>
 * <dt>{index}</dt>
 * <dd>the current parameter index</dd>
 * <dt>{0}</dt>
 * <dd>the first parameter value</dd>
 * <dt>{1}</dt>
 * <dd>the second parameter value</dd>
 * <dt>...</dt>
 * <dd></dd>
 * </dl>
 * In the example given above, the <code>Parameterized</code> runner creates
 * names like <code>[1: fib(3)=2]</code>. If you don't use the name parameter,
 * then the current parameter index is used as name.
 * </p>
 *
 * You can also write:
 *
 * <pre>
 * &#064;RunWith(Parameterized.class)
 * public class FibonacciTest {
 *  &#064;Parameters
 *  public static Iterable&lt;Object[]&gt; data() {
 *      return Arrays.asList(new Object[][] { { 0, 0 }, { 1, 1 }, { 2, 1 },
 *                 { 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 } });
 *     }
 *  &#064;Parameter(0)
 *  public int fInput;
 *
 *  &#064;Parameter(1)
 *  public int fExpected;
 *
 *  &#064;Test
 *  public void test() {
 *      assertEquals(fExpected, Fibonacci.compute(fInput));
 *     }
 * }
 * </pre>
 *
 * <p>
 * Each instance of <code>FibonacciTest</code> will be constructed with the default constructor
 * and fields annotated by <code>&#064;Parameter</code>  will be initialized
 * with the data values in the <code>&#064;Parameters</code> method.
 * </p>
 *
 * @since 4.0
 */
public class ParameterizedInjectingRunner extends Suite {

    private static final List<Runner> NO_RUNNERS = Collections
            .<Runner>emptyList();

    private final ArrayList<Runner> runners = new ArrayList<Runner>();

    private ReactorManager manager;

    private StagedExamReactor stagedReactor;

    /**
     * Only called reflectively. Do not use programmatically.
     */
    public ParameterizedInjectingRunner(Class<?> klass) throws Throwable {
        super(klass, NO_RUNNERS);
        
        manager = ReactorManager.getInstance();
        manager.setAnnotationHandler(new JUnitLegacyAnnotationHandler());
        manager.prepareReactor(klass, null);
        stagedReactor = manager.stageReactor();
        
        Parameters parameters = getParametersMethod().getAnnotation(
                Parameters.class);
        createRunnersForParameters(allParameters(), parameters.name());
    }

    @Override
    protected List<Runner> getChildren() {
        return runners;
    }
    
    @Override
    public void run(RunNotifier notifier) {
        //LOG.info("running test class {}", getTestClass().getName());
        Class<?> testClass = getTestClass().getJavaClass();
        try {
            manager.beforeClass(stagedReactor, testClass);
            super.run(notifier);
        }
        // CHECKSTYLE:SKIP : catch all wanted
        catch (Throwable e) {
            // rethrowing the exception does not help, we have to use the notifier here
            Description description = Description.createSuiteDescription(testClass);
            notifier.fireTestFailure(new Failure(description, e));
        }
        finally {
            manager.afterClass(stagedReactor, testClass);
        }
    }

    @SuppressWarnings("unchecked")
    private Iterable<Object[]> allParameters() throws Throwable {
        Object parameters = getParametersMethod().invokeExplosively(null);
        if (parameters instanceof Iterable) {
            return (Iterable<Object[]>) parameters;
        } else {
            throw parametersMethodReturnedWrongType();
        }
    }

    private FrameworkMethod getParametersMethod() throws Exception {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(
                Parameters.class);
        for (FrameworkMethod each : methods) {
            if (each.isStatic() && each.isPublic()) {
                return each;
            }
        }

        throw new Exception("No public static parameters method on class "
                + getTestClass().getName());
    }

    private void createRunnersForParameters(Iterable<Object[]> allParameters,
            String namePattern) throws InitializationError, Exception {
        try {
            int i = 0;
            for (Object[] parametersOfSingleTest : allParameters) {
                String name = nameFor(namePattern, i, parametersOfSingleTest);
                TestClassRunnerForParameters runner = new TestClassRunnerForParameters(
                        getTestClass().getJavaClass(), parametersOfSingleTest,
                        name);
                runners.add(runner);
                ++i;
            }
        } catch (ClassCastException e) {
            throw parametersMethodReturnedWrongType();
        }
    }

    private String nameFor(String namePattern, int index, Object[] parameters) {
        String finalPattern = namePattern.replaceAll("\\{index\\}",
                Integer.toString(index));
        String name = MessageFormat.format(finalPattern, parameters);
        return "[" + name + "]";
    }

    private Exception parametersMethodReturnedWrongType() throws Exception {
        String className = getTestClass().getName();
        String methodName = getParametersMethod().getName();
        String message = MessageFormat.format(
                "{0}.{1}() must return an Iterable of arrays.",
                className, methodName);
        return new Exception(message);
    }
}