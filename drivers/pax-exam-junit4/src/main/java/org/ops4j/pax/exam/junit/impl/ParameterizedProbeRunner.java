/*
 * Copyright 2010 - 2013 Toni Menzel, Harald Wellmann
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
package org.ops4j.pax.exam.junit.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.ops4j.pax.exam.ExamConfigurationException;
import org.ops4j.pax.exam.ExceptionHelper;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDirectory;
import org.ops4j.pax.exam.TestInstantiationInstruction;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit runner for parameterized Pax Exam tests executed via an invoker. This runner is used for
 * all operation modes except CDI. See {@link Parameterized} for more details on specifying
 * parameter sets.
 *
 * @author Harald Wellmann
 */
public class ParameterizedProbeRunner extends BlockJUnit4ClassRunner {

    private static final Logger LOG = LoggerFactory.getLogger(ParameterizedProbeRunner.class);

    /**
     * Reactor manager singleton.
     */
    private final ReactorManager manager;

    private final Map<TestAddress,ParameterizedContext> testAddressToContext = new HashMap<> ();
    private final Map<FrameworkMethod, TestAddress> methodToTestAddressMap = new LinkedHashMap<FrameworkMethod, TestAddress>();
    private String parameterizedName;




    public ParameterizedProbeRunner(Class<?> klass) throws InitializationError {
        super(klass);
        LOG.info("creating PaxExam runner for {}", klass);
        this.manager = ReactorManager.getInstance();

        try {
        	// We use parameterized class instances to initialize
        	// the tests (in this constructor) and to run the tests.
        	int index = 0;
        	for( Object[] params : allParameters()) {

        		// Instantiate the test class and prepare the reactor
    			Object testClassInstance = getTestClass().getOnlyConstructor().newInstance( params );
    			ExamReactor examReactor = this.manager.prepareReactor( klass, testClassInstance );

    			// Now, prepare the tests
    			ParameterizedContext ctx = new ParameterizedContext();
    			ctx.testClassInstance = testClassInstance;
        		addTestsToReactor( examReactor, testClassInstance, params, index, ctx );

        		// Complete the reactor
        		ctx.reactor = this.manager.stageReactor();
        		index ++;
        	}

        } catch( Throwable exc ) {
            throw new InitializationError(exc);
        }
    }

    /**
     * We decorate the super method by reactor setup and teardown.
     * <p>
     * This method is called once per class, which is logical, even with parameterized tests.
     * Note that the given reactor strategy decides whether or not the setup and teardown
     * actually happens at this level.
     * </p>
     */
    @Override
    public void run(RunNotifier notifier) {
        LOG.info("running test class {}", getTestClass().getName());
        Class<?> testClass = getTestClass().getJavaClass();

        // The reactor has the same kind/class.
    	// So, we can pick up any instance. The first one will be fine.
        StagedExamReactor reactor = this.testAddressToContext.values().iterator().next().reactor;
        try {
            this.manager.beforeClass( reactor, testClass);
            super.run(notifier);
        }
        // CHECKSTYLE:SKIP : catch all wanted
        catch (Throwable e) {
            // rethrowing the exception does not help, we have to use the notifier here
            Description description = Description.createSuiteDescription(testClass);
            notifier.fireTestFailure(new Failure(description, e));
        }
        finally {
            this.manager.afterClass( reactor, testClass);
        }
    }

    /**
     * Override to avoid running BeforeClass and AfterClass by the driver. They shall only be run by
     * the container when using a probe invoker.
     */
    @Override
	protected Statement classBlock(final RunNotifier notifier) {
        Statement statement = childrenInvoker(notifier);
        return statement;
    }

    /**
     * Override to avoid running Before, After and Rule methods by the driver. They shall only be
     * run by the container when using a probe invoker.
     */
    @Override
	protected Statement methodBlock( final FrameworkMethod method ) {

        Object test;
        try {
            ReflectiveCallable reflectiveCallable = new ReflectiveCallable() {

                @Override
                // CHECKSTYLE:SKIP : Base class API
                protected Object runReflectiveCall() throws Throwable {
                    return retrieveTestClassInstance( method );
                }
            };
            test = reflectiveCallable.run();
        }
        // CHECKSTYLE:SKIP : ReflectiveCallable API
        catch (Throwable e) {
            return new Fail(e);
        }

        Statement statement = methodInvoker(method, test);
        return statement;
    }

    /**
     * When using a probe invoker, we replace the test methods of this class by a potentially larger
     * set of decorated test methods. Each original test method may give rise to multiple copies per
     * test container or configuration.
     */
    @Override
    protected List<FrameworkMethod> getChildren() {
        if (this.methodToTestAddressMap.isEmpty()) {
            fillChildren();
        }
        return new ArrayList<FrameworkMethod>(this.methodToTestAddressMap.keySet());
    }

    private void fillChildren() {

        Set<StagedExamReactor> reactors = new HashSet<> ();
        for( ParameterizedContext ctx : this.testAddressToContext.values())
        	reactors.add( ctx.reactor );

        List<TestAddress> addresses = new ArrayList<> ();
        for( StagedExamReactor reactor : reactors )
        	addresses.addAll( reactor.getTargets());

        for (TestAddress address : addresses ) {
            FrameworkMethod frameworkMethod = (FrameworkMethod) this.manager.lookupTestMethod( address.root());

            // The reactor may contain targets which do not belong to the current test class
            if (frameworkMethod == null) {
                continue;
            }

            Class<?> frameworkMethodClass = frameworkMethod.getMethod().getDeclaringClass();
            String className = frameworkMethodClass.getName();
            String methodName = frameworkMethod.getName();

            if (frameworkMethodClass.isAssignableFrom(getTestClass().getJavaClass())) {
                FrameworkMethod method = new ParameterizedFrameworkMethod(address, frameworkMethod);
                TestDirectory.getInstance().add(address, new TestInstantiationInstruction(className + ";" + methodName));

                this.methodToTestAddressMap.put(method, address);
            }
        }
    }

