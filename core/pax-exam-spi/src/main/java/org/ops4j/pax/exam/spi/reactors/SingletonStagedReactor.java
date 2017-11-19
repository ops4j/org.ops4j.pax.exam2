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

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton reactor which starts the container and installs the probes just once. The teardown
 * logic is postponed to a JVM shutdown hook.
 *
 * @author Harald Wellmann
 * @since 3.0.0
 */
public class SingletonStagedReactor implements StagedExamReactor {

    private static final Logger LOG = LoggerFactory.getLogger(SingletonStagedReactor.class);

    private static SingletonStagedReactor instance;

    private List<TestContainer> testContainers;
    private TestProbeBuilder probeBuilder;

    private SingletonStagedReactor(List<TestContainer> containers, TestProbeBuilder mProbes) {
        testContainers = containers;
        probeBuilder = mProbes;
    }

    /**
     * @param containers
     *            to be used
     * @param probeBuilder
     *            to be installed on all probes
     * @return staged reactor
     */
    public static synchronized StagedExamReactor getInstance(List<TestContainer> containers,
        TestProbeBuilder probeBuilder) {
        if (instance == null) {
            instance = new SingletonStagedReactor(containers, probeBuilder);
        }
        else if (!Objects.equals(instance.probeBuilder, probeBuilder)) {
            throw new TestContainerException(
                "Using the PerSuite reactor strategy, all test classes must share the same probe");
        }
        return instance;
    }

    public void tearDown() {
    }

    @Override
    public void beforeSuite() {
        for (TestContainer container : testContainers) {
            container.start();

            if (probeBuilder != null) {
                LOG.debug("installing probe {}", probeBuilder);

                try {
                    container.installProbe(probeBuilder.build().getStream());
                }
                catch (IOException e) {
                    throw new TestContainerException("Unable to build the probe.", e);
                }
            }
        }
    }

    @Override
    public void afterSuite() {
        for (TestContainer container : testContainers) {
            container.stop();
        }
    }

    @Override
    public void beforeClass() {

    }

    @Override
    public void afterClass() {
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) throws Exception {
        assert (description != null) : "TestAddress must not be null.";

        TestContainer testContainer = testContainers.get(0);
        testContainer.runTest(description, listener);
    }
}
