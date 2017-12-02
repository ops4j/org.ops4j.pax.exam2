/*
 * Copyright 2010 Toni Menzel.
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

import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One target only reactor implementation (simpliest and fastest)
 *
 * @author tonit
 */
public class PerClassStagedReactor implements StagedExamReactor {

    private static final Logger LOG = LoggerFactory.getLogger(PerClassStagedReactor.class);

    private final TestContainer testContainer;
    private final TestProbeBuilder probeBuilder;

    /**
     * @param containers
     *            to be used
     * @param mProbes
     *            to be installed on all probes
     */
    public PerClassStagedReactor(TestContainer container, TestProbeBuilder mProbes) {
        this.testContainer = container;
        this.probeBuilder = mProbes;
    }

    public void setUp() {
        testContainer.start();

        if (probeBuilder != null) {
            LOG.debug("installing probe {}", probeBuilder);

            try {
                testContainer.installProbe(probeBuilder.build().getStream());
            }
            catch (IOException e) {
                throw new TestContainerException("Unable to build the probe.", e);
            }
        }
    }

    public void tearDown() {
        testContainer.stop();
    }

    @Override
    public void afterSuite() {
    }

    public void afterTest() {
    }

    public void beforeTest() {
    }

    @Override
    public void afterClass() {
        tearDown();
    }

    @Override
    public void beforeClass() {
        setUp();
    }

    @Override
    public void beforeSuite() {
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) throws Exception {
        assert (description != null) : "TestDescription must not be null.";

        if (description.getMethodName() != null) {
            return;
        }

        testContainer.runTest(description, listener);
    }
}
