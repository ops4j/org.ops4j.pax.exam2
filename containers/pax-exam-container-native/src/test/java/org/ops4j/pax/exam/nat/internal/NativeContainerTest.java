/*
 * Copyright 2009 Toni Menzel.
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
package org.ops4j.pax.exam.nat.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.mockito.Matchers;
import org.ops4j.pax.exam.ExamSystem;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * Test
 */
public class NativeContainerTest {

    @Test
    public void emptySetup() throws IOException {
        FrameworkFactory ff = mock(FrameworkFactory.class);
        ExamSystem system = mock(ExamSystem.class);
        NativeTestContainer container = new NativeTestContainer(system, ff);
        assertThat(container, is(notNullValue()));
        verifyNoMoreInteractions(ff);
    }

    // @Test
    public void starting() throws IOException {
        FrameworkFactory ff = mock(FrameworkFactory.class);
        Framework fw = mock(Framework.class);
        when(ff.newFramework(Matchers.<Map<?, ?>> anyObject())).thenReturn(fw);
        ExamSystem system = mock(ExamSystem.class);

        NativeTestContainer container = new NativeTestContainer(system, ff);
        container.start();
        verifyNoMoreInteractions(ff);
        verifyNoMoreInteractions(fw);

    }
}
