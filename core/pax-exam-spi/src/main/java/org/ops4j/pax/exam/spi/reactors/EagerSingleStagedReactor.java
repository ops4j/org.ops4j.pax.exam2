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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestContainerException;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.intern.DefaultTestAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * One target only reactor implementation (simpliest and fastest)
 *
 * @author tonit
 */
public class EagerSingleStagedReactor implements StagedExamReactor {

    private static final Logger LOG = LoggerFactory.getLogger(EagerSingleStagedReactor.class);

    private final List<TestContainer> targetContainer;
    private final List<TestProbeBuilder> probes;
    private final Map<TestAddress, TestContainer> map;

    /**
     * @param containers
     *            to be used
     * @param mProbes
     *            to be installed on all probes
     */
    public EagerSingleStagedReactor(List<TestContainer> containers, List<TestProbeBuilder> mProbes) {
        map = new LinkedHashMap<TestAddress, TestContainer>();
        targetContainer = containers;
        probes = mProbes;

        int index = 0;
        for (TestContainer container : containers) {
            String caption = buildCaption(containers, container, index);
            for (TestProbeBuilder builder : mProbes) {
                // each probe has addresses.
                for (TestAddress a : builder.getTests()) {
                    // we need to create a new, because "a" exists for each test container
                    // this new address makes the test (reachable via getTargets() ) reachable
                    // directly.
                    map.put(new DefaultTestAddress(a, caption), container);
                }
            }
            index++;
        }
    }

    private String buildCaption(List<TestContainer> containers, TestContainer container, int index) {
        if (containers.size() == 1) {
            return container.toString();
        }
        else {
            return String.format("%s[%d]", container.toString(), index);
        }
    }

    public void setUp() {
        for (TestContainer container : targetContainer) {
            container.start();

            for (TestProbeBuilder builder : probes) {
                LOG.debug("installing probe " + builder);

                try {
                    container.installProbe(builder.build().getStream());
                }
                catch (IOException e) {
                    throw new TestContainerException("Unable to build the probe.", e);
                }
            }
        }
    }

    @Override
    public Set<TestAddress> getTargets() {
        return map.keySet();
    }

    public void tearDown() {
        for (TestContainer container : targetContainer) {
            container.stop();
        }
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

        TestContainer testContainer = targetContainer.get(0);
        testContainer.runTest(description, listener);
    }
}
