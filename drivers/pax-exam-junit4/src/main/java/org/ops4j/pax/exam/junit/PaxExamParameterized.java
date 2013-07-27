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
package org.ops4j.pax.exam.junit;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.Constants;
import org.ops4j.pax.exam.junit.impl.ParameterizedInjectingRunner;
import org.ops4j.pax.exam.junit.impl.ParameterizedProbeRunner;
import org.ops4j.pax.exam.spi.reactors.ReactorManager;

/**
 * JUnit runner for parameterized Pax Exam tests. See {@link Parameterized} for more details
 * on specifying parameter sets.
 * 
 * @author Harald Wellmann
 *
 */
public class PaxExamParameterized extends Runner implements Filterable, Sortable  {
    
    private ParentRunner<?> delegate;
    private Class<?> testClass;
    
    public PaxExamParameterized(Class<?> klass) throws InitializationError {
        this.testClass = klass;
        createDelegate();
    }

    private void createDelegate() throws InitializationError {
        ReactorManager manager = ReactorManager.getInstance();
        if (manager.getSystemType().equals(Constants.EXAM_SYSTEM_CDI)) {
            delegate = new ParameterizedInjectingRunner(testClass);
        }
        else {
            delegate = new ParameterizedProbeRunner(testClass);
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
