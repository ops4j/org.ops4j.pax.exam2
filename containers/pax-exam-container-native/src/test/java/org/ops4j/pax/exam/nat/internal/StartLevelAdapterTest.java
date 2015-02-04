/*
 * Copyright 2015 Mario Krizmanić.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.startlevel.FrameworkStartLevel;

/**
 * Unit tests for the {@link StartLevelAdapter} class.
 */
public class StartLevelAdapterTest {

    private FrameworkStartLevel frameworkStartLevel = mock(FrameworkStartLevel.class);
    private StartLevelAdapter startLevel = new StartLevelAdapter(frameworkStartLevel);

    @Before
    public void setUp() {
        reset(frameworkStartLevel);
    }

    @Test
    public void testGetStartLevel() {
        int startLevelToReturn = 10;

        when(frameworkStartLevel.getStartLevel()).thenReturn(startLevelToReturn);

        int actualStartLevel = startLevel.getStartLevel();

        verify(frameworkStartLevel).getStartLevel();
        assertEquals(startLevelToReturn, actualStartLevel);
    }

    @Test
    public void testSetStartLevel() {
        int startLevelToSet = 6;

        startLevel.setStartLevel(startLevelToSet);

        verify(frameworkStartLevel).setStartLevel(startLevelToSet);;
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetBundleStartLevel() {
        startLevel.getBundleStartLevel(mock(Bundle.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetBundleStartLevel() {
        int startLevelToSet = 6;

        startLevel.setBundleStartLevel(mock(Bundle.class), startLevelToSet);
    }

    @Test
    public void testGetInitialBundleStartLevel() {
        int initialBundleStartLevel = 4;

        when(frameworkStartLevel.getInitialBundleStartLevel()).thenReturn(initialBundleStartLevel);

        int actualBundleStartLevel = startLevel.getInitialBundleStartLevel();

        verify(frameworkStartLevel).getInitialBundleStartLevel();
        assertEquals(initialBundleStartLevel, actualBundleStartLevel);
    }

    @Test
    public void testSetInitialBundleStartLevel() {
        int startLevelToSet = 11;

        startLevel.setInitialBundleStartLevel(startLevelToSet);

        verify(frameworkStartLevel).setInitialBundleStartLevel(startLevelToSet);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIsBundlePersistentlyStarted() {
        startLevel.isBundlePersistentlyStarted(mock(Bundle.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIsBundleActivationPolicyUsed() {
        startLevel.isBundleActivationPolicyUsed(mock(Bundle.class));
    }
}