    /**
     * Adds test methods to the reactor, mapping method names to test addresses which are used by
     * the probe invoker.
     * <p>
     * Note that when a collection of test classes is passed to an external JUnit runner like
     * Eclipse or Maven Surefire, this method is invoked (via the constructor of this runner) for
     * each class <em>before</em> the {@link #run(RunNotifier)} method is invoked for any class.
     * <p>
     * This way, we can register all test methods in the reactor before the actual test execution
     * starts.
     *
     * @param reactor
     * @param testClass
     * @param testClassInstance
     * @param params
     * @param index
     * @param ctx
     * @throws IOException
     * @throws ExamConfigurationException
     */
    private void addTestsToReactor( ExamReactor reactor, Object testClassInstance, Object[] params, int index, ParameterizedContext ctx  )
    throws IOException, ExamConfigurationException {

    	TestProbeBuilder probe = this.manager.createProbeBuilder(testClassInstance);
    	for (FrameworkMethod s : super.getChildren()) {

    		// record the method -> address matching
    		TestAddress address;
    		if( this.parameterizedName != null ) {
    			String name = this.parameterizedName.
    					replace( "{index}", String.valueOf( index )).
    					replace( "{method}", s.getMethod().getName());

    			name = MessageFormat.format( name, params );
    			address = probe.addTest( testClassInstance.getClass(), s.getMethod().getName(), index, name );

    		} else {
    			address = probe.addTest( testClassInstance.getClass(), s.getMethod().getName(), index );
    		}

    		this.testAddressToContext.put( address, ctx );
    		this.manager.storeTestMethod(address, s);
    	}

        reactor.addProbe(probe);
    }

    /**
     * When using a probe invoker, we replace the super method and invoke the test method indirectly
     * via the reactor.
     */
    @Override
	protected synchronized Statement methodInvoker(final FrameworkMethod method, final Object test) {

        return new Statement() {

            @Override
            // CHECKSTYLE:SKIP : Statement API
            public void evaluate() throws Throwable {
                TestAddress address = ParameterizedProbeRunner.this.methodToTestAddressMap.get(method);
                TestAddress root = address.root();

                LOG.debug("Invoke " + method.getName() + " @ " + address + " Arguments: " + Arrays.toString( root.arguments()));
                try {
                	// Find the right reactor
                	ParameterizedContext ctx = ParameterizedProbeRunner.this.testAddressToContext.get( address.root());
                	ctx.reactor.invoke(address);
                }
                // CHECKSTYLE:SKIP : StagedExamReactor API
                catch (Exception e) {
                    Throwable t = ExceptionHelper.unwind(e);
                    throw t;
                }
            }
        };
    }

//    /**
//     * Creates an instance of the current test class. When using a probe invoker, this simply
//     * delegates to super. Otherwise, we perform injection on the instance created by the super
//     * method.
//     * <p>
//     * In this case, an {@link InjectorFactory} is obtained via SPI lookup.
//     */
    protected Object retrieveTestClassInstance( FrameworkMethod method ) throws Exception {

    	TestAddress address = ParameterizedProbeRunner.this.methodToTestAddressMap.get(method);
    	ParameterizedContext ctx = this.testAddressToContext.get( address.root());
        return ctx.testClassInstance;
    }


    @SuppressWarnings("unchecked")
    // CHECKSTYLE:SKIP - JUnit API
    private Iterable<Object[]> allParameters() throws Throwable {

    	Object params = getParametersMethod().invokeExplosively(null);
    	if (params instanceof Iterable) {
    		this.parameterizedName = getParametersMethod().getAnnotation( Parameters.class ).name();
    		return (Iterable<Object[]>) params;
    	}
    	else {
    		throw parametersMethodReturnedWrongType();
    	}
    }

    private FrameworkMethod getParametersMethod() throws Exception {
        List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(Parameters.class);
        for (FrameworkMethod each : methods) {
            if (each.isStatic() && each.isPublic()) {
                return each;
            }
        }

        throw new Exception("No public static parameters method on class "
            + getTestClass().getName());
    }

    private Exception parametersMethodReturnedWrongType() throws Exception {
        String className = getTestClass().getName();
        String methodName = getParametersMethod().getName();
        String message = MessageFormat.format("{0}.{1}() must return an Iterable of arrays.",
            className, methodName);
        return new TestContainerException(message);
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
        validateOnlyOneConstructor(errors);
        if (fieldsAreAnnotated()) {
            validateZeroArgConstructor(errors);
        }
    }

    private List<FrameworkField> getAnnotatedFieldsByParameter() {
        return getTestClass().getAnnotatedFields(Parameter.class);
    }

    private boolean fieldsAreAnnotated() {
        return !getAnnotatedFieldsByParameter().isEmpty();
    }


    /**
     * A bean that allow various bindings.
     * <p>
     * Parameterized tests hardly fit into the Exam code organization.
     * So that parameterized tests rely on the most minimalist assumptions,
     * we must remember for every test which reactor must be used.
     * </p>
     * <p>
     * Most of the time, this will be useless, except for tests that use different
     * configurations and use the PerMethod reactor strategy.
     * </p>
     *
     * @author Vincent Zurczak - Linagora
     */
    private static class ParameterizedContext {

    	public StagedExamReactor reactor;
    	public Object testClassInstance;
    }
}
