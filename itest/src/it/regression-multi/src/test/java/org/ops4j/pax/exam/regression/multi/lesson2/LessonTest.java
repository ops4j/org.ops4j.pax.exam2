/*
 * Copyright (C) 2011 Toni Menzel
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
package org.ops4j.pax.exam.regression.multi.lesson2;

import static org.ops4j.pax.exam.spi.PaxExamRuntime.*;
import static org.ops4j.pax.exam.CoreOptions.*;

import java.io.IOException;

import org.junit.Test;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.TestAddress;
import org.ops4j.pax.exam.TestContainerFactory;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.TestProbeProvider;
import org.ops4j.pax.exam.options.libraries.JUnitBundlesOption;
import org.ops4j.pax.exam.spi.ExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactor;
import org.ops4j.pax.exam.spi.StagedExamReactorFactory;
import org.ops4j.pax.exam.spi.DefaultExamReactor;
import org.ops4j.pax.exam.spi.reactors.EagerSingleStagedReactorFactory;

/**
 * This is a copy of lesson 1 but with a higher level abstraction on how we interact with TestContainers.
 * In Lesson1 we learned about the lifecycle of TestContainers, and that we need to control it.
 *
 * When writing tests, you may not be really interested into how to start/stop a container (or better: all the containers).
 * You may really want to write a setup where start/stopping is not your job but part of a "strategy".
 * This can be like:
 * - I want a fresh container for every test
 * - I want to reuse containers across tests
 * etc.
 *
 * This is what {@link ExxamReactor} is about.
 * You feed the Reactor with the TestContainerFactory, Configuration (Options!) and Probes.
 * The {@link ExxamReactor#stage(org.ops4j.pax.exam.spi.StagedExamReactorFactory)} gives you access to launching the tests.
 * You don't see (and care) when a container is started and stopped.
 */
public class LessonTest {

    @Test
    public void testLesson2Unit1()
        throws Exception
    {
        TestContainerFactory factory = getTestContainerFactory();
        ExamSystem system = createTestSystem();

        ExamReactor reactor = new DefaultExamReactor( system, factory );

        TestProbeProvider probe = makeProbe(system);
        reactor.addProbe( probe );
        reactor.addConfiguration( options( new JUnitBundlesOption() ) );

        StagedExamReactorFactory strategy = new EagerSingleStagedReactorFactory();
        StagedExamReactor stagedReactor = reactor.stage( strategy );
        try {
            for( TestAddress call : stagedReactor.getTargets() ) {
                stagedReactor.invoke( call );
            }

        } finally {
            stagedReactor.tearDown();
        }
    }

    /**
     * Here's how you make a probe. As mentioned in {@link Probe} its a bundle that is computed on the fly.
     * You - as a user - just add "tests", and invoke "build() at the very end. You will end up with something ({@link TestProbeProvider})
     * where you get the physicall bundle ({@link org.ops4j.pax.exam.TestProbeProvider#getStream()}) from.
     *
     * @return Ready to use probe
     *
     * @throws java.io.IOException creating probe can fail.
     */
    private TestProbeProvider makeProbe(ExamSystem system)
        throws IOException
    {
        TestProbeBuilder probe = system.createProbe();
        probe.addTest(
            Probe.class, "probe1"
        );
        probe.addTest(
            Probe.class, "probe2"
        );

        return probe.build();
    }
}
