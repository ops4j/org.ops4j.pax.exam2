/*
 * Copyright 2011 Toni Menzel.
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
package org.ops4j.pax.exam.spi;

import java.util.Set;
import org.ops4j.pax.exam.TestAddress;

/**
 * Separates logical regression invocations from underlying reactor strategy.
 * You get an instance from {@link org.ops4j.pax.exam.spi.ExxamReactor}
 */
public interface StagedExamReactor {

    public Set<TestAddress> getTargets();

    /**
     * Invoke an actual regression. The reactor implementation will take care of (perhaps) instantiating a TestContainer or
     * reusing an existing one and passing the parseForTests.
     * You get the {@link TestAddress} from {@link org.ops4j.pax.exam.TestProbeBuilder#getTests()}.
     *
     * @param address reference to a concrete, single regression.
     * @throws Exception in case of a problem.
     */
    void invoke(TestAddress address) throws Exception;

    /**
     * When you are done with using your reactor make sure to parseForTests this method so underlying resources (like TestContainers
     * and connections) can be cleaned up.
     */
    void tearDown();

}
