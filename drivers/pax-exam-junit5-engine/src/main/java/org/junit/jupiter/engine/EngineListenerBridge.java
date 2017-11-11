/*
 * Copyright 2017 OPS4J Contributors
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
package org.junit.jupiter.engine;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @author Harald Wellmann
 *
 */
public class EngineListenerBridge implements TestExecutionListener {


    private EngineExecutionListener engineListener;
    private TestDescriptor root;

    public EngineListenerBridge(EngineExecutionListener engineListener, TestDescriptor root) {
        this.engineListener = engineListener;
        this.root = root;
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        TestDescriptor testDescriptor = root.findByUniqueId(UniqueId.parse(testIdentifier.getUniqueId())).get();
        engineListener.executionSkipped(testDescriptor, reason);
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        TestDescriptor testDescriptor = root.findByUniqueId(UniqueId.parse(testIdentifier.getUniqueId())).get();
        engineListener.executionStarted(testDescriptor);
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier,
        TestExecutionResult testExecutionResult) {
        TestDescriptor testDescriptor = root.findByUniqueId(UniqueId.parse(testIdentifier.getUniqueId())).get();
        engineListener.executionFinished(testDescriptor, testExecutionResult);
    }
}
