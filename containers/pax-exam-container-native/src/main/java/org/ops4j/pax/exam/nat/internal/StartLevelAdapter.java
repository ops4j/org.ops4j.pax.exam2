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

import org.osgi.framework.Bundle;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.service.startlevel.StartLevel;

/**
 * Adapts {@link FrameworkStartLevel} to match deprecated {@link StartLevel}
 * interface.
 * <p>
 * This adapter is introduced to support backward compatibility with OSGi 4.0.0
 * version because FrameworkStartLevel appeared in the newer versions.
 */
@SuppressWarnings("deprecation")
public class StartLevelAdapter implements StartLevel {

    private final FrameworkStartLevel startLevel;

    public StartLevelAdapter(FrameworkStartLevel startLevel) {
        this.startLevel = startLevel;
    }

    @Override
    public int getStartLevel() {
        return startLevel.getStartLevel();
    }

    @Override
    public void setStartLevel(int startlevel) {
        startLevel.setStartLevel(startlevel);
    }

    @Override
    public int getBundleStartLevel(Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBundleStartLevel(Bundle bundle, int startlevel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInitialBundleStartLevel() {
        return startLevel.getInitialBundleStartLevel();
    }

    @Override
    public void setInitialBundleStartLevel(int startlevel) {
        startLevel.setInitialBundleStartLevel(startlevel);
    }

    @Override
    public boolean isBundlePersistentlyStarted(Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBundleActivationPolicyUsed(Bundle bundle) {
        throw new UnsupportedOperationException();
    }
}
