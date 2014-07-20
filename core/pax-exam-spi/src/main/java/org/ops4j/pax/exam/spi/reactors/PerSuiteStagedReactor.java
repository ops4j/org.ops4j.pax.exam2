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
package org.ops4j.pax.exam.spi.reactors;

import java.util.List;
import java.util.Set;

import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.StagedExamReactor;

/**
 * A reactor implementation which keeps the same container(s) running for the entire test suite.
 * <p>
 * Since the test drivers create a new reactor per test class, this implementation delegates to a
 * {@link SingletonStagedReactor} which remembers its state and does not restart the test
 * containers.
 * 
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class PerSuiteStagedReactor implements StagedExamReactor {

    private StagedExamReactor delegate;

    public PerSuiteStagedReactor(List<TestContainer> containers, List<TestProbeBuilder> mProbes) {
        this.delegate = SingletonStagedReactor.getInstance(containers, mProbes);
    }

    public void invoke(TestAddress address) throws Exception {
        delegate.invoke(address);
    }

    public Set<TestAddress> getTargets() {
        return delegate.getTargets();
    }

    @Override
    public void afterSuite() {
        delegate.afterSuite();
    }

    public void afterTest() {
    }

    public void beforeTest() {
    }

    public void afterClass() {
    }

    public void beforeClass() {
    }

    public void beforeSuite() {
        delegate.beforeSuite();
    }

    @Override
    public boolean awaitsBeforeSuite() {
       return delegate.awaitsBeforeSuite();
    }
}
