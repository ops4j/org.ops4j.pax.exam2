/*
 * Copyright (C) 2010 Toni Menzel
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
package org.ops4j.pax.exam.spi.reactors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestDescription;
import org.ops4j.pax.exam.TestListener;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.intern.DefaultTestAddress;

/**
 * This will use new containers for any regression (hence confined)
 */
public class PerMethodStagedReactor implements StagedExamReactor {

    private final List<TestProbeBuilder> probes;
    private final List<TestContainer> testContainers;

    private final Map<TestAddress, TestContainer> map;

    /**
     * @param containers
     *            to be used
     * @param probes
     *            probes to be installed
     */
    public PerMethodStagedReactor(List<TestContainer> containers, List<TestProbeBuilder> probes) {
        this.testContainers = containers;
        this.probes = probes;
        map = new LinkedHashMap<TestAddress, TestContainer>();
        int index = 0;
        for (TestContainer container : containers) {
            String caption = buildCaption(containers, container, index);
            for (TestProbeBuilder builder : probes) {
                for (TestAddress a : builder.getTests()) {
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
        // empty
    }

    @Override
    public void invoke(TestAddress address) throws Exception {
        assert (address != null) : "TestAddress must not be null.";
        // you can directly invoke:
        TestContainer container = map.get(address);
        if (container == null) {
            throw new IllegalArgumentException("TestAddress " + address
                + " not from this reactor? Got it from getTargets() really?");
        }
        container.start();
        try {
            for (TestProbeBuilder builder : probes) {
                container.installProbe(builder.build().getStream());
            }
            container.call(address);
        }
        finally {
            container.stop();
        }

    }

    @Override
    public Set<TestAddress> getTargets() {
        return map.keySet();
    }

    public void tearDown() {
        // empty
    }

    @Override
    public void afterSuite() {
        // empty
    }

    public void afterTest() {
        // empty
    }

    public void beforeTest() {
        // empty
    }

    @Override
    public void afterClass() {
        // empty
    }

    @Override
    public void beforeClass() {
        // empty
    }

    @Override
    public void beforeSuite() {
        // empty
    }

    @Override
    public void runTest(TestDescription description, TestListener listener) throws Exception {
        assert (description != null) : "TestDescription must not be null.";
        if (description.getMethodName() == null) {
            return;
        }
        TestContainer container = testContainers.get(0);
        TestProbeBuilder probeBuilder = probes.get(0);
        container.start();
        container.installProbe(probeBuilder.build().getStream());
        try {
            container.runTest(description, listener);
        }
        finally {
            container.stop();
        }
    }
}
