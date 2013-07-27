/*
 * Copyright 2010 - 2012 Toni Menzel, Harald Wellmann
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

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.junit.impl.InjectingRunner;
import org.ops4j.pax.exam.junit.impl.ProbeRunner;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;

/**
 * Default JUnit runner for Pax Exam. To use this runner, annotate your test class with
 * {@code @RunWith(PaxExam.class)}.
 * <p>
 * The optional class-level annotation {@code @ExamReactorStrategy} defines the restart behaviour of
 * the test reactor which defaults to {@code PerMethod} in OSGi mode and to {@code PerSuite}
 * otherwise.
 * <p>
 * The test class may contain one or more methods annotated by {@code @Configuration}, returning a
 * list of options for configuring the test container.
 * <p>
 * If there is more than one configuration method, each test method is run for each configuration.
 * <p>
 * The JUnit annotations {@code @Rule, @Before, @After} work as expected, the corresponding actions
 * are executed within the Pax Exam test container.
 * <p>
 * The JUnit annotations {@code @BeforeClass, @AfterClass} are of limited use only: The
 * corresponding actions will be executed in the driver, but not in the Pax Exam test container.)
 * <p>
 * The {@code javax.inject.Inject} annotation can be used on fields to inject dependencies into the
 * test class. In Java EE and CDI modes, injection is performed by the CDI bean manager. In web
 * mode, injection is performed by CDI or by Spring, depending on the configured injector.
 * <p>
 * In OSGi mode, Pax Exam injects OSGi services, obtained from the service registry with the default
 * Pax Exam system timeout. The optional {@code @Filter} annotation can be used on an injection
 * point to define an LDAP filter or to customize the timeout.
 * <p>
 * For parameterized tests, use {@link PaxExamParameterized} instead of this runner.
 * 
 * @author Harald Wellmann
 */
public class PaxExam extends Runner implements Filterable, Sortable {

    private ParentRunner<?> delegate;
    private Class<?> testClass;

    public PaxExam(Class<?> klass) throws InitializationError {
        this.testClass = klass;
        createDelegate();
    }

    private void createDelegate() throws InitializationError {
        ReactorManager manager = ReactorManager.getInstance();
        if (manager.getSystemType().equals(Constants.EXAM_SYSTEM_CDI)) {
            delegate = new InjectingRunner(testClass);
        }
        else {
            delegate = new ProbeRunner(testClass);
        }
    }

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        delegate.run(notifier);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        delegate.filter(filter);
    }

    @Override
    public void sort(Sorter sorter) {
        delegate.sort(sorter);
    }
}
