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
package org.ops4j.pax.exam.spi.reactors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ops4j.pax.exam.TestContainer;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.spi.StagedExamReactor;

/**
 *
 */
public abstract class BaseStagedReactorTest {

    abstract protected StagedExamReactor getReactor(List<TestContainer> containers,
        List<TestProbeBuilder> providers);

    @Test
    public void testEmptyContainersAndBuilders() throws IOException {
        List<TestContainer> containers = new ArrayList<TestContainer>();
        List<TestProbeBuilder> providers = new ArrayList<TestProbeBuilder>();

        StagedExamReactor reactor = getReactor(containers, providers);
        assertThat(reactor, is(notNullValue()));
    }
}
