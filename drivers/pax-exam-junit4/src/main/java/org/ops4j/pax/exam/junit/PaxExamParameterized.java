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

import java.util.List;

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
import org.ops4j.pax.exam.junit.impl.ExtensibleSuite;
import org.ops4j.pax.exam.junit.impl.ParameterizedDriverExtension;

/**
 * JUnit runner for parameterized Pax Exam tests. See {@link Parameterized} for more details on
 * specifying parameter sets.
 * <p>
 * See {@link PaxExam} for more information on other annotations supported on Pax Exam test classes
 * or methods.
 *
 * @author Harald Wellmann
 *
 */
public class PaxExamParameterized extends ParentRunner<Runner> implements Filterable, Sortable {

    private ExtensibleSuite delegate;
    private ParameterizedDriverExtension extension;

    public PaxExamParameterized(Class<?> klass) throws InitializationError {
        super(klass);
        extension = new ParameterizedDriverExtension(klass);
        delegate = new ExtensibleSuite(klass, extension);
        extension.setBase(delegate);
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

    @Override
    protected List<Runner> getChildren() {
        return extension.getChildren();
    }

    @Override
    protected Description describeChild(Runner child) {
        return extension.describeChild(child);
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        extension.runChild(child, notifier);
    }
}
